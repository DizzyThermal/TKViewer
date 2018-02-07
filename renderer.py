#!/usr/bin/env python

"""Renderer for NexusTK files."""

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

from file_reader import EPFHandler

from PIL import Image


class Renderer(object):
    _ALPHA_COLORS = (
        (0x00, 0x3B, 0x00),
        (0x00, 0xC8, 0xC8),
        (0xFF, 0xFF, 0xFF),
        (0x00, 0x3F, 0x00),
        (0xD1, 0xB6, 0xE3))
    _TILE_B_OFFSET = 49151

    def __init__(self, epfs, pals, tbl=None, sobj_tbl=None, dsc=None):
        def handler_to_list(lst, handler):
            if isinstance(lst, handler):
                return [lst]

            return lst

        super(Renderer, self).__init__()
        self.epfs = handler_to_list(epfs, EPFHandler)
        self.pals = pals

        # Tile Description File
        self.tbl = tbl
        # Static Object Description File
        self.sobj_tbl = sobj_tbl
        # Part Description File
        self.dsc = dsc

    # Required: PAL, TBL (Tiles)
    def render_tile(self, index, alpha_rgb=(0, 0, 0), background_color='black', dim=(48, 48)):
        def get_epf_index(index):
            idx = 0
            frame_count = 0
            while idx < len(self.epfs):
                if index >= (frame_count + self.epfs[idx].frame_count):
                    frame_count += self.epfs[idx].frame_count
                    idx += 1
                else:
                    return idx, frame_count

            return idx, frame_count

        (epf_index, offset) = get_epf_index(index)
        frame = self.epfs[epf_index].frames[index - offset]
        width = frame['width']
        height = frame['height']

        pixel_data_offset = frame['pixel_data_offset']
        pixel_data = self.epfs[epf_index].pixel_data[pixel_data_offset:pixel_data_offset + (width * height)]

        if not self.tbl:
            palette_index = 0
        else:
            palette_index = self.tbl.palette_indices[index]

        pixel_bytes = list()
        for i in range(height * width):
            pixel_byte = self.pals[palette_index]['colors'][pixel_data[i]]['rgb']
            if pixel_byte in self._ALPHA_COLORS:
                pixel_byte = alpha_rgb

            pixel_bytes.append(pixel_byte)

        if dim:
            sub_image = Image.new('RGBA', (width, height), background_color)
            sub_image.putdata(pixel_bytes)

            image = Image.new('RGBA', dim, background_color)
            image.paste(sub_image, (frame['left'], frame['top']))
        else:
            image = Image.new('RGBA', (width, height))
            image.putdata(pixel_bytes)

        return image

    # Required: PAL, SOBJ_TBL (Static Objects)
    def render_static_object(self, index, alpha_rgb=(0, 0, 0), background_color='black',
                             height_pad=0, dim=(48, 48)):
        obj = self.sobj_tbl.objects[index]
        if not height_pad:
            height_pad = obj['height']

        image = Image.new('RGBA', (dim[0], (height_pad * dim[1])), background_color)
        for i in range(obj['height']):
            if obj['tile_indices'][i] == -1:
                im = Image.new('RGBA', dim, background_color)
            else:
                im = self.render_tile(obj['tile_indices'][i],
                                      alpha_rgb=alpha_rgb,
                                      background_color=background_color,
                                      dim=dim)

            b = ((height_pad - i) * dim[1])
            image.paste(im, (0, b - dim[1], dim[0], b))

        return image
