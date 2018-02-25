#!/usr/bin/env python

"""Resources for NexusTK."""

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

import os
import pygame

from file_reader import DATHandler

"""Config Values :: Edit these values to match your system."""
config = dict()
config['nexus_data_dir'] = r'C:\Program Files (x86)\KRU\NexusTK\Data'
config['data_dir'] = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'Data')
config['resources_dir'] = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'Resources')
config['export_dir'] = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'Exports')
config['tile_export_dir'] = os.path.join(config['export_dir'], 'Tiles')
config['sobj_export_dir'] = os.path.join(config['export_dir'], 'Static Objects')
config['maps_dir'] = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'Maps')
config['client_icon'] = os.path.join(config['resources_dir'], 'client_icon.png')
config['client_width'] = 17
config['client_height'] = 15
config['viewer_icon'] = os.path.join(config['resources_dir'], 'viewer_icon.ico')

MAP_IDS = {
    '000330': {'map_name': 'Buya'},
    '001015': {'map_name': 'Wilderness Valley'},
    '003941': {'map_name': 'Phoenix Hall'},
    '027800': {'map_name': 'Malgalod'}
}

"""Extracts dats to the Data Directory"""
def extract_dats(dats, extract_directory=config['data_dir']):
    if os.path.exists(extract_directory):
        for i in range(len(dats)):
            dat = DATHandler(dats[i])
            dat.export_files(extract_dir=extract_directory)

"""Returns the TK ID of the Map Name passed in, defined above in MAP_IDS."""
def get_id_from_map_name(map_name):
    for map_id, map_info in MAP_IDS.items():
        if map_info['map_name'] == map_name:
            return map_id

    return None

"""Creates a Sprite to represent a part"""
class Part(pygame.sprite.Sprite):
    def __init__(self, part):
        super().__init__()

        self.image = pygame.image.fromstring(part.tobytes(), part.size, part.mode)
        self.rect = self.image.get_rect()

    def draw(self, screen):
        screen.blit(self.image, self.rect)
