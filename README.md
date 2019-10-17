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

* Note: The tile structures are compressed with zlib in the CMP format - it must be inflated!

```cpp
short width                            (2 bytes)                         # width of the map (in tiles)
short height                           (2 bytes)                         # height of the map (in tiles)
tile[width * height] tiles             (width * height * 4 bytes)        # list of tile structures (zlib compressed)

typedef struct {
  short abTileId                       (2 bytes)                         # ground tile frame index (Tile/Tbl)
  short passableTile                   (2 bytes)                         # passable tile flag
  short sObjTileId                     (2 bytes)                         # static object index (TileC/SObjTbl)
} tile                                 (6 bytes)
```

#### DAT

```cpp
int file_count                         (4 bytes)                         # file count + 1 (decrement for file count)
file[file_count] files                 ((file_count * file_size) bytes)  # list of file structures
byte[] file_data                                                         # binary data of all files (head-to-tail)

typedef struct {
  int data_location                    (4 bytes)                         # index of 'file_data' for the start of this file
  byte[13] file_name                   (13 bytes)                        # the file name (UTF-8 padded - 13 bytes) 
} file                                 (17 + size bytes)
```

#### DNA

```cpp
int mob_count                          (4 bytes)                         # number of mobs in file
mob[mob_count] mobs                    (mob_count * mob_size bytes)      # list of mob structures

typedef struct {
  int frame_index                      (4 bytes)                         # frame index of mob
  byte chunk_count                     (1 byte)                          # number of chunks in mob
  byte unknown1                        (1 byte)                          # unknown id/flag (1)
  short palette_index                  (2 bytes)                         # palette index of mob
  chunk[chunk_count] chunks            (chunk_count * chunk_size bytes)  # list of chunk structures
} mob                                  (8 + (chunk_count * chunk_size) bytes)

typedef struct {
  short block_count                    (2 bytes)                         # block count of chunk
  block[block_count] blocks            (block_count * block_size bytes)  # list of block structures
} chunk                                (2 + (block_count * block_size) bytes)

typedef struct {
  short unknownId1                     (2 bytes)                         # unknown id/flag (1)
  short unknownId2                     (2 bytes)                         # unknown id/flag (2)
  short unknownId3                     (2 bytes)                         # unknown id/flag (3)
  short unknownId4                     (2 bytes)                         # unknown id/flag (4)
  byte unknownId5                      (1 byte)                          # unknown id/flag (5)
} block                                (9 bytes)
```

#### DSC

```cpp
byte[15] header                        (15 bytes)                        # PartDescription (literal)
byte[7] null                           (7 bytes)                         # unknown null bytes (1)
byte unknown1                          (1 byte)                          # unknown id/flag (2)
int part_count                         (4 bytes)                         # number of parts
part[part_count] parts                 (part_count * part_size bytes)    # list of part structures

typedef struct {
  int id                               (4 bytes)                         # id of the part
  int palette_id                       (4 bytes)                         # palette index of the part
  int frame_index                      (4 bytes)                         # first frame index of the part
  int frame_count                      (4 bytes)                         # number of sequential frames after frame_index
  byte unknown2                        (1 byte)                          # unknown id/flag (3)
  int unknown3                         (4 bytes)                         # unknown id/flag (4)
  byte unknown4                        (1 byte)                          # unknown id/flag (5)
  int unknown5                         (4 bytes)                         # unknown id/flag (6)
  int unknown6                         (4 bytes)                         # unknown id/flag (7)
  int chunk_count                      (4 bytes)                         # number of chunks
  chunk[chunk_count] chunks            (chunk_count * chunk_size bytes)  # list of chunk structures
} part                                 (34 + (chunk_count * chunk_size) bytes)

typedef struct {
  int id                               (4 bytes)                         # id of the chunk
  int unknown                          (4 bytes)                         # unknown id/flag (1)
  int block_count                      (4 bytes)                         # number of blocks
  block[block_count] blocks            (block_count * block_size bytes)  # list of block structures
} chunk                                (12 + (block_count * block_size) bytes)

typedef struct {
  byte id                              (1 byte)                          # id of the block
  int null                             (4 bytes)                         # unknown id/flag (1)
  int unknown                          (4 bytes)                         # unknown id/flag (2)
} block                                (9 bytes)
```

#### EPF

```cpp
short frame_count                      (2 bytes)                         # number of frames in the EPF
short height                           (2 bytes)                         # height of the EPF (?)
short width                            (2 bytes)                         # width of the EPF (?)
short unknown                          (2 bytes)                         # unknown id/flag (1)
int pixel_data_length                  (4 bytes)                         # length of the pixel data
byte[pixel_data_length] pixel_data     (pixel_data_length bytes)         # list of pixel data bytes
frame[frame_count] frames              (frame_count * frame_size bytes)  # list of frame structures

typedef struct {
  short top                            (2 bytes)                         # top offset of the frame (in pixels)
  short left                           (2 bytes)                         # left offset of the frame (in pixels)
  short bottom                         (2 bytes)                         # bottom offset of the frame (in pixels)
  short right                          (2 bytes)                         # right offset of the frame (in pixels)
  int pixel_data_offset                (4 bytes)                         # index of 'pixel_data' for the start of this frame's pixel data
  int stencil_data_offset              (4 bytes)                         # index of 'pixel_data' for the start of this frame's stencil data
} frame                                (16 bytes)
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

#### TBL (Effects)
```cpp
int effect count                     (4 bytes)

effect [effect_count] effects
typedef struct {
  int effect index                  (4 bytes)
  int frame count                    (4 bytes) # number of frames in the effect
  byte[20] unknown
  frame [frame_count] frames
  typedef struct {
	int frame index                 (4 bytes)
	int frame delay                 (4 bytes) # ms
	int pallete number              (4 bytes)
	byte[4] unknown
  }
} effect
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
