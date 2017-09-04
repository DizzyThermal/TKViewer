# EPFViewer

## Quickstart

This script will take a while to initially load up, give it some time.

* Python 3 (PyQt5 is Py3)
* Python pip3 dependencies:

```bash
pip3 install pyqt5
pip3 install pillow
pip3 install struct

```

* Running the script

```bash
python3 epf_viewer.py
```

NexusTK EPF Viewer for viewing and exporting EPF/TBL/PAL files.

## file_reader module

This module contains readers for EPF, TBL, PAL, and MAP files.

### File Structures

#### TBL File Structure

```cpp
int tile_count                     (4 bytes)
int palette_count                  (4 bytes)
byte[3] unknown                    (3 bytes)
short[tile_count] palette_indicies (2 * tile_count bytes)
```

#### PAL File Structure

```cpp
byte[32] header    (32 bytes)
color[256] palette (1024 bytes)

typedef struct {
  byte blue        (1 byte)
  byte green       (1 byte)
  byte red         (1 byte)
  byte padding     (1 byte)
} color            (4 bytes)
```

#### EPF File Structure

```cpp
short tile_count                    (2 bytes)
short width                         (2 bytes)
short height                        (2 bytes)
short unknown                       (2 bytes)
int pixel_data_length               (4 bytes)
byte[pixel_data_length] pixel_data  (pixel_data_length bytes)
tile_entry[tile_count] tile_entries (tile_count * 16 bytes)

typedef struct {
  int unknown                       (4 bytes)
  short width                       (2 bytes)
  short height                      (2 bytes)
  int pixel_data_offset             (4 bytes)
  int unknown_offset                (4 bytes)
} table_entry                       (16 bytes)
```

#### MAP File Structure

```cpp
short width              (2 bytes)
short height             (2 bytes)
tile[width*height] tiles (width * height * 4 bytes)

typedef struct {
  short tile_id          (2 bytes)
  short unknown          (2 bytes)
} tile                   (4 bytes)
```

## epf_viewer GUI (PyQt5)

Features:

Since this exports NexusTK EPF/PAL/TBL files, the EPFViewer is coded to
reference TileA, TileB, TileC format - this can be modified for custom projects.

* Export A Tiles to Bitmaps
* Export B Tiles to Bitmaps
* Export C Tiles to Bitmaps

* Export individual Tiles to Bitmap (Right-Click)
