package com.gamemode.tkviewer.resources;

import java.io.File;

public class Resources {
    public static final int TILE_DIM = 48;
    // Count of required Map resource files:
    //   tile*.epf    | tile.pal  | tile.tbl
    //   tilec*.epf   | TileC.pal | TILEC.tbl | SObj.tbl
    public static final int REQUIRED_MAP_FILES = 56;

    // Count of required Body resource files:
    //   Body*.epf | Body.pal | Body.dsc
    public static final int REQUIRED_BODY_FILES = 17;

    // Count of required Bow resource files:
    //   Bow*.epf | Bow.pal | Bow.dsc
    public static final int REQUIRED_BOW_FILES = 4;

    // Count of required Coat resource files:
    //   Coat*.epf | Coat.pal | Coat.dsc
    public static final int REQUIRED_COAT_FILES = 16;

    // Count of required Face resource files:
    //   Face*.epf | Face.pal | Face.dsc
    public static final int REQUIRED_FACE_FILES = 3;

    // Count of required Fan resource files:
    //   Fan*.epf | Fan.pal | Fan.dsc
    public static final int REQUIRED_FAN_FILES = 3;

    // Count of required Hair resource files:
    //   Hair*.epf | Hair.pal | Hair.dsc
    public static final int REQUIRED_HAIR_FILES = 6;

    // Count of required Helmet resource files:
    //   Helmet*.epf | Helmet.pal | Helmet.dsc
    public static final int REQUIRED_HELMET_FILES = 6;

    // Count of required Mantle resource files:
    //   Mantle*.epf | Mantle.pal | Mantle.dsc
    public static final int REQUIRED_MANTLE_FILES = 6;

    // Count of required Shield resource files:
    //   Spear*.epf | Spear.pal | Spear.dsc
    public static final int REQUIRED_SPEAR_FILES = 4;

    // Count of required Shield resource files:
    //   Shield*.epf | Shield.pal | Shield.dsc
    public static final int REQUIRED_SHIELD_FILES = 3;

    // Count of required Shoes resource files:
    //   Shoes*.epf | Shoes.pal | Shoes.dsc
    public static final int REQUIRED_SHOES_FILES = 3;

    // Count of required Sword resource files:
    //   Sword*.epf | Sword.pal | Sword.dsc
    public static final int REQUIRED_SWORD_FILES = 6;

    public static final String PROGRAM_FILES = "C:\\Program Files";
    public static final String PROGRAM_FILES_X86 = "C:\\Program Files (x86)";
    public static final String PATH_PREFIX = (System.getProperty("os.arch").contains("64"))?PROGRAM_FILES_X86:PROGRAM_FILES;

    public static final String NEXUSTK_DATA_DIRECTORY = PROGRAM_FILES_X86 + File.separator + "KRU\\NexusTK\\Data";
    public static final String DATA_DIRECTORY = System.getProperty("java.io.tmpdir") + File.separator + "TKViewer\\NexusTK-Data";

    public static final String CLIENT_ICON = "client_icon.png";

    public static enum GUI_LOADING_FUNCTION {
        BODIES, BOWS, COATS, FACES, FANS, HAIR, HELMETS, MANTLES, MAPS, SPEARS, SHIELDS, SHOES, SWORDS
    }
}
