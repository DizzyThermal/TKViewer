#!/usr/bin/env python

"""Renderer for NexusTK files."""

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

from PIL import Image

from file_reader import EPFHandler
from file_reader import CMPHandler
from file_reader import MAPHandler
from resources import config


class MapRenderer(object):
    def __init__(self, tile_renderer, sobj_renderer):
        self.tile_renderer = tile_renderer
        self.sobj_renderer = sobj_renderer

    def render_map_cropped(self, map_handler, tile_dim=(48, 48), alpha_rgb=(0, 0, 0, 0), xy=(0, 0)):
        def get_cropped_tiles(tiles, xy):
            map_width = map_handler.width

            cropped_tiles = list()
            for i in range(xy[1], xy[1] + config['client_height']):
                for j in range(xy[0], xy[0] + config['client_width']):
                    cropped_tiles.append(tiles[(map_width * i) + j])

            for i in range(3):
                for j in range(xy[0], xy[0] + config['client_width']):
                    cropped_tiles.append(tiles[(map_width * i) + j])

            return cropped_tiles

        width = config['client_width']
        height = config['client_width']
        tiles = get_cropped_tiles(map_handler.tiles, xy)

        black = Image.new('RGBA', tile_dim, 'black')
        map_image = Image.new('RGBA', (width * tile_dim[0], height * tile_dim[1]), 'black')

        depth = 0
        length = 0
        for i in range(len(tiles)):
            if tiles[i]['ab_tile'] > 0:
                tile = self.tile_renderer.render_tile(tiles[i]['ab_tile'] + 1)
            else:
                tile = black

            map_image.paste(tile, (length * tile_dim[0], depth * tile_dim[1]))

            # If a Static Object is here, render upwards to height
            if tiles[i]['sobj_tile']:
                tile_index = tiles[i]['sobj_tile']
                sobj_image = self.sobj_renderer.render_static_object(tile_index, alpha_rgb=alpha_rgb)

                height = int(sobj_image.height / tile_dim[1])
                map_image.paste(sobj_image, (length * tile_dim[0], (depth - height + 1) * tile_dim[1]),
                                sobj_image)

            if (((i + 1) % width) == 0) and (i != 0):
                depth += 1
                length = 0
            else:
                length += 1

        return map_image

    def render_map(self, map_handler, tile_dim=(48, 48), alpha_rgb=(0, 0, 0, 0)):
        width = map_handler.width
        height = map_handler.height
        tiles = map_handler.tiles

        black = Image.new('RGBA', tile_dim, 'black')
        map_image = Image.new('RGBA', (width * tile_dim[0], height * tile_dim[1]), 'black')

        depth = 0
        length = 0
        for i in range(len(tiles)):
            if tiles[i]['ab_tile'] > 0:
                tile = self.tile_renderer.render_tile(tiles[i]['ab_tile'] + 1)
            else:
                tile = black

            map_image.paste(tile, (length * tile_dim[0], depth * tile_dim[1]))

            # If a Static Object is here, render upwards to height
            if tiles[i]['sobj_tile']:
              tile_index = tiles[i]['sobj_tile']
              sobj_image = self.sobj_renderer.render_static_object(tile_index, alpha_rgb=alpha_rgb)

              height = int(sobj_image.height / tile_dim[1])
              map_image.paste(sobj_image, (length * tile_dim[0], (depth - height + 1) * tile_dim[1]),
                              sobj_image)

            if (((i + 1) % width) == 0) and (i != 0):
                depth += 1
                length = 0
            else:
                length += 1

        return map_image

    def render_map_from_file(self, file_path, xy=None):
        map_handler = None
        if file_path.lower().endswith('.cmp'):
            map_handler = CMPHandler(file_path)
        elif file_path.lower().endswith('.map'):
            map_handler = MAPHandler(file_path)

        if not map_handler:
            return None
        elif xy:
            return self.render_map_cropped(map_handler, xy=xy)
        else:
            return self.render_map(map_handler)


class Renderer(object):
    _ALPHA_COLORS = (
        (0x00, 0x3B, 0x00),
        (0x00, 0xC8, 0xC8),
        (0xFF, 0xFF, 0xFF),
        (0x00, 0x3F, 0x00),
        (0xD1, 0xB6, 0xE3),
        (0x00, 0xC8, 0x96),
        (0xE5, 0xB9, 0xB5),
        (0x00, 0x53, 0x7F),
        (0x00, 0xFF, 0xFF),
        (0x60, 0xFF, 0xB5),
        (0x00, 0x2F, 0x69),
        (0xE3, 0xB6, 0xE1),
        (0x00, 0x4B, 0x00),
        (0xE4, 0xB9, 0xED),
        (0x00, 0x7E, 0xBF),
        (0x5F, 0x63, 0x5F),
        (0x0B, 0x52, 0x80),
        (0x5D, 0x93, 0x9E),
        (0x58, 0x83, 0x8B),
        (0x00, 0x4a, 0x80),
        (0x47, 0x94, 0x86),
        (0xE5, 0xB9, 0xB6),
        (0x7C, 0x7A, 0x7D),
        (0x0F, 0x0F, 0x0F),
        (0x00, 0x00, 0x00),
        (0x24, 0x26, 0x33),
        (0x2C, 0xB6, 0xE3),
        (0xDE, 0xA3, 0xC4),
        (0xCF, 0x9F, 0x84),
        (0xED, 0xAF, 0xDE),
        (0xC4, 0xA3, 0xBC))

    def __init__(self, epfs, pals, tbl=None, sobj_tbl=None, dsc=None):
        # Adds single class to list if passed in by itself
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

    # Required: PAL, DSC (Parts)
    def render_part(self, index, frame=0, epf_index=0, alpha_rgb=(0, 0, 0, 0)):
        part = self.dsc.parts[index]
        frame = self.epfs[epf_index].frames[part['frame_index'] + frame]
        width = frame['width']
        height = frame['height']
        pixel_data_offset = frame['pixel_data_offset']
        pixel_data = self.epfs[epf_index].pixel_data[pixel_data_offset:pixel_data_offset + (width * height)]

        palette_index = part['palette_id']

        pixel_bytes = list()
        for i in range(height * width):
            pixel_byte = self.pals[palette_index]['colors'][pixel_data[i]]['rgb']
            if pixel_byte in self._ALPHA_COLORS:
                pixel_byte = alpha_rgb

            pixel_bytes.append(pixel_byte)

        image = Image.new('RGBA', (width, height))
        image.putdata(pixel_bytes)

        return image

    # Required: PAL, TBL (Tiles)
    def render_tile(self, index, dim=(48, 48), alpha_rgb=(0, 0, 0), sobj=False):
        def get_epf_index(tile_index):
            frame_count = 0
            for idx in range(len(self.epfs)):
                if tile_index < (frame_count + self.epfs[idx].frame_count):
                    return idx, frame_count

                frame_count += self.epfs[idx].frame_count

        try:
            (epf_index, offset) = get_epf_index(index)
        except:
            return None

        frame = self.epfs[epf_index].frames[index - offset]
        width = frame['width']
        height = frame['height']
        pixel_data_offset = frame['pixel_data_offset']
        pixel_data = self.epfs[epf_index].pixel_data[pixel_data_offset:pixel_data_offset + (width * height)]

        palette_index = self.tbl.palette_indices[index]

        pixel_bytes = list()
        if sobj:
            image_mode = 'RGBA'
            for i in range(height * width):
                pixel_byte = self.pals[palette_index]['colors'][pixel_data[i]]['rgb']
                if pixel_byte in self._ALPHA_COLORS:
                    pixel_byte = alpha_rgb

                pixel_bytes.append(pixel_byte)
        else:
            image_mode = 'P'
            pixel_bytes = pixel_data

        if dim and (width != dim[0] or height != dim[1]):
            palette = self.pals[palette_index]['color_stream']
            sub_image = Image.new(image_mode, (width, height))
            if not sobj:
                sub_image.putpalette(palette)

            sub_image.putdata(pixel_bytes)

            image = Image.new(image_mode, dim)
            if not sobj:
                image.putpalette(palette)
            image.paste(sub_image, (frame['left'], frame['top']))
        else:
            image = Image.new(image_mode, (width, height))
            if not sobj:
                image.putpalette(self.pals[palette_index]['color_stream'])
            image.putdata(pixel_bytes)

        return image

    # Required: PAL, SOBJ_TBL (Static Objects)
    def render_static_object(self, index, height_pad=0, dim=(48, 48), alpha_rgb=(0, 0, 0, 0)):
        obj = self.sobj_tbl.objects[index]
        if not height_pad:
            height_pad = obj['height']

        image = Image.new('RGBA', (dim[0], (height_pad * dim[1])))
        for i in range(obj['height']):
            tile_index = obj['tile_indices'][i]

            if tile_index == -1:
                im = Image.new('RGBA', dim)
            else:
                im = self.render_tile(tile_index, dim=dim, alpha_rgb=alpha_rgb, sobj=True)

            b = ((height_pad - i) * dim[1])
            image.paste(im, (0, b - dim[1], dim[0], b))

        return image
