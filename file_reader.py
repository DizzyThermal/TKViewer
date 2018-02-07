#!/usr/bin/env python

"""File reader for NexusTK map files."""

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

import array
import os

"""Base Class for other Handlers."""
class FileHandler(object):
    def __init__(self, file_path):
        self.file_path = file_path
        self.file_handler = open(self.file_path, 'rb')
        self.file_name = os.path.split(self.file_path)[1]

    def read(self, read_type='int', seek_pos=None, little_endian=False,
            signed=False, items_to_read=1):
        if seek_pos:
            self.seek(seek_pos)

        if read_type == 'word':
            fmt = 'I'
            items_to_read = 2
        elif read_type == 'short':
            if signed:
                fmt = 'h'
            else:
                fmt = 'H'
        elif read_type == 'byte':
            fmt = 'B'
        else: # int
            if signed:
                fmt = 'i'
            else:
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


"""The DSC file handler representing *.dsc files."""
class DSCHandler(FileHandler):
    _PART_COUNT_POS = 23

    def __init__(self, *args):
        super(DSCHandler, self).__init__(*args)
        self.seek(self._PART_COUNT_POS)
        self.part_count = self.read('int')
        self.parts = []
        for i in range(self.part_count):
            part = {}
            part['id'] = self.read('int')
            part['palette_id'] = self.read('int')
            part['frame_index'] = self.read('int')
            part['frame_count'] = self.read('int')
            self.seek(14, whence=1)
            
            # Skip chunks/blocks
            self.seek(1036, whence=1)
            #part['chunk_count'] = self.read('int')
            #part['chunks'] = []
            #for j in range(part['chunk_count']):
            #    chunk = {}
            #    chunk['id'] = self.read('int')
            #    chunk['unk'] = self.read('int')
            #    chunk['block_count'] = self.read('int')
            #    chunk['blocks'] = []
            #    for k in range(chunk['block_count']):
            #        block = {}
            #        block['id'] = self.read('byte')
            #        self.seek(4, whence=1)
            #        block['unk'] = self.read('int')
            #        chunk['blocks'].append(block)
            #    part['chunks'].append(chunk)
            self.parts.append(part)


"""The EPF file handler representing *.epf files."""
class EPFHandler(FileHandler):
    def __init__(self, *args):
        super(EPFHandler, self).__init__(*args)
        self.frame_count = self.read('short')
        self.width = self.read('short')
        self.height = self.read('short')
        self.bitBLT = self.read('short')
        self.pixel_data_length = self.read('int')
        self.pixel_data = self.file_handler.read(self.pixel_data_length)

        self.frames = []
        for i in range(self.frame_count):
            top_left = self.read('int', signed=True)
            top = (top_left & 0x0000FFFF)
            left = (top_left >> 0x10)

            bottom_right = self.read('int', signed=True)
            bottom = (bottom_right & 0x0000FFFF)
            right = (bottom_right >> 0x10)

            pixel_data_offset = self.read('int')
            stencil_data_offset = self.read('int')

            frame = {
                    'top': top,
                    'left': left,
                    'bottom': bottom,
                    'right': right,
                    'width': right - left,
                    'height': bottom - top,
                    'pixel_data_offset': pixel_data_offset,
                    'stencil_data_offset': stencil_data_offset
            }
            self.frames.append(frame)


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


"""The PAL file handler representing *.pal files."""
class PALHandler(FileHandler):
    _ANIMATION_COUNT_POS = 24
    _COLOR_COUNT = 256

    def __init__(self, *args):
        super(PALHandler, self).__init__(*args)
        self.pal_count = 1

        try:
            header = self.peek(9).decode()
        except UnicodeDecodeError:
            header = self.peek(9)

        if header != 'DLPalette':
            self.pal_count = self.read('int')

        self.pals = []
        for i in range(self.pal_count):
            pal = {}
            self.seek(self._ANIMATION_COUNT_POS, whence=1)
            pal['animation_color_count'] = self.read('byte')
            self.seek(7, whence=1)
            pal['animation_color_offsets'] = []
            for j in range(pal['animation_color_count']):
                pal['animation_color_offsets'].append(self.read('short'))

            pal['colors'] = []
            for j in range(self._COLOR_COUNT):
                colors = self.read('int', little_endian=True)
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


"""The SObj.tbl file handler, defining static objects."""
class SObjTBLHandler(FileHandler):
    def __init__(self, *args, old_format=False):
        super(SObjTBLHandler, self).__init__(*args)
        self.object_count = self.read('int')
        self.seek(2, whence=1) # unknown short

        self.objects = []
        for i in range(self.object_count):
            if not old_format:
                self.seek(5, whence=1) # unknown int/byte

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


"""The TBL file handler representing Tile{A,B,C}.tbl files."""
class TBLHandler(FileHandler):
    def __init__(self, *args, old_format=False):
        super(TBLHandler, self).__init__(*args)
        self.tile_count = self.read('int')
        if old_format:
            self.palette_count = self.read('int')
            self.seek(3, 1) # unknown
        self.palette_indices = []
        for i in range(self.tile_count):
            self.palette_indices.append(self.read('byte'))
            self.seek(1,1)