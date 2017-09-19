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
    OBJ_COUNT_POS = 0
    OBJ_POS = 6
    OBJ_WIDTH = 24
    TILE_HEIGHT = 24
    TILEC_FILE = 'TileC.epf'

    def __init__(self, *args, epf=None):
        super(SObjTBLHandler, self).__init__(*args)
        self._object_count = 0
        self._epf = epf
        self._objects = []
        self._images = []


    class SObj(object):
        def __init__(self, epf, movement_direction, height, tile_indicies):
            self.epf = epf
            self.movement_direction = movement_direction or 0
            self.height = height or 0
            self.tile_indicies = tile_indicies
            self._image = None
            self._background_color = 'black'

        def get_image(self, alpha_rgb=(0, 0, 0), background_color='black',
                height_pad=0):
            if not self._image or (self._background_color != background_color):
                if not height_pad:
                    height_pad = self.height

                self._image = Image.new('RGBA',
                        (24, (height_pad * 24)), background_color)
                self._background_color = background_color
                for i in range(self.height):
                    l = 0
                    b = ((height_pad - i) * 24)
                    r = 24
                    t = b - 24
                    if self.tile_indicies[i] == -1:
                        imm = Image.new('RGBA', (24, 24), background_color)
                    else:
                        imm = self.epf.get_tile(self.tile_indicies[i],
                                alpha_rgb=alpha_rgb, background_color=background_color)

                    self._image.paste(imm, (l, t, r, b))

            return self._image


    @property
    def object_count(self):
        if not self._object_count:
            self._object_count = self.read('short',
                    seek_pos=SObjTBLHandler.OBJ_COUNT_POS)

        return self._object_count

    @property
    def epf(self):
        if not self._epf:
            epf_file_path = self.file_path.replace('SObj.tbl', self.TILEC_FILE)
            self._epf = EPFHandler(epf_file_path)
            self._epf.tile_count
            self._epf.tile_entries

        return self._epf

    @property
    def objects(self):
        if not self._objects:
            self.file_handler.seek(SObjTBLHandler.OBJ_POS)
            for i in range(self.object_count):
                movement_direction = self.read('byte')
                height = self.read('byte')
                tile_indicies = []
                for j in range(height):
                    tile_index = self.read('short')
                    tile_indicies.append(tile_index - 1) # Zero-based

                self._objects.append(SObjTBLHandler.SObj(self.epf,
                    movement_direction, height, tile_indicies))

        return self._objects

    def get_images(self, max=None, alpha_rgb=(0, 0, 0),
            background_color='black', height_pad=0):
        if not max:
            max = self.object_count
        
        images = []
        sobjs = self.objects
        for i in range(len(sobjs)):
            if i >= max:
                break

            images.append(sobjs[i].get_image(alpha_rgb=alpha_rgb,
                background_color=background_color, height_pad=height_pad))

        return images


"""The PAL file handler representing *.pal files."""
class PALHandler(FileHandler):
    _COLOR_POS = 32
    _COLOR_COUNT = 256

    def __init__(self, *args):
        super(PALHandler, self).__init__(*args)
        self._colors = []


    class Color(object):
        def __init__(self, red, green, blue, alpha):
            self.red = red or 0
            self.green = green or 0
            self.blue = blue or 0
            self.alpha = alpha or 0

        def rgb(self):
            return self.red, self.green, self.blue

        def rgba(self):
            return self.red, self.green, self.blue, self.alpha


    @property
    def colors(self):
        if not self._colors:
            self.file_handler.seek(self._COLOR_POS)
            for i in range(self._COLOR_COUNT):
                red = self.read('byte')
                green = self.read('byte')
                blue = self.read('byte')
                alpha = self.read('byte')

                color = PALHandler.Color(red, green, blue, alpha)
                self._colors.append(color)

        return self._colors


"""The EPF file handler representing *.epf files."""
class EPFHandler(FileHandler):
    _TILES_POS = 0
    _HEIGHT_POS = 2
    _WIDTH_POS = 4
    _PIXEL_DATA_POS = 8
    _PIXELS_POS = 12
    _ALPHA_COLORS = (
            (0x00, 0x3B, 0x00),
            (0x00, 0xC8, 0xC8),
            (0xFF, 0xFF, 0xFF))
    _TILE_B_OFFSET = 49151

    def __init__(self, *args):
        super(EPFHandler, self).__init__(*args)
        self._tile_count = 0
        self._height = 0
        self._width = 0
        self._pixel_data_length = 0
        self._pixel_data = []
        self._tile_entries = []
        self._tbl = None
        self._pals = []


    """Tile Entry"""
    class TileEntry(object):
        def __init__(self, pad_top, pad_left, height, width, pixel_data_offset,
                unk_offset):
            self.pad_top = pad_top
            self.pad_left = pad_left
            self.height = height
            self.width = width
            self.pixel_data_offset = pixel_data_offset or 0
            self.unk_offset = unk_offset


    @property
    def tile_count(self):
        if not self._tile_count:
            self._tile_count = self.read('short', seek_pos=self._TILES_POS)

        return self._tile_count

    @property
    def height(self):
        if not self._height:
            self._height = self.read('short', seek_pos=self._HEIGHT_POS)

        return self._height

    @property
    def width(self):
        if not self._width:
            self._width = self.read('short', seek_pos=self._WIDTH_POS)

        return self._width

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
                pad_top = self.read('short')
                pad_left = self.read('short')
                height = self.read('short')
                width = self.read('short')
                pixel_data_offset = self.read('int')
                unk_offset = self.read('int')
                tile_entry = EPFHandler.TileEntry(pad_top, pad_left, height,
                        width, pixel_data_offset, unk_offset)
                self._tile_entries.append(tile_entry)

        return self._tile_entries

    @property
    def tbl(self):
        if not self._tbl:
            tbl_file_path = self.file_path.replace('epf', 'tbl')
            self._tbl = TBLHandler(tbl_file_path)
            self._tbl
            self._tbl.tile_count
            self._tbl.palette_count
            self._tbl.palette_indices

        return self._tbl

    @property
    def pals(self):
        if not self._pals:
            for i in range(self.tbl.palette_count):
                pal_file_path = self.file_path.replace(
                    '.epf', '{}.pal'.format(i))
                pal = PALHandler(pal_file_path)
                pal.colors
                self._pals.append(pal)

        return self._pals

    def close(self):
        super(EPFHandler, self).close()
        if self._tbl and not self._tbl.closed():
            self._tbl.close()
        for i in range(len(self.pals)):
            if self.pals[i] and not self.pals[i].closed():
                self.pals[i].closed()

    def get_tile(self, index, alpha_rgb=(0, 0, 0), background_color='black', is_b=False):
        if is_b:
            index -= self._TILE_B_OFFSET

        tile = self.tile_entries[index]
        height = tile.height - tile.pad_top
        width = tile.width - tile.pad_left

        pixel_data_offset = tile.pixel_data_offset
        pixel_data = self.pixel_data[pixel_data_offset:pixel_data_offset+(height*width)]
        palette_index = self.tbl.palette_indices[index]

        pixel_bytes = []
        for i in range(height * width):
            pixel_byte = self.pals[palette_index].colors[pixel_data[i]].rgb()
            if pixel_byte in self._ALPHA_COLORS:
                pixel_byte = alpha_rgb

            pixel_bytes.append(pixel_byte)

        if tile.pad_top or tile.pad_left or height != 24 or width != 24:
            sub_image = Image.new('RGBA', (width, height), background_color)
            sub_image.putdata(pixel_bytes)
            image = Image.new('RGBA', (24, 24), background_color)
            image.paste(sub_image, (tile.pad_left, tile.pad_top))
        else:
            image = Image.new('RGBA', (width, height))
            image.putdata(pixel_bytes)

        return image

    def get_tiles(self, max=None, alpha_rgb=(0, 0, 0), background_color='black'):
        if not max:
            max = self.tile_count

        images = []
        for i in range(self.tile_count):
            if i >= max:
                break

            images.append(self.get_tile(i, alpha_rgb=alpha_rgb,
                background_color=background_color))

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

    class Tile(object):
        def __init__(self, ab_tile, sobj_tile):
            self.ab_tile = ab_tile
            self.sobj_tile = sobj_tile

    @property
    def width(self):
        if not self._width:
            self._width = self.read('short', seek_pos=self._WIDTH_POS, little_endian=True)

        return self._width

    @property
    def height(self):
        if not self._height:
            self._height = self.read('short', seek_pos=self._HEIGHT_POS, little_endian=True)

        return self._height
        return self._tile_count

    @property
    def tiles(self):
        if not self._tiles:
            self.file_handler.seek(self._TILE_POS)
            for i in range(self.width * self.height):
                ab_tile = self.read('short', little_endian=True)
                sobj_tile = self.read('short', little_endian=True)
                self._tiles.append(MAPHandler.Tile(ab_tile=ab_tile,
                    sobj_tile=sobj_tile))

        return self._tiles
