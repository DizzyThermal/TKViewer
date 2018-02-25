#!/usr/bin/env python3

"""The start to a light-weight client."""

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

import glob
import os
import pygame
import sys

from file_reader import CMPHandler
from file_reader import DSCHandler
from file_reader import EPFHandler
from file_reader import PALHandler
from file_reader import SObjTBLHandler
from file_reader import TBLHandler
from renderer import MapRenderer
from renderer import Renderer
from resources import Part
from resources import config
from resources import extract_dats
from resources import get_id_from_map_name


if not os.path.exists(os.path.join(config['data_dir'], 'Body.pal')):
    if not os.path.exists(config['data_dir']):
        os.makedirs(config['data_dir'])
    extract_dats(dats=glob.glob(os.path.join(config['nexus_data_dir'], 'tile*.dat')))
    extract_dats(dats=glob.glob(os.path.join(config['nexus_data_dir'], 'char*.dat')))
    extract_dats(dats=glob.glob(os.path.join(config['nexus_data_dir'], 'body*.dat')))
    extract_dats(dats=glob.glob(os.path.join(config['nexus_data_dir'], 'head*.dat')))
    extract_dats(dats=glob.glob(os.path.join(config['nexus_data_dir'], 'face*.dat')))

if not os.path.exists(config['maps_dir']):
    os.makedirs(config['maps_dir'])

tile_pal = PALHandler(os.path.join(config['data_dir'], 'tile.pal'))
tile_tbl = TBLHandler(os.path.join(config['data_dir'], 'tile.tbl'))
tilec_pal = PALHandler(os.path.join(config['data_dir'], 'TileC.pal'))
tilec_tbl = TBLHandler(os.path.join(config['data_dir'], 'TILEC.TBL'))
sobj_tbl = SObjTBLHandler(os.path.join(config['data_dir'], 'SObj.tbl'))
tile_epf_files = glob.glob(os.path.join(config['data_dir'], 'tile*.epf'))
tilec_epf_files = glob.glob(os.path.join(config['data_dir'], 'tilec*.epf'))
head_pal = PALHandler(os.path.join(config['data_dir'], 'Face.pal'))
head_dsc = DSCHandler(os.path.join(config['data_dir'], 'Face.dsc'))
head_epf_files = glob.glob(os.path.join(config['data_dir'], 'Head*.epf'))
body_pal = PALHandler(os.path.join(config['data_dir'], 'Body.pal'))
body_dsc = DSCHandler(os.path.join(config['data_dir'], 'Body.dsc'))
body_epf_files = glob.glob(os.path.join(config['data_dir'], 'Body*.epf'))

# Load Buya (Must have TK000330.cmp in Data Directory)
_MAP_ID = get_id_from_map_name('Buya')
_CMP = '{}\\TK{}.cmp'.format(config['maps_dir'], _MAP_ID)

tile_epfs = []
for i in range(len(tile_epf_files)):
    if 'tilec' not in tile_epf_files[i]:
        epf = EPFHandler(os.path.join(config['data_dir'], 'tile{}.epf'.format(len(tile_epfs))))
        tile_epfs.append(epf)

tilec_epfs = []
for i in range(len(tilec_epf_files)):
    epf = EPFHandler(os.path.join(config['data_dir'], 'tilec{}.epf'.format(len(tilec_epfs))))
    tilec_epfs.append(epf)

head_epfs = []
for i in range(len(head_epf_files)):
    if 'SP' not in head_epf_files[i]:
        epf = EPFHandler(os.path.join(config['data_dir'], 'Head{}.epf'.format(len(head_epfs))))
        head_epfs.append(epf)

body_epfs = []
for i in range(len(body_epf_files)):
    epf = EPFHandler(os.path.join(config['data_dir'], 'Body{}.epf'.format(len(body_epfs))))
    body_epfs.append(epf)

tile_renderer = Renderer(epfs=tile_epfs, pals=tile_pal.pals, tbl=tile_tbl)
sobj_renderer = Renderer(epfs=tilec_epfs, pals=tilec_pal.pals, tbl=tilec_tbl, sobj_tbl=sobj_tbl)
head_renderer = Renderer(epfs=head_epfs, pals=head_pal.pals, dsc=head_dsc)
body_renderer = Renderer(epfs=body_epfs, pals=body_pal.pals, dsc=body_dsc)

cmp_handler = CMPHandler(_CMP)
map_renderer = MapRenderer(tile_renderer=tile_renderer, sobj_renderer=sobj_renderer)

pygame.init()
screen = pygame.display.set_mode((config['client_width'] * 48, config['client_height'] * 48))
pygame.display.set_caption("NexusPY")
client_icon = pygame.image.load(config['client_icon'])
client_icon.set_colorkey((0, 0, 0))
pygame.display.set_icon(client_icon)

# West Gate Buya
current_coordinates = (2, 82)
buya_map = map_renderer.render_map_cropped(map_handler=cmp_handler, xy=current_coordinates)
py_background_image = pygame.image.fromstring(buya_map.tobytes(), buya_map.size, buya_map.mode)
all_sprites_list = pygame.sprite.Group()

# Bowl Cut
head = Part(part=head_renderer.render_part(0, 6))
# Backwards Riding
body = Part(part=body_renderer.render_part(0, 56))

# West Gate Buya
head.rect.x = 48 * 8 + 5
head.rect.y = 48 * 7
body.rect.x = 48 * 8
body.rect.y = 48 * 7 + 30
all_sprites_list.add(body)
all_sprites_list.add(head)

# Full-refresh implementation of movement around the map, very inefficient
#
# This needs to be optimized to only load the column (or row), depending on the direction and
# update the surface instead of refreshing and re-rendering every time movement occurs.
while True:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            sys.exit(0)
        elif event.type == pygame.KEYDOWN and event.key == pygame.K_ESCAPE:
            sys.exit(0)
        elif event.type == pygame.KEYDOWN and event.key == pygame.K_DOWN:
            current_coordinates = (current_coordinates[0], current_coordinates[1] + 1)
            buya_map = map_renderer.render_map_cropped(map_handler=cmp_handler, xy=current_coordinates)
            py_background_image = pygame.image.fromstring(buya_map.tobytes(), buya_map.size, buya_map.mode)
        elif event.type == pygame.KEYDOWN and event.key == pygame.K_UP:
            current_coordinates = (current_coordinates[0], current_coordinates[1] - 1)
            buya_map = map_renderer.render_map_cropped(map_handler=cmp_handler, xy=current_coordinates)
            py_background_image = pygame.image.fromstring(buya_map.tobytes(), buya_map.size, buya_map.mode)
        elif event.type == pygame.KEYDOWN and event.key == pygame.K_LEFT:
            current_coordinates = (current_coordinates[0] - 1, current_coordinates[1])
            buya_map = map_renderer.render_map_cropped(map_handler=cmp_handler, xy=current_coordinates)
            py_background_image = pygame.image.fromstring(buya_map.tobytes(), buya_map.size, buya_map.mode)
        elif event.type == pygame.KEYDOWN and event.key == pygame.K_RIGHT:
            current_coordinates = (current_coordinates[0] + 1, current_coordinates[1])
            buya_map = map_renderer.render_map_cropped(map_handler=cmp_handler, xy=current_coordinates)
            py_background_image = pygame.image.fromstring(buya_map.tobytes(), buya_map.size, buya_map.mode)
        elif event.type == pygame.KEYDOWN and event.key == pygame.K_RETURN:
            print('Coordinates: {}'.format(current_coordinates))

    screen.blit(py_background_image, [0, 0])
    all_sprites_list.draw(screen)
    pygame.display.flip()
