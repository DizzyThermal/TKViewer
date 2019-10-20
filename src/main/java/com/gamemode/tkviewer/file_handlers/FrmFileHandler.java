package com.gamemode.tkviewer.file_handlers;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class FrmFileHandler extends FileHandler {

    private final byte TBL_MASK = 0x7F;

    public long effectCount;

    public List<Integer> paletteIndices;

    public FrmFileHandler(String filepath) {
        this(new File(filepath));
    }

    public FrmFileHandler(File file) {
        this(file, false);
    }

    public FrmFileHandler(File file, boolean decode) {
        super(file);

        this.effectCount = this.readInt(true, true);

        this.paletteIndices = new ArrayList<Integer>();
        for (int i = 0; i < this.effectCount; i++) {
            this.paletteIndices.add(this.readInt(true, true).intValue());
        }

        this.close();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        // Not implemented
        return null;
    }
}
