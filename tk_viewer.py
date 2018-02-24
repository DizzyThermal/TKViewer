#!/usr/bin/env python3

import glob
import os
import tkinter

from tkinter import Menu
from tkinter import filedialog
from tkinter import messagebox

from file_reader import DATHandler
from file_reader import EPFHandler
from file_reader import PALHandler
from file_reader import SObjTBLHandler
from file_reader import TBLHandler
from renderer import MapRenderer
from renderer import Renderer
from resources import config


class TKViewer(tkinter.Frame):
    def __init__(self, parent, tile_renderer, sobj_renderer):
        tkinter.Frame.__init__(self, parent)
        self.parent = parent
        self.parent.title('TKViewer')
        self.tile_renderer = tile_renderer
        self.sobj_renderer = sobj_renderer
        self.map_renderer = MapRenderer(tile_renderer=self.tile_renderer, sobj_renderer=self.sobj_renderer)
        self.init_ui()

    def init_ui(self):
        menu_bar = Menu(self.parent, tearoff=False)
        self.parent.config(menu=menu_bar)

        file_menu = Menu(menu_bar, tearoff=False)

        open_menu = Menu(file_menu, tearoff=False)
        open_menu.add_command(label="Map File (*.cmp | *.map)", command=self.open_map)

        export_menu = Menu(file_menu, tearoff=False)
        export_menu.add_command(label="Tiles to Bitmaps", command=self.export_tiles)
        export_menu.add_command(label="Static Objects to Bitmaps", command=self.export_sobjs)

        extract_menu = Menu(file_menu, tearoff=False)
        extract_menu.add_command(label="Data Files (*.dat)", command=self.extract_dats)

        file_menu.add_cascade(label="Open", menu=open_menu)
        file_menu.add_cascade(label="Export", menu=export_menu)
        file_menu.add_cascade(label="Extract", menu=extract_menu)
        menu_bar.add_cascade(label="File", menu=file_menu)

    def open_map(self):
        file_path = filedialog.Open(self, filetypes=[('NexusTK Maps', 'cmp map')]).show()

        if os.path.exists(file_path):
            self.map_renderer.render_map_from_file(file_path).show()

    def export_tiles(self):
        if not os.path.exists(config['tile_export_dir']):
            os.makedirs(config['tile_export_dir'])

        export_dir = filedialog.askdirectory(initialdir=config['tile_export_dir'])

        if os.path.exists(export_dir):
            tile_count = self.tile_renderer.tbl.tile_count
            if tile_count > 1000:
                if not messagebox.askokcancel('TKViewer',
                                              'There are {} tiles to extract, this may take a very '
                                              'long time to complete, are you sure want to '
                                              'continue?'.format(tile_count)):
                    return

            for i in range(tile_count):
                self.tile_renderer.render_tile(i).save(
                    os.path.join(export_dir, 'tile-{0:05d}.bmp'.format(i)))

            messagebox.showinfo('TKViewer',
                                '{} tiles were successfully exported.'.format(tile_count))


    def export_sobjs(self):
        if not os.path.exists(config['sobj_export_dir']):
            os.makedirs(config['sobj_export_dir'])

        export_dir = filedialog.askdirectory(initialdir=config['sobj_export_dir'])

        if os.path.exists(export_dir):
            sobj_count = self.sobj_renderer.sobj_tbl.object_count
            if sobj_count > 1000:
                if not messagebox.askokcancel('TKViewer',
                                              'There are {} static objects to extract, this may '
                                              'take a very long time to complete, are you sure '
                                              'want to continue?'.format(sobj_count)):
                    return

            for i in range(self.sobj_renderer.sobj_tbl.object_count):
                self.sobj_renderer.render_static_object(i).save(
                    os.path.join(export_dir, 'sobj-{0:05d}.bmp'.format(i)))

            messagebox.showinfo('TKViewer',
                                '{} static objects were successfully exported.'.format(sobj_count))

    def extract_dats(self):
        initialdir = None
        if os.path.exists(config['nexus_data_dir']):
            initialdir = config['nexus_data_dir']

        dat_files = filedialog.askopenfilenames(parent=self,
                                                title='Select Data File(s) to Extract',
                                                initialdir=initialdir)

        if not os.path.exists(config['data_dir']):
            os.makedirs(config['data_dir'])

        extract_dir = filedialog.askdirectory(initialdir=config['data_dir'])

        total_files = 0
        if os.path.exists(extract_dir):
            for i in range(len(dat_files)):
                dat = DATHandler(dat_files[i])
                dat.export_files(extract_dir=extract_dir)
                total_files += dat.file_count

        messagebox.showinfo('TKViewer',
                            '{} data files were successfully extracted.'.format(total_files))


def main():
    tile_pal = PALHandler(os.path.join(config['data_dir'], 'tile.pal'))
    tile_tbl = TBLHandler(os.path.join(config['data_dir'], 'tile.tbl'))
    tilec_pal = PALHandler(os.path.join(config['data_dir'], 'TileC.pal'))
    tilec_tbl = TBLHandler(os.path.join(config['data_dir'], 'TILEC.TBL'))
    sobj_tbl = SObjTBLHandler(os.path.join(config['data_dir'], 'SObj.tbl'))
    tile_epf_files = glob.glob(os.path.join(config['data_dir'], 'tile*.epf'))
    tilec_epf_files = glob.glob(os.path.join(config['data_dir'], 'tilec*.epf'))

    tile_epfs = []
    for i in range(len(tile_epf_files)):
        if 'tilec' not in tile_epf_files[i]:
            epf = EPFHandler(os.path.join(config['data_dir'], 'tile{}.epf'.format(i)))
            tile_epfs.append(epf)

    tilec_epfs = []
    for i in range(len(tilec_epf_files)):
        epf = EPFHandler(os.path.join(config['data_dir'], 'tilec{}.epf'.format(i)))
        tilec_epfs.append(epf)

    tile_renderer = Renderer(epfs=tile_epfs, pals=tile_pal.pals, tbl=tile_tbl)
    sobj_renderer = Renderer(epfs=tilec_epfs, pals=tilec_pal.pals, tbl=tilec_tbl, sobj_tbl=sobj_tbl)

    root = tkinter.Tk()
    ex = TKViewer(root, tile_renderer=tile_renderer, sobj_renderer=sobj_renderer)
    root.geometry("640x480")
    root.iconbitmap(config['viewer_icon'])
    root.mainloop()


if __name__ == '__main__':
    main()
