package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Color;
import com.gamemode.tkviewer.resources.Palette;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PalFileHandler extends FileHandler {

    private final int ANIMATION_COUNT_POS = 24;
    private final int COLOR_COUNT = 256;

    public long paletteCount = 1;

    public List<Palette> palettes;

    public PalFileHandler(String filepath) {
        this(new File(filepath));
    }

    public PalFileHandler(File file) {
        super(file);

        String header = this.readString(9, true);
        this.seek(0, true);

        if (!header.equals("DLPalette")) {
            this.paletteCount = this.readInt(true, true);
        }

        palettes = new ArrayList<Palette>();
        for (int i = 0; i < this.paletteCount; i++) {
            this.seek(this.ANIMATION_COUNT_POS, false);
            byte animationColorCount = this.readSignedByte();

            this.seek(7, false);
            List<Integer> animationOffsets = new ArrayList<Integer>();
            for (int j = 0; j < (int) animationColorCount; j++) {
                animationOffsets.add(this.readShort(true, true));
            }

            List<Color> colors = new ArrayList<Color>();
            for (int j = 0; j < this.COLOR_COUNT; j++) {
                colors.add(new Color(this.readInt(false, true)));
            }

            palettes.add(new Palette(animationColorCount, animationOffsets, colors));
        }

        this.close();
    }
}
