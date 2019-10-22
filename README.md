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

Run TKViewer with:

```bash
java -jar target/TKViewer-*.jar
```


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
  short frame_offset                   (2 bytes)                         # offset from frame_index in chunk
  short duration                       (2 bytes)                         # amount of time to play the frame
  short unknownId1                     (2 bytes)                         # normally -1, only has real values
                                                                           in death animations for 2 mobs
  byte transparency                    (1 bytes)                         # transparency
  byte unknownId2                      (1 byte)                          # unknown id/flag (5)
  byte unknownId3                      (1 byte)                          # unknown id/flag (5)
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
int effect_count                       (4 bytes)                         # number of effects in FRM
int[effect_count] palette_index        (effect_count * 4 bytes)          # list of palette indicies for effects
```

#### MAP

```cpp
short width                            (2 bytes)                         # width of the map (in tiles)
short height                           (2 bytes)                         # height of the map (in tiles)
tile[width*height] tiles               (width * height * 4 bytes)        # list of tile structures

typedef struct {
  short ab_tile_id                     (2 bytes)                         # ground tile frame index (Tile/Tbl)
  short sobj_tile_id                   (2 bytes)                         # static object index (TileC/SObjTbl)
} tile                                 (4 bytes)
```
#### PAL (Single)

```cpp
byte[9] header                        (9 bytes) # DLPalette              # DLPalette (literal)
byte[15] unknown                      (15 bytes)                         # unknown bytes (1)
byte animation_color_count            (1 byte)                           # number of animation colors
byte[7] unknown2                      (7 bytes)                          # unknown bytes (2)
short[animation_color_count]          (animation_color_count * 2 bytes)  # list of animation colors (short)
color[256] palette                    (1024 bytes)                       # list of color structures

typedef struct {
  byte red                            (1 byte)                           # red value for color
  byte green                          (1 byte)                           # green value for color
  byte red                            (1 byte)                           # red value for color
  byte alpha                          (1 byte)                           # alpha value for color
} color                               (4 bytes)
```

#### PAL (Packed)

```cpp
int palette_count                                                        # number of palettes in file
PAL[palette_count] palettes                                              # list of PAL structures

typedef struct {
  byte[9] header                     (9 bytes)                           # DLPalette (literal)
  byte[15] unknown                   (15 bytes)                          # unknown bytes (1)
  byte animation_color_count         (1 byte)                            # number of animation colors
  byte[7] unknown2                   (7 bytes)                           # unknown bytes (2)
  short[animation_color_count]       (animation_color_count * 2 bytes)   # list of animation colors (short)
  color[256] palette                 (1024 bytes)                        # list of color structures

  typedef struct {
    byte blue                        (1 byte)                            # blue value for color
    byte green                       (1 byte)                            # green value for color
    byte red                         (1 byte)                            # red value for color
    byte alpha                       (1 byte)                            # alpha value for color
  } color                            (4 bytes)
} PAL
```

#### TBL (Effects)
```cpp
int effect count                     (4 bytes)                           # number of effects in TBL

effect [effect_count] effects                                            # list of effect structures
typedef struct {
  int effect_index                   (4 bytes)                           # effect index
  int frame count                    (4 bytes)                           # number of sequential frames after effect_index
  byte[20] unknown                                                       # unknown bytes (1)
  frame [frame_count] frames                                             # list of frame structures
  typedef struct {
	int frame index                 (4 bytes)                            # start frame index for effect
	int frame delay                 (4 bytes)                            # delay until next frame (milliseconds)
	int pallete number              (4 bytes)                            # palette index to use when renderer
	byte[4] unknown                 (4 bytes)                            # unknown bytes (1)
  }
} effect
```

#### TBL (Static Objects)
```cpp
int object_count                    (4 bytes)                            # number of objects in SObj TBL
short unknown                       (2 bytes)                            # unknown short
object[object_count]                (obj_count * obj_size bytes)         # list of object structures

typedef struct {
  byte movement_directions          (1 byte)                             # movement directions for static object (see list below)
  byte tile_count                   (1 byte)                             # number of tiles in static object
  short[tile_count]                 (tile_count * 2 bytes)               # list of tile indicies for static object
} object
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
int tile_count                       (4 bytes)                           # number of tiles in TBL
tile[tile_count] tiles               (tile_count * 2 bytes)              # list of tile structures

typedef struct {
  short palette_index                (1 byte)                            # palette index for tile (masked) 
} tile                               (2 bytes)
```

## Contributors

Huge thank you to everyone who helps figure out NTK file structures:

  * DDeokk
  * herbert3000
  * rbcastner
  * wattostudios
