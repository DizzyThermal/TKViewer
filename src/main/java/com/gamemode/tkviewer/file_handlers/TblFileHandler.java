package com.gamemode.tkviewer.file_handlers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TblFileHandler extends FileHandler {

    private final byte TBL_MASK = 0x7F;

    public long tileCount;

    public List<Integer> paletteIndices;

    public TblFileHandler(String filepath) {
        this(new File(filepath));
    }

    public TblFileHandler(File file) {
        super(file);

        this.tileCount = this.readInt(true, true);

        this.paletteIndices = new ArrayList<Integer>();
        for (int i = 0; i < this.tileCount; i++) {
            int lsb = this.readUnsignedByte();
            int msb = this.readUnsignedByte();

            this.paletteIndices.add(((msb & this.TBL_MASK) << 8) | lsb);
        }

        this.close();
    }
}
