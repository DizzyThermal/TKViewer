package com.gamemode.tkviewer.utilities;

import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.file_handlers.DscFileHandler;
import com.gamemode.tkviewer.file_handlers.EfxTblFileHandler;
import com.gamemode.tkviewer.file_handlers.FrmFileHandler;
import com.gamemode.tkviewer.file_handlers.PalFileHandler;
import com.gamemode.tkviewer.file_handlers.SObjTblFileHandler;
import com.gamemode.tkviewer.file_handlers.TileTblFileHandler;
import com.gamemode.tkviewer.render.EffectRenderer;
import com.gamemode.tkviewer.render.MapRenderer;
import com.gamemode.tkviewer.render.MobRenderer;
import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.render.SObjRenderer;
import com.gamemode.tkviewer.render.TileRenderer;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.PivotData;
import com.gamemode.tkviewer.resources.Resources;

import java.io.File;
import java.util.List;

public class RenderUtils {
    public static DatFileHandler CHAR_DAT = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "char.dat");

    /**
     * Private constructor to prevent instantiation of static utility class
     */
    private RenderUtils() {}

    public static PivotData getPivotData(List<Frame> frames) {
        // Determine Canvas Size
        int left, top, right, bottom;
        left = top = 10000;
        right = bottom = -10000;
        for (Frame frame : frames) {
            if (frame == null) {
                continue;
            }

            if (frame.getLeft() < left) {
                left = frame.getLeft();
            }
            if (frame.getTop() < top) {
                top = frame.getTop();
            }
            if (frame.getRight() > right) {
                right = frame.getRight();
            }
            if (frame.getBottom() > bottom) {
                bottom = frame.getBottom();
            }
        }

        int width = right-left;
        int height = bottom-top;

        return new PivotData(Math.abs(left), Math.abs(top), width, height);
    }

    public static PartRenderer createBodyRenderer () { return new PartRenderer("Body"); }
    public static PartRenderer createBowRenderer () {
        return new PartRenderer("Bow");
    }
    public static PartRenderer createCoatRenderer () {
        return new PartRenderer("Coat");
    }
    public static EffectRenderer createEffectRenderer () {
        return new EffectRenderer();
    }
    public static PartRenderer createFaceRenderer () {
        return new PartRenderer("Face");
    }
    public static PartRenderer createFanRenderer () {
        return new PartRenderer("Fan");
    }
    public static PartRenderer createHairRenderer () { return new PartRenderer("Hair");}
    public static PartRenderer createHelmetRenderer () {
        return new PartRenderer("Helmet");
    }
    public static PartRenderer createMantleRenderer () {
        return new PartRenderer("Mantle");
    }
    public static MapRenderer createMapRenderer () { return new MapRenderer(); }
    public static MobRenderer createMobRenderer () {
        return new MobRenderer();
    }
    public static PartRenderer createSpearRenderer () {
        return new PartRenderer("Spear");
    }
    public static PartRenderer createShieldRenderer () { return new PartRenderer("Shield"); }
    public static PartRenderer createShoeRenderer () {
        return new PartRenderer("Shoes");
    }
    public static PartRenderer createSwordRenderer () {
        return new PartRenderer("Sword");
    }
}
