#!/usr/bin/env python

"""File reader for EPF/TBL/PAL files from NexusTK."""

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

import os
import struct

from PIL import Image


class FileHandlerReadTypeException(Exception):
    """Raised when an invalid type is specified to read."""


class FileHandler(object):
    def __init__(self, file_path):
        self.file_path = file_path
        self._file_handler = None
        self._file_name = os.path.split(self.file_path)[1]

    @property
    def file_handler(self):
        if not self._file_handler:
            self._file_handler = open(self.file_path, 'rb')

        return self._file_handler

    @property
    def file_name(self):
        if self._file_name:
            return self._file_name

    def read(self, read_type, seek_pos=None):
        if seek_pos:
            self.file_handler.seek(seek_pos)

        if read_type == 'word':
            bytes_to_read = 16
            fmt = '16s'
        elif read_type == 'int':
            bytes_to_read = 4
            fmt = 'i'
        elif read_type == 'short':
            bytes_to_read = 2
            fmt = 'h'
        elif read_type == 'byte':
            bytes_to_read = 1
            fmt = 'c'
        else:
            error_message = 'Invalid type "{}" received, unable to read.'
            raise FileHandlerReadTypeException(error_message.format(read_type))

        raw_bytes = self.file_handler.read(bytes_to_read)
        return struct.unpack(fmt, raw_bytes)[0]

    def close(self):
        if not self.file_handler.closed:
            self.file_handler.close()

    def closed(self):
        return self.file_handler.closed


"""The TBL file handler representing *.tbl files."""
class TBLHandler(FileHandler):
    _TILES_POS = 0
    _PALETTES_POS = 4
    _PALETTE_INDICES_POS = 11

    def __init__(self, *args):
        super(TBLHandler, self).__init__(*args)
        self._tile_count = 0
        self._palette_count = 0
        self._palette_indices = []

    @property
    def tile_count(self):
        if not self._tile_count:
            self._tile_count = self.read('int', seek_pos=self._TILES_POS)

        return self._tile_count

    @property
    def palette_count(self):
        if not self._palette_count:
            self._palette_count = self.read('int', seek_pos=self._PALETTES_POS)

        return self._palette_count

    @property
    def palette_indices(self):
        if not self._palette_indices:
            self.file_handler.seek(self._PALETTE_INDICES_POS)
            for i in range(self.tile_count):
                palette = self.read('short')
                self._palette_indices.append(palette)

        return self._palette_indices


"""The PAL file handler representing *.pal files."""
class PALHandler(FileHandler):
    _COLOR_POS = 32
    _COLOR_COUNT = 256

    def __init__(self, *args):
        super(PALHandler, self).__init__(*args)
        self._colors = []


    class Color(object):
        def __init__(self, color_int):
            color_bytes = '{:08x}'.format(color_int)
            self._blue = color_bytes[2:4]
            self._green = color_bytes[4:6]
            self._red = color_bytes[6:8]

        @property
        def red(self):
            if self._red:
                return self._red
            else:
                return 0

        @property
        def green(self):
            if self._green:
                return self._green
            else:
                return 0

        @property
        def blue(self):
            if self._blue:
                return self._blue
            else:
                return 0

        def rgb(self):
            return int(self.red, 16), int(self.green, 16), int(self.blue, 16)


    @property
    def colors(self):
        if not self._colors:
            self.file_handler.seek(self._COLOR_POS)
            for i in range(self._COLOR_COUNT):
                color = PALHandler.Color(self.read('int'))
                self._colors.append(color)

        return self._colors


"""The EPF file handler representing *.epf files."""
class EPFHandler(FileHandler):
    _TILES_POS = 0
    _WIDTH_POS = 2
    _HEIGHT_POS = 4
    _PIXEL_DATA_POS = 8
    _PIXELS_POS = 12

    def __init__(self, *args):
        super(EPFHandler, self).__init__(*args)
        self._tile_count = 0
        self._width = 0
        self._height = 0
        self._pixel_data_length = 0
        self._pixel_data = []
        self._tile_entries = []
        self._tbl = None
        self._pal = None


    """Table Entry"""
    class TileEntry(object):
        def __init__(self, tile_word):
            tile_word = bytearray(tile_word)
            self._unk = struct.unpack('i', tile_word[0:4])[0]
            self._width = struct.unpack('h', tile_word[4:6])[0]
            self._height = struct.unpack('h', tile_word[6:8])[0]
            self._pixel_data_offset = struct.unpack('i', tile_word[8:12])[0]
            self._unk_offset = struct.unpack('i', tile_word[12:16])[0]


        @property
        def unk(self):
            if self._unk:
                return self._unk

        @property
        def width(self):
            if self._width:
                return self._width

        @property
        def height(self):
            if self._height:
                return self._height

        @property
        def pixel_data_offset(self):
            if self._pixel_data_offset:
                return self._pixel_data_offset

        @property
        def unk_offset(self):
            if self._unk_offset:
                return self._unk_offset


    @property
    def tile_count(self):
        if not self._tile_count:
            self._tile_count = self.read('short', seek_pos=self._TILES_POS)

        return self._tile_count

    @property
    def width(self):
        if not self._width:
            self._width = self.read('short', seek_pos=self._WIDTH_POS)

        return self._width

    @property
    def height(self):
        if not self._height:
            self._height = self.read('short', seek_pos=self._HEIGHT_POS)

        return self._height

    @property
    def pixel_data_length(self):
        if not self._pixel_data_length:
            self._pixel_data_length = self.read('int',
                    seek_pos=self._PIXEL_DATA_POS)

        return self._pixel_data_length

    @property
    def pixel_data(self):
        if not self._pixel_data:
            self.file_handler.seek(self._PIXELS_POS)
            self._pixel_data = self.file_handler.read(self.pixel_data_length)

        return self._pixel_data

    @property
    def tile_entries(self):
        if not self._tile_entries:
            self.file_handler.seek(self._PIXELS_POS + self.pixel_data_length)
            for i in range(self.tile_count):
                tile_entry = EPFHandler.TileEntry(self.read('word'))
                self._tile_entries.append(tile_entry)

        return self._tile_entries

    @property
    def tbl(self):
        if not self._tbl:
            tbl_file_path = self.file_path.replace('epf', 'tbl')
            self._tbl = TBLHandler(tbl_file_path)

        return self._tbl

    @property
    def pal(self):
        if not self._pal:
            pal_file_path = self.file_path.replace('.epf', '0.pal')
            self._pal = PALHandler(pal_file_path)

        return self._pal

    def close(self):
        super(EPFHandler, self).close()
        if self._tbl and not self._tbl.closed():
            self._tbl.close()
        if self._pal and not self._pal.closed():
            self._pal.closed()

    def loadTBL(self):
        self.tbl
        self.tbl.tile_count
        self.tbl.palette_count
        self.tbl.palette_indices

    def loadPAL(self):
        self.pal
        self.pal.colors

    def get_tile(self, index):
        pixel_data_offset = self.tile_entries[index].pixel_data_offset or 0
        pixel_data = self.pixel_data[pixel_data_offset:pixel_data_offset+(24*24)]
        pixel_bytes = []
        for i in range(self.width * self.height):
            pixel_bytes.append(self.pal.colors[pixel_data[i]].rgb())

        image = Image.new('RGBA', (24, 24))
        image.putdata(pixel_bytes)

        return image

    def get_tiles(self, max=None):
        images = []
        if not max:
            max = self.tile_count
        for i in range(self.tile_count):
            if i >= max:
                break

            images.append(self.get_tile(i))

        return images
