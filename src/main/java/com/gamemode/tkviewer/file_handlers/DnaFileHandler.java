package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Mob;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DnaFileHandler extends FileHandler {

    public long mobCount;

    public List<Mob> mobs;

    public DnaFileHandler(String filepath) {
        this(new File(filepath));
    }

    public DnaFileHandler(File file) {
        super(file);

        this.mobCount = this.readInt(true, true);

        this.mobs = new ArrayList<Mob>();
        for (int i = 0; i < this.mobCount; i++) {
            long frameIndex = this.readInt(true, true);
            int chunkCount = this.readUnsignedByte();
            // Skip Unknown Byte
            this.seek(1, false);
            int paletteIndex = this.readShort(true, true);

            this.mobs.add(new Mob(frameIndex, paletteIndex, (byte)chunkCount));

            for (int j = 0; j < chunkCount; j++) {
                int blockCount = this.readShort(true, true);
                for (int k = 0; k < blockCount; k++) {
                    // Block Data
                    this.seek(9, false);
                }
            }
        }

        this.close();
    }
}
