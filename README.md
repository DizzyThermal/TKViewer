# TKViewer

NexusTK Resource Viewer for EPF and related files.

## Quickstart

IntelliJ is recommended for development, but you should be able to build with
just Maven: (mvn clean; mvn package)

```bash
mvn clean
mvn package
```

* Output is stored in `target/`

## FileReader Classes

- **File Structures**
  * [CMP](#cmp)
  * [DAT](#dat)
  * [DNA](#dna)
  * [DSC](#dsc)
  * [EPF](#epf)
  * [FRM](#frm)
  * [MAP](#map)
  * [PAL (Single)](#pal-single)
  * [PAL (Packed)](#pal-packed)
  * [TBL (Tiles)](#tbl-tiles)
  * [TBL (Static Objects)](#tbl-static-objects)

### File Structures

#### CMP

* Note: This format is compressed with zlib, it must be inflated!

```cpp
short width                            (2 bytes)
short height                           (2 bytes)
tile[width*height] tiles               (width * height * 4 bytes)

typedef struct {
  short abTileId                       (2 bytes)
  short passableTile                   (2 bytes)
  short sObjTileId                     (2 bytes)
} tile                                 (6 bytes)
```

#### DAT

```cpp
int file_count                         (4 bytes) # File Count + 1
file[file_count] files                 ((file_count * file_size) bytes)
byte[] file_data

typedef struct {
  int data_location                    (4 bytes)
  byte[13] file_name                   (4 bytes)
  int size                             (4 bytes)
} file                                 (12 + size bytes)
```

#### DNA

```cpp
int mob_count                          (4 bytes)
mob[mob_count] mobs                    ((mob_count * 8) bytes)

typedef struct {
  int frame_index                      (4 bytes)
  byte chunk_count                     (1 byte)
  byte unknown1                        (1 byte)
  short palette_index                  (2 bytes)
} mob                                  (8 bytes)
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

#### FRM

```cpp
int effect_count                       (4 bytes)
int[effect_count] palette_index        (effect_count * 4 bytes)
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
  byte alpha                          (1 byte)
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
    byte alpha                       (1 byte)
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
