#!/usr/bin/env python3

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'
__version__ = '1.4'
__version_codename__ = 'Rat'

import array
import os
import signal
import sys

from tk_gui import Ui_MainWindow
from file_reader import EPFHandler
from file_reader import PALHandler
from file_reader import MAPHandler
from file_reader import SObjTBLHandler
from file_reader import TBLHandler
from renderer import Renderer

from PIL import Image
from PIL.ImageQt import ImageQt

from PyQt5.QtCore import Qt
from PyQt5.QtGui import QIcon
from PyQt5.QtGui import QPixmap
from PyQt5.QtWidgets import QAction
from PyQt5.QtWidgets import QApplication
from PyQt5.QtWidgets import QFileDialog
from PyQt5.QtWidgets import QGridLayout
from PyQt5.QtWidgets import QLabel
from PyQt5.QtWidgets import QMainWindow
from PyQt5.QtWidgets import QMessageBox
from PyQt5.QtWidgets import QSizePolicy
from PyQt5.QtWidgets import QWidget

_CUR_DIR = os.path.dirname(os.path.realpath(sys.argv[0]))
_ICON = os.path.join(_CUR_DIR, 'icon.png')
_DATA_DIR = os.path.join(_CUR_DIR, 'Data')
_TILE_A = os.path.join(_DATA_DIR, 'TileA')
_TILE_B = os.path.join(_DATA_DIR, 'TileB')
_TILE_C = os.path.join(_DATA_DIR, 'TileC')
_SOBJ = os.path.join(_DATA_DIR, 'SObj')

_DIM = (24, 24)
_W, _H = 0, 0
_HEIGHT_PAD = 10
_TILE_B_OFFSET = 49151
_TILE_BYTES = 1728


class EPFViewer(QMainWindow):

    def __init__(self, app=None, ui=None):
        super().__init__()
        self.app = app
        self.ui = ui
        self._actions = list()

    def set_images(self, images, scroll_area, start_index=1, column_width=14):
        def update_dimensions(rows, columns, column_width):
            columns += 1
            if columns >= column_width > 0:
                rows += 1
                columns = 0

            return rows, columns

        rows, columns = 0, 0
        grid_layout = QGridLayout()
        grid_layout.setHorizontalSpacing(1)
        grid_layout.setVerticalSpacing(1)
        for i in range(len(images)):
            ql = QLabel()
            ql.setPixmap(QPixmap.fromImage(ImageQt(images[i])))
            ql.setScaledContents(True)
            ql.setSizePolicy(QSizePolicy.Fixed, QSizePolicy.Fixed)
            ql.setContextMenuPolicy(Qt.ActionsContextMenu)
            action = QAction('Export Tile {} to BMP'.format((i + start_index)), self)
            action.triggered.connect((lambda idx: lambda: self.export_tile(images[idx]))(i))
            self._actions.append(action)
            ql.addAction(action)
            grid_layout.addWidget(ql, rows, columns)

            rows, columns = update_dimensions(rows, columns, column_width)

        client = QWidget()
        scroll_area.setWidget(client)
        client.setLayout(grid_layout)

    def export(self, images, prefix='tile', dir_path=None, multi=True):
        if not dir_path:
            multi = False
            dir_path = str(QFileDialog.getExistingDirectory(self, "Select Directory"))

        if not dir_path:
            return

        pad_length = len(str(len(images)))
        for i in range(len(images)):
            images[i].save(os.path.join(dir_path, '_'.join(
                (prefix, str(i).zfill(pad_length))) + '.bmp'))
        if not multi:
            QMessageBox.about(self,
                              'TKViewer', 'Successfully exported tiles to: "{}"'.format(dir_path))

    def export_all(self, images, prefixes=('tileA', 'tileB', 'tileC', 'tileStatic')):
        dir_path = str(QFileDialog.getExistingDirectory(self, "Select Directory"))

        if not dir_path:
            return

        for i in range(len(images)):
            self.export(images[i], prefix=prefixes[i], dir_path=dir_path)

        QMessageBox.about(self,
                          'TKViewer', 'Successfully exported tiles to: "{}"'.format(dir_path))

    def export_tile(self, image):
        dir_path = QFileDialog.getSaveFileName(self,
                                               'Save Tile', os.path.join(os.path.expanduser('~'),
                                                                         'tile.bmp'))

        if not dir_path:
            return

        image.save(dir_path[0])
        QMessageBox.about(self, 'TKViewer', 'Successfully exported tile')

    def view_map(self, a_images, b_images, sobj_objects, sobj_images):
        dir_path = QFileDialog.getOpenFileName(self,
                                               'Map File', os.path.join(os.path.expanduser('~')))

        if not dir_path:
            return

        map_name = os.path.split(dir_path[0])[1]
        map_handler = MAPHandler(dir_path[0])
        map_width = map_handler.width
        map_height = map_handler.height
        tiles = map_handler.tiles

        black = Image.new('RGBA', _DIM, 'black')
        im = Image.new('RGBA', (map_width * _DIM[_W], map_height * _DIM[_H]), 'black')
        depth = 0
        length = 0

        for i in range(len(tiles)):
            if tiles[i]['ab_tile'] >= _TILE_B_OFFSET:
                tile = b_images[tiles[i]['ab_tile'] - _TILE_B_OFFSET]
            elif tiles[i]['ab_tile'] > 0:
                tile = a_images[tiles[i]['ab_tile']]
            else:
                tile = black

            im.paste(tile, (length * _DIM[_W], depth * _DIM[_H]))

            # If a Static Object is here, render upwards to height
            if tiles[i]['sobj_tile']:
                tile_index = tiles[i]['sobj_tile']
                sobj_image = sobj_images[tile_index]

                height = sobj_objects[tile_index]['height'] - 1
                im.paste(sobj_image, (length * _DIM[_W], (depth - height) * _DIM[_H]),
                         sobj_image)

            if (((i + 1) % map_width) == 0) and (i != 0):
                depth += 1
                length = 0
            else:
                length += 1

        im.show(map_name)

    def show_about(self):
        dialog = QMessageBox.information(self, 'TKViewer v{} ({})'.format(
            __version__,
            __version_codename__),
                                         'Github: https://github.com/DizzyThermal/TKViewer')

    def extract_dats(self):
        dir_path = QFileDialog.getOpenFileNames(
            self, 'Select Data File(s) to Extract', os.path.join(os.path.expanduser('~')),
            'Data File(s) (*.dat);;All Files (*.*)')

        if not dir_path:
            return

        output_dir = str(QFileDialog.getExistingDirectory(self,
                                                          'Select Extraction Output Directory'))

        if not output_dir:
            return

        int_read = array.array('I')

        total_files = 0
        for dat_file in dir_path[0]:
            dat_file_handler = open(dat_file, 'rb')

            # Read number of Files
            int_read.fromfile(dat_file_handler, 1)
            num_of_files = int_read.pop() - 1
            total_files += num_of_files

            # Next File Location
            for k in range(num_of_files):
                # Read File Data Location
                int_read.fromfile(dat_file_handler, 1)
                data_location = int_read.pop()

                # Read File Name
                name = dat_file_handler.read(13).split(b'\0', 1)[0].decode()
                next_file_location = dat_file_handler.tell()

                # Read File Size (minus offset)
                int_read.fromfile(dat_file_handler, 1)
                size = int_read.pop() - data_location

                # Create Inner File Handler
                fn = os.path.join(output_dir, name)
                inner_file_handler = open(fn, 'wb')

                dat_file_handler.seek(data_location)
                inner_file_handler.write(dat_file_handler.read(size))

                # Close Inner File Handler and Seek
                inner_file_handler.close()
                dat_file_handler.seek(next_file_location)

            dat_file_handler.close()

        QMessageBox.about(self, 'TKViewer',
                          'Successfully extracted {} DAT files to: "{}"'.format(len(dir_path[0]),
                                                                                output_dir))


def main(argv):
    a_tbl = TBLHandler('.'.join((_TILE_A, 'tbl')), old_format=True)
    a_pals = list()
    for i in range(a_tbl.palette_count):
        pal = PALHandler('.'.join((_TILE_A + '{}'.format(i), 'pal')))
        a_pals.append(pal.pals[0])
        pal.close()
    a_renderer = Renderer(
        epfs=EPFHandler('.'.join((_TILE_A, 'epf'))),
        pals=a_pals,
        tbl=a_tbl)
    a_images = [a_renderer.render_tile(x, dim=_DIM) for x in range(a_tbl.tile_count)]
    a_tbl.close()

    b_tbl = TBLHandler('.'.join((_TILE_B, 'tbl')), old_format=True)
    b_pals = list()
    for i in range(b_tbl.palette_count):
        pal = PALHandler('.'.join((_TILE_B + '{}'.format(i), 'pal')))
        b_pals.append(pal.pals[0])
        pal.close()
    b_renderer = Renderer(
        epfs=EPFHandler('.'.join((_TILE_B, 'epf'))),
        pals=b_pals,
        tbl=b_tbl)
    b_images = [b_renderer.render_tile(x, dim=_DIM) for x in range(b_tbl.tile_count)]
    b_tbl.close()

    c_tbl = TBLHandler('.'.join((_TILE_C, 'tbl')), old_format=True)
    sobj_tbl = SObjTBLHandler('.'.join((_SOBJ, 'tbl')), old_format=True)
    c_pals = list()
    for i in range(c_tbl.palette_count):
        pal = PALHandler('.'.join((_TILE_C + '{}'.format(i), 'pal')))
        c_pals.append(pal.pals[0])
        pal.close()
    c_renderer = Renderer(
        epfs=EPFHandler('.'.join((_TILE_C, 'epf'))),
        pals=c_pals,
        tbl=c_tbl,
        sobj_tbl=sobj_tbl)
    c_images = [c_renderer.render_tile(x, dim=_DIM) for x in range(c_tbl.tile_count)]
    sobj_images = [c_renderer.render_static_object(
        x, alpha_rgb=(0, 0, 255), background_color='blue', height_pad=10, dim=_DIM)
        for x in range(sobj_tbl.object_count)]
    sobj_images_raw = [c_renderer.render_static_object(
        x, alpha_rgb=(0, 0, 0, 0), background_color=None, dim=_DIM)
        for x in range(sobj_tbl.object_count)]
    c_tbl.close()
    sobj_tbl.close()

    app = QApplication(argv)
    ui = Ui_MainWindow()
    window = EPFViewer(app, ui)
    ui.setupUi(window)

    window.set_images(a_images, ui.a_tiles_scroll_area)
    window.set_images(b_images, ui.b_tiles_scroll_area, start_index=(_TILE_B_OFFSET + 1))
    window.set_images(c_images, ui.c_tiles_scroll_area)
    window.set_images(sobj_images, ui.sobj_tiles_scroll_area, column_width=-1)

    ui.actionA_Tiles.triggered.connect(lambda: window.export(a_images))
    ui.actionB_Tiles.triggered.connect(lambda: window.export(b_images))
    ui.actionC_Tiles.triggered.connect(lambda: window.export(c_images))
    ui.actionStatic_Tiles.triggered.connect(lambda: window.export(sobj_images))
    ui.actionExport_All.triggered.connect(lambda: window.export_all(
        [a_images, b_images, c_images, sobj_images]))
    ui.actionOpen_Map.triggered.connect(lambda: window.view_map(a_images,
                                                                b_images,
                                                                sobj_tbl.objects,
                                                                sobj_images_raw))
    ui.actionAbout.triggered.connect(lambda: window.show_about())
    ui.actionData_Files.triggered.connect(lambda: window.extract_dats())

    signal.signal(signal.SIGINT, signal.SIG_DFL)

    app.setWindowIcon(QIcon(_ICON))

    window.show()
    sys.exit(app.exec_())


if __name__ == '__main__':
    main(sys.argv)
