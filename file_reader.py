#!/usr/bin/env python

"""File reader for NexusTK map files."""

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

import array
import os

from PIL import Image


class FileHandler(object):
    def __init__(self, file_path):
        self.file_path = file_path
        self.file_handler = open(self.file_path, 'rb')
        self.file_name = os.path.split(self.file_path)[1]

    def read(self, read_type='int', seek_pos=None, little_endian=False,
            items_to_read=1):
        if seek_pos:
            self.seek(seek_pos)

        if read_type == 'word':
            fmt = 'I'
            items_to_read = 2
        elif read_type == 'short':
            fmt = 'H'
        elif read_type == 'byte':
            fmt = 'B'
        else: # int
            fmt = 'I'

        a = array.array(fmt)
        a.fromfile(self.file_handler, items_to_read)

        if little_endian:
            a.byteswap()

        return a.pop()

    def peek(self, number_of_bytes):
        content = self.file_handler.read(number_of_bytes)
        self.seek(-number_of_bytes, 1)

        return content

    def seek(self, seek_pos, whence=0):
        self.file_handler.seek(seek_pos, whence)

    def close(self):
        if not self.file_handler.closed:
            self.file_handler.close()

    def closed(self):
        return self.file_handler.closed


"""The TBL file handler representing Tile{A,B,C}.tbl files."""
class TBLHandler(FileHandler):
    def __init__(self, *args):
        super(TBLHandler, self).__init__(*args)
        self.tile_count = self.read('int')
        self.palette_count = self.read('int')
        self.seek(3, 1) # unknown
        self.palette_indices = []
        for i in range(self.tile_count):
            self.palette_indices.append(self.read('short'))


"""The SObj.tbl file handler, defining static objects."""
class SObjTBLHandler(FileHandler):
    def __init__(self, *args, epf):
        super(SObjTBLHandler, self).__init__(*args)
        self.epf = epf
        self.object_count = self.read('short')

        self.objects = []
        for i in range(self.object_count):

            movement_direction = self.read('byte')
            height = self.read('byte')
            tile_indices = []
            for j in range(height):
                tile_index = self.read('short')
                tile_indices.append(tile_index - 1) # Zero-based

            obj = {
                    'movement_direction': movement_direction,
                    'height': height,
                    'tile_indices': tile_indices}
            self.objects.append(obj)

    def get_image(self, index, alpha_rgb=(0, 0, 0), background_color='black',
            height_pad=0):
        obj = self.objects[index]
        if not height_pad:
            height_pad = obj['height']

        image = Image.new('RGBA', (24, (height_pad * 24)), background_color)
        for i in range(obj['height']):
            if obj['tile_indices'][i] == -1:
                im = Image.new('RGBA', (24, 24), background_color)
            else:
                im = self.epf.get_tile(obj['tile_indices'][i],
                        alpha_rgb=alpha_rgb, background_color=background_color)

            b = ((height_pad - i) * 24)
            image.paste(im, (0, b - 24, 24, b))

        return image

    def get_images(self, max=None, alpha_rgb=(0, 0, 0),
            background_color='black', height_pad=0):
        if not max:
            max = self.object_count
        
        images = []
        for i in range(max):
            images.append(self.get_image(i, alpha_rgb=alpha_rgb,
                background_color=background_color, height_pad=height_pad))

        return images


"""The PAL file handler representing *.pal files."""
class PALHandler(FileHandler):
    _ANIMATION_COUNT_POS = 24
    _COLOR_COUNT = 256

    def __init__(self, *args, number_of_pals=1):
        super(PALHandler, self).__init__(*args)
        header = self.peek(9).decode()

        if header != 'DLPalette':
            number_of_pals = self.read('int')

        self.pals = []
        for i in range(number_of_pals):
            pal = {}
            self.seek(self._ANIMATION_COUNT_POS, 1)
            pal['animation_color_count'] = self.read('byte')
            self.seek(7, whence=1)
            pal['animation_color_offsets'] = []
            for j in range(pal['animation_color_count']):
                pal['animation_color_offsets'].append(self.read('short'))

            pal['colors'] = []
            for j in range(self._COLOR_COUNT):
                colors = self.read('int', little_endian='True')
                color = {
                        'red':      (colors >> 0x18),
                        'green':    (colors & 0x00FF0000) >> 16,
                        'blue':     (colors & 0x0000FF00) >> 8,
                        'alpha':    (colors & 0x000000FF)
                }
                color['rgb'] = (color['red'], color['green'], color['blue'])
                color['rgba'] = (color['red'], color['green'], color['blue'],
                        color['alpha'])

                pal['colors'].append(color)

            self.pals.append(pal)


"""The EPF file handler representing *.epf files."""
class EPFHandler(FileHandler):
    _ALPHA_COLORS = (
            (0x00, 0x3B, 0x00),
            (0x00, 0xC8, 0xC8),
            (0xFF, 0xFF, 0xFF))
    _TILE_B_OFFSET = 49151

    def __init__(self, *args, pals=[], tbl=None):
        super(EPFHandler, self).__init__(*args)
        self.tile_count = self.read('short')
        self.height = self.read('short')
        self.width = self.read('short')
        self.seek(2, whence=1)
        self.pixel_data_length = self.read('int')
        self.pixel_data = self.file_handler.read(self.pixel_data_length)

        self.tiles = []
        for i in range(self.tile_count):
            padding = self.read('int')
            pad_top = (padding & 0x0000FFFF)
            pad_left = (padding >> 0x10)

            dims = self.read('int')
            height = (dims & 0x0000FFFF)
            width = (dims >> 0x10)
            pixel_data_offset = self.read('int')
            stencil_data_offset = self.read('int')
            tile = {
                    'pad_top': pad_top,
                    'pad_left': pad_left,
                    'height': height,
                    'width': width,
                    'pixel_data_offset': pixel_data_offset or 0,
                    'stencil_data_offset': stencil_data_offset or 0}
            self.tiles.append(tile)

        tbl_file_path = self.file_path.replace('epf', 'tbl')
        self.tbl = TBLHandler(tbl_file_path)

        self.pals = []
        for i in range(self.tbl.palette_count):
            pal_file_path = self.file_path.replace(
                '.epf', '{}.pal'.format(i))
            pal = PALHandler(pal_file_path)
            self.pals.append(pal.pals[0])
            pal.close()

    def close(self):
        super(EPFHandler, self).close()
        if self.tbl and not self.tbl.closed():
            self.tbl.close()

    def get_tile(self, index, alpha_rgb=(0, 0, 0), background_color='black',
            sub=True):
        tile = self.tiles[index]
        height = tile['height'] - tile['pad_top']
        width = tile['width'] - tile['pad_left']

        pixel_data_offset = tile['pixel_data_offset']
        pixel_data = self.pixel_data[pixel_data_offset:pixel_data_offset+(height*width)]
        if not self.tbl:
            palette_index = 0
        else:
            palette_index = self.tbl.palette_indices[index]

        pixel_bytes = []
        self.pals
        for i in range(height * width):
            pixel_byte = (
                    self.pals[palette_index]['colors'][pixel_data[i]]['rgb'])
            if pixel_byte in self._ALPHA_COLORS:
                pixel_byte = alpha_rgb

            pixel_bytes.append(pixel_byte)

        if tile['pad_top'] or tile['pad_left'] or (sub and (
            height != 24 or width != 24)):
            sub_image = Image.new('RGBA', (width, height), background_color)
            sub_image.putdata(pixel_bytes)
            image = Image.new('RGBA', (24, 24), background_color)
            image.paste(sub_image, (tile['pad_left'], tile['pad_top']))
        else:
            image = Image.new('RGBA', (width, height))
            image.putdata(pixel_bytes)

        return image

    def get_tiles(self, max=None, alpha_rgb=(0, 0, 0), background_color='black',
            sub=True):
        if not max:
            max = self.tile_count

        images = []
        for i in range(self.tile_count):
            if i >= max:
                break

            images.append(self.get_tile(i, alpha_rgb=alpha_rgb,
                background_color=background_color, sub=sub))

        return images


"""The MAP file handler representing *.map files."""
class MAPHandler(FileHandler):
    def __init__(self, *args):
        super(MAPHandler, self).__init__(*args)
        dims = self.read('int', little_endian=True)
        self.width = dims >> 0x10
        self.height = dims & 0x0000FFFF

        self.tiles = []
        for i in range(self.width * self.height):
            tile = {}
            tiles = self.read('int', little_endian=True)
            tile['ab_tile'] = (tiles >> 0x10) - 1
            tile['sobj_tile'] = (tiles & 0x0000FFFF) + 1
            self.tiles.append(tile)
