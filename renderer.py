#!/usr/bin/env python

"""Renderer for NexusTK files."""

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

import array
import os

from PIL import Image

"""?"""
class Renderer(object):
    _ALPHA_COLORS = (
            (0x00, 0x3B, 0x00),
            (0x00, 0xC8, 0xC8),
            (0xFF, 0xFF, 0xFF),
            (0x00, 0x3F, 0x00))
    _TILE_B_OFFSET = 49151
    
    def __init__(self, *args, epf, dsc=None, pals=None, sobj_tbl=None, tbl=None):
        super(Renderer, self).__init__(*args)
        self.epf = epf
        self.pals = pals
        # Tile Description File
        self.tbl = tbl
        # Static Object Description File
        self.sobj_tbl = sobj_tbl
        # Part Description File
        self.dsc = dsc

    # Required: PAL, TBL (Tiles)
    def render_tile(self, index, alpha_rgb=(0, 0, 0), background_color='black'):
        frame = self.epf.frames[index]
        width = frame['width']
        height = frame['height']

        pixel_data_offset = frame['pixel_data_offset']
        stencil_data_offset = frame['stencil_data_offset']

        pixel_data = self.epf.pixel_data[pixel_data_offset:pixel_data_offset+(width*height)]

        if not self.tbl:
            palette_index = 0
        else:
            palette_index = self.tbl.palette_indices[index]

        pixel_bytes = []
        for i in range(height * width):
            pixel_byte = self.pals[palette_index]['colors'][pixel_data[i]]['rgb']
            if pixel_byte in self._ALPHA_COLORS:
                pixel_byte = alpha_rgb

            pixel_bytes.append(pixel_byte)

        if frame['top'] or frame['left'] or (height != 24 or width != 24):
            sub_image = Image.new('RGBA', (width, height), background_color)
            sub_image.putdata(pixel_bytes)

            image = Image.new('RGBA', (24, 24), background_color)
            image.paste(sub_image, (frame['left'], frame['top']))
        else:
            image = Image.new('RGBA', (width, height))
            image.putdata(pixel_bytes)

        return image

    # Required: PAL, SOBJ_TBL (Static Objects)
    def render_static_object(self, index, alpha_rgb=(0, 0, 0), background_color='black',
                             height_pad=0):
        obj = self.sobj_tbl.objects[index]
        if not height_pad:
            height_pad = obj['height']

        image = Image.new('RGBA', (24, (height_pad * 24)), background_color)
        for i in range(obj['height']):
            if obj['tile_indices'][i] == -1:
                im = Image.new('RGBA', (24, 24), background_color)
            else:
                im = self.render_tile(obj['tile_indices'][i],
                        alpha_rgb=alpha_rgb, background_color=background_color)

            b = ((height_pad - i) * 24)
            image.paste(im, (0, b - 24, 24, b))

        return image