package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Part;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DscFileHandler extends FileHandler {

    private static final int HEADER = 0x17;

    public long partCount;

    public List<Part> parts;

    public DscFileHandler(String filepath) {
        this(new File(filepath));
    }

    public DscFileHandler(File file) {
        super(file);

        // Seek past header
        this.seek(this.HEADER, true);

        this.partCount = this.readInt(true, true);

        this.parts = new ArrayList<Part>();
        for (int i = 0; i < this.partCount; i++) {
            long id = this.readInt(true, true);
            long paletteIndex = this.readInt(true,true);

            long frameIndex = this.readInt(true,true);
            long frameCount = this.readInt(true,true);

            this.parts.add(new Part(id, paletteIndex, frameIndex, frameCount));

            // Unknown Flags
            this.seek(14, false);

            // Number of Chunks
            long chunkCount = this.readInt(true, true);
            for (int j = 0; j < chunkCount; j++) {
                // Two Unknown Ints
                this.seek(8, false);

                // Number of Blocks
                long blockCount = this.readInt(true, true);
                for (int k = 0; k < blockCount; k++) {
                    // Block Data
                    this.seek(9, false);
                }
            }
        }

        this.close();
    }
}
