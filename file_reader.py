#!/usr/bin/env python

"""File reader for NexusTK map files."""

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

import array
import os

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

    def read(self, read_type, seek_pos=None, little_endian=False):
        if seek_pos:
            self.file_handler.seek(seek_pos)

        items_to_read = 1
        if read_type == 'word':
            fmt = 'I'
            items_to_read = 2
        elif read_type == 'int':
            fmt = 'I'
        elif read_type == 'short':
            fmt = 'H'
        elif read_type == 'byte':
            fmt = 'B'
        else:
            error_message = 'Invalid type "{}" received, unable to read.'
            raise FileHandlerReadTypeException(error_message.format(read_type))

        a = array.array(fmt)
        a.fromfile(self.file_handler, items_to_read)
        if little_endian:
            a.byteswap()

        return a.pop()

    def close(self):
        if not self.file_handler.closed:
            self.file_handler.close()

    def closed(self):
        return self.file_handler.closed


"""The TBL file handler representing Tile{A,B,C}.tbl files."""
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


"""The SObj.tbl file handler, defining static objects."""
class SObjTBLHandler(FileHandler):
    _OBJ_WIDTH = 24
    _OBJ_COUNT_POS = 0
    _OBJ_POS = 6
    _TILE_HEIGHT = 24

    def __init__(self, *args):
        super(SObjTBLHandler, self).__init__(*args)
        self._object_count = 0
        self._objects = []


    class SObj(object):
        def __init__(self, movement_direction, height, tile_indicies):
            self._movement_direction = movement_direction
            self._height = height
            self._tile_indicies = tile_indicies

        @property
        def movement_direction(self):
            if self._movement_direction:
                return self._movement_direction
            else:
                return 0

        @property
        def height(self):
            if self._height:
                return self._height
            else:
                return 0

        @property
        def tile_indicies(self):
            if self._tile_indicies:
                return self._tile_indicies

        def get_image(self):
            if not self._image:
                self._image = Image.new('RGB', (self._OBJ_WIDTH, (self.height
                    * self._TILE_HEIGHT)))


    @property
    def object_count(self):
        if not self._object_count:
            self._object_count = self.read('int', seek_pos=self._OBJ_COUNT_POS)

        return self._object_count

    @property
    def objects(self):
        if not self._objects:
            self.file_handler.seek(self._OBJ_POS)
            for i in range(self.object_count):
                movement_direction = int.from_bytes(self.read('byte'),
                        byteorder='big')
                height = int.from_bytes(self.read('byte'), byteorder='big')
                tile_indicies = []
                for j in range(height):
                    tile_indicies.append(self.read('short'))

                self._objects.append(SObjTBLHandler.SObj(movement_direction,
                    height, tile_indicies))

        return self._objects


"""The PAL file handler representing *.pal files."""
class PALHandler(FileHandler):
    _COLOR_POS = 32
    _COLOR_COUNT = 256

    def __init__(self, *args):
        super(PALHandler, self).__init__(*args)
        self._colors = []


    class Color(object):
        def __init__(self, red, green, blue):
            self.red = red or 0
            self.green = green or 0
            self.blue = blue or 0

        def rgb(self):
            return self.red, self.green, self.blue


    @property
    def colors(self):
        if not self._colors:
            self.file_handler.seek(self._COLOR_POS)
            for i in range(self._COLOR_COUNT):
                red = self.read('byte')
                green = self.read('byte')
                blue = self.read('byte')
                # Unknown -- usually 0x04
                self.read('byte')

                color = PALHandler.Color(red, green, blue)
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
        def __init__(self, unk, width, height, pixel_data_offset, unk_offset):
            self.unk = unk
            self.width = width
            self.height = height
            self.pixel_data_offset = pixel_data_offset
            self.unk_offset = unk_offset


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
                unk = self.read('int')
                width = self.read('short')
                height = self.read('short')
                pixel_data_offset = self.read('int')
                unk_offset = self.read('int')
                tile_entry = EPFHandler.TileEntry(unk, width, height,
                        pixel_data_offset, unk_offset)
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


"""The MAP file handler representing *.map files."""
class MAPHandler(FileHandler):
    _WIDTH_POS = 0
    _HEIGHT_POS = 2
    _TILE_POS = 4

    def __init__(self, *args):
        super(MAPHandler, self).__init__(*args)
        self._width = 0
        self._height = 0
        self._tiles = []

    @property
    def width(self):
        if not self._width:
            self._width = self.read('short', seek_pos=self._WIDTH_POS, endian='>')

        return self._width

    @property
    def height(self):
        if not self._height:
            self._height = self.read('short', seek_pos=self._HEIGHT_POS, endian='>')

        return self._height
        return self._tile_count

    @property
    def tiles(self):
        if not self._tiles:
            self.file_handler.seek(self._TILE_POS)
            for i in range(self.width * self.height):
                self._tiles.append(self.read('short', endian='>'))
                self.read('short') # other information (TileC?)

        return self._tiles
