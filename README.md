# TKViewer

NexusTK Resource Viewer for EPF and related files.

## Quickstart

This script will take a bit to load up, give it some time.

**Dependencies**:
* Python 3
* Python pip3 dependencies:

```bash
pip install pillow
```

**Running the scripts**:

```bash
python3 tk_viewer.py
```

**Experimental**

```bash
python3 tk_client.py
```

## FileReader Module

This module contains readers for the following files:
- **File Structures**
  * [DAT](#dat)
  * [DSC](#dsc)
  * [EPF](#epf)
  * [MAP](#map)
  * [PAL (Single)](#pal-single)
  * [PAL (Packed)](#pal-packed)
  * [TBL (Tiles - Modern)](#tbl-tiles---modern)
  * [TBL (Tiles - Legacy)](#tbl-tiles---legacy)
  * [TBL (Static Objects)](#tbl-static-objects)

### File Structures

#### DAT

```cpp
int file_count                         (2 bytes)
file[file_count] files                 ((file_count * file_size) bytes)

typedef struct {
  int data_location                    (4 bytes)
  byte[13] file_name                   (4 bytes)
  int size                             (4 bytes)
  byte[size] file_data                 (size bytes)
} file                                 (12 + size bytes)
```

#### DSC
```cpp
byte[15] header                        (15 bytes) # PartDescription
byte[7] null                           (7 bytes)
byte unknown1                          (1 byte)
int part_count                         (4 bytes)
part[part_count] parts                 (27 + (part_count * part_size) bytes)

typedef struct {
  int id                               (4 bytes)
  int palette_id                       (4 bytes)
  int frame_index                      (4 bytes)
  int frame_count                      (4 bytes)
  byte unknown2                        (1 byte)
  int unknown3                         (4 bytes)
  byte unknown4                        (1 byte)
  int unknown5                         (4 bytes)
  int unknown6                         (4 bytes)
  int chunk_count                      (4 bytes)
  chunk[chunk_count] chunks            (34 + (chunk_count * chunk_size) bytes)
} part

typedef struct {
  int id                               (4 bytes)
  int unknown                          (4 bytes)
  int block_count                      (4 bytes)
  block[block_count] blocks            (16 + (block_count * block_size) bytes)
} chunk

typedef struct {
  byte id                              (1 byte)
  int null                             (4 bytes)
  int unknown                          (4 bytes)
} block                                (9 bytes)
```

#### EPF

```cpp
short frame_count                      (2 bytes)
short height                           (2 bytes)
short width                            (2 bytes)
short unknown                          (2 bytes)
int pixel_data_length                  (4 bytes)
byte[pixel_data_length] pixel_data     (pixel_data_length bytes)
frame_entry[frame_count] frame_entries (frame_count * 16 bytes)

typedef struct {
  short pad_top                        (2 bytes)
  short pad_left                       (2 bytes)
  short height                         (2 bytes)
  short width                          (2 bytes)
  int pixel_data_offset                (4 bytes)
  int stencil_data_offset              (4 bytes)
} frame_entry                          (16 bytes)
```

#### MAP

```cpp
short width                            (2 bytes)
short height                           (2 bytes)
tile[width*height] tiles               (width * height * 4 bytes)

typedef struct {
  short ab_tile_id                     (2 bytes)
  short sobj_tile_id                   (2 bytes)
} tile                                 (4 bytes)
```

#### PAL (Single)

```cpp
byte[9] header                        (9 bytes) # DLPalette
byte[15] unknown                      (15 bytes)
byte animation_color_count            (1 byte)
byte[7] unknown2                      (7 bytes)
short[animation_color_count]          (animation_color_count * 2 bytes)
color[256] palette                    (1024 bytes)

typedef struct {
  byte blue                           (1 byte)
  byte green                          (1 byte)
  byte red                            (1 byte)
  byte alpha                          (1 byte)
} color                               (4 bytes)
```

#### PAL (Packed)

```cpp
int palette_count
PAL[palette_count] palettes

typedef struct {
  byte[9] header                     (9 bytes) # DLPalette
  byte[15] unknown                   (15 bytes)
  byte animation_color_count         (1 byte)
  byte[7] unknown2                   (7 bytes)
  short[animation_color_count]       (animation_color_count * 2 bytes)
  color[256] palette                 (1024 bytes)

  typedef struct {
    byte blue                        (1 byte)
    byte green                       (1 byte)
    byte red                         (1 byte)
    byte alpha                       (1 byte)
  } color                            (4 bytes)
} PAL
```

#### TBL (Static Objects)
```cpp
int obj_count                        (4 bytes)
short unknown                        (2 bytes)
obj[obj_count]                       (obj_count * ((tile_count * 2) + 2) bytes)

typedef struct {
  byte movement_directions           (1 byte)
  byte tile_count                    (1 byte)
  short[tile_count]                  (tile_count * 2 bytes)
} obj
```

**Note**: Movement Directions appear to have 6 states:
* 0x00 (Empty)
* 0x01 (Bottom)
* 0x02 (Top)
* 0x04 (Left)
* 0x08 (Right)
* 0x0F (Full)

#### TBL (Tiles)
```cpp
int tile_count                       (4 bytes)
tile[tile_count] tiles               (tile_count * 2 bytes)

typedef struct {
  byte palette_index                 (1 byte)
  byte unknown                       (1 byte)
} tile                               (2 bytes)
```

## Contributors

Huge thank you to everyone who helps figure out NTK file structures:

  * DDeokk
  * herbert3000
  * wattostudios
