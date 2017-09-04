#!/usr/bin/env python

__author__ = 'DizzyThermal'
__email__ = 'DizzyThermal@gmail.com'
__license__ = 'GNU GPLv3'

import binascii
import io
import os
import sys

from epf_gui import Ui_MainWindow
from file_reader import EPFHandler

from PIL import Image
from PIL.ImageQt import ImageQt

from PyQt5.QtCore import Qt
from PyQt5.QtGui import QImage
from PyQt5.QtGui import QPixmap
from PyQt5.QtWidgets import QAction
from PyQt5.QtWidgets import QApplication
from PyQt5.QtWidgets import QFileDialog
from PyQt5.QtWidgets import QGridLayout
from PyQt5.QtWidgets import QLabel
from PyQt5.QtWidgets import QMainWindow
from PyQt5.QtWidgets import QMenu
from PyQt5.QtWidgets import QMessageBox
from PyQt5.QtWidgets import QProgressDialog
from PyQt5.QtWidgets import QSizePolicy
from PyQt5.QtWidgets import QWidget

_CUR_DIR = os.path.dirname(os.path.realpath(sys.argv[0]))
_TILE_CACHE = os.path.join(_CUR_DIR, 'Data/_tile_cache')
_TILE_A = os.path.join(_CUR_DIR, 'Data/TileA.epf')
_TILE_B = os.path.join(_CUR_DIR, 'Data/TileB.epf')
_TILE_C = os.path.join(_CUR_DIR, 'Data/TileC.epf')
_TILE_BYTES = 1728


class EPFViewer(QMainWindow):
    _COL_WIDTH = 14
    _TILE_WIDTH = 24
    _TILE_HEIGHT = 24

    def __init__(self, app=None, ui=None):
        super().__init__()
        self.app = app
        self.ui = ui

    def set_images(self, images, scroll_area):
        row = 0
        col = 0
        grid_layout = QGridLayout()
        grid_layout.setHorizontalSpacing(1)
        grid_layout.setVerticalSpacing(1)
        self._actions = []
        for i in range(len(images)):
            qim = ImageQt(images[i])
            pixmap = QPixmap.fromImage(qim)
            ql = QLabel()
            ql.setPixmap(pixmap)
            ql.setScaledContents(True)
            ql.setSizePolicy(QSizePolicy.Fixed, QSizePolicy.Fixed)
            ql.setContextMenuPolicy(Qt.ActionsContextMenu)
            action = QAction('Export Tile #{} to BMP'.format(i), self)
            action.triggered.connect((lambda idx: lambda: self.export_tile(images[idx]))(i))
            self._actions.append(action)
            ql.addAction(action)
            grid_layout.addWidget(ql, row, col)

            col += 1
            if col >= self._COL_WIDTH:
                row += 1
                col = 0

        client = QWidget()
        scroll_area.setWidget(client)
        client.setLayout(grid_layout)
        
    def export(self, images, prefix='tile', dir_path=None, multi=True):
        if not dir_path:
            multi=False
            dir_path = str(QFileDialog.getExistingDirectory(self, "Select Directory"))

        if not dir_path:
            return

        pad_length = len(str(len(images)))
        for i in range(len(images)):
            images[i].save(os.path.join(dir_path, '_'.join(
                (prefix, str(i).zfill(pad_length))) + '.bmp'))
        if not multi:
            QMessageBox.about(self, 'EPFViewer', 'Successfully exported tiles to: "{}"'.format(dir_path))

    def export_all(self, images, prefixes=['tileA', 'tileB', 'tileC']):
        dir_path = str(QFileDialog.getExistingDirectory(self, "Select Directory"))

        if not dir_path:
            return

        for i in range(len(images)):
            self.export(images[i], prefix=prefixes[i], dir_path=dir_path)

        QMessageBox.about(self, 'EPFViewer', 'Successfully exported tiles to: "{}"'.format(dir_path))

    def export_tile(self, image):
        dir_path = QFileDialog.getSaveFileName(self,
                'Save Tile', os.path.join(os.path.expanduser('~'),
                    'tile.bmp'))
    
        if not dir_path:
            return

        image.save(dir_path[0])
        QMessageBox.about(self, 'EPFViewer', 'Successfully exported tile')


def main(argv):
    epf = EPFHandler(_TILE_A)
    a_images = epf.get_tiles()
    epf.close()

    epf = EPFHandler(_TILE_B)
    b_images = epf.get_tiles()
    epf.close()

    epf = EPFHandler(_TILE_C)
    c_images = epf.get_tiles()
    epf.close()

    app = QApplication(argv)
    ui = Ui_MainWindow()
    window = EPFViewer(app, ui)
    ui.setupUi(window)

    window.set_images(a_images, ui.a_tiles_scroll_area)
    window.set_images(b_images, ui.b_tiles_scroll_area)
    window.set_images(c_images, ui.c_tiles_scroll_area)

    ui.actionA_Tiles.triggered.connect(lambda: window.export(a_images))
    ui.actionB_Tiles.triggered.connect(lambda: window.export(b_images))
    ui.actionC_Tiles.triggered.connect(lambda: window.export(c_images))
    ui.actionExport_All.triggered.connect(lambda: window.export_all([a_images, b_images, c_images]))

    window.show()
    sys.exit(app.exec_())

    epf.close()

if __name__ == '__main__':
    main(sys.argv)
