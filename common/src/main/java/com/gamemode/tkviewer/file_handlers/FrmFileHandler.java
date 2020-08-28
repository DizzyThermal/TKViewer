package com.gamemode.tkviewer.file_handlers;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class FrmFileHandler extends FileHandler {

    private final byte HEADER = 0x4;
    private final byte PALETTE_SIZE = 0x4;

    public long effectCount;

    public List<Integer> paletteIndices;

    public FrmFileHandler(String filepath) {
        this(new File(filepath));
    }

    public FrmFileHandler(ByteBuffer bytes) {
        super(bytes);
        init();
    }

    public FrmFileHandler(File file) {
        super(file);
        init();
    }

    public void init() {
        this.effectCount = this.readInt(true, true);

        this.paletteIndices = new ArrayList<Integer>();
        for (int i = 0; i < this.effectCount; i++) {
            this.paletteIndices.add(this.readInt(true, true).intValue());
        }

        this.close();
    }


    @Override
    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(HEADER + ((int)this.effectCount * PALETTE_SIZE));

        byteBuffer.putInt((int)this.effectCount);

        for (int i = 0; i < this.effectCount; i++) {
            byteBuffer.putInt(this.paletteIndices.get(i));
        }

        return byteBuffer;
    }
}
