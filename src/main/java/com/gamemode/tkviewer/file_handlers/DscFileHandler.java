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
            System.out.println(String.format("FilePosition = %d",this.filePosition));
            long id = this.readInt(true, true);
            long paletteIndex = this.readInt(true,true);

            long frameIndex = this.readInt(true,true);
            long frameCount = this.readInt(true,true);

            // Unknown Flags
            int partUnknown1 = this.readShort(true, false);
            int partUnknown2 = this.readShort(true, false);
            int partUnknown3 = this.readShort(true, false);
            int partUnknown4 = this.readShort(true, false);
            int partUnknown5 = this.readShort(true, false);
            int partUnknown6 = this.readShort(true, false);
            int partUnknown7 = this.readShort(true, false);

            this.parts.add(new Part(id, paletteIndex, frameIndex, frameCount));

            // Number of Chunks
            long chunkCount = this.readInt(true, true);
            for (int j = 0; j < chunkCount; j++) {
                // Two Unknown Ints
                long chunkNumber = this.readInt(true, false);
                long chunkUnknown2 = this.readInt(true, false);
                // Number of Blocks
                long blockCount = this.readInt(true, true);
                for (int k = 0; k < blockCount; k++) {
                    System.out.println(this.filePosition);
                    int frameOffset = this.readShort(true, false);
                    int unknownId1 = this.readUnsignedByte();
                    int duration = this.readShort(true, false);
                    int unknownId2 = this.readUnsignedByte();
                    int unknownId3 = this.readUnsignedByte();
                    int unknownId4 = this.readUnsignedByte();
                    int unknownId5 = this.readUnsignedByte();
                    System.out.println("");
                }
            }
        }

        this.close();
    }
}
