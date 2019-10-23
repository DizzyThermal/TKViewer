package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Part;
import com.gamemode.tkviewer.resources.PartBlock;
import com.gamemode.tkviewer.resources.PartChunk;
import com.gamemode.tkviewer.resources.PartMetadata;

import java.io.File;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

public class DscFileHandler extends FileHandler {

    private static final int HEADER = 0x17;

    public long partCount;

    public List<Part> parts;

    public DscFileHandler(String filepath) {
        this(new File(filepath));
    }

    public DscFileHandler(ByteBuffer bytes) {
        super(bytes);
        init();
    }

    public DscFileHandler(File file) {
        super(file);
        init();
    }

    public void init() {
        // Seek past header
        this.seek(DscFileHandler.HEADER, true);

        this.partCount = this.readInt(true, true);

        this.parts = new ArrayList<Part>();
        for (int i = 0; i < this.partCount; i++) {
            System.out.println(String.format("FilePosition = %d",this.filePosition));
            long id = this.readInt(true, true);
            long paletteIndex = this.readInt(true, true);

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

            // Number of Chunks
            long chunkCount = this.readInt(true, true);
            List<PartChunk> partChunks = new ArrayList<>();
            for (int j = 0; j < chunkCount; j++) {
                // Two Unknown Ints
                long chunkNumber = this.readInt(true, false);
                long chunkUnknown2 = this.readInt(true, false);

                // Number of Blocks
                long blockCount = this.readInt(true, true);
                List<PartBlock> partBlocks = new ArrayList<>();
                for (int k = 0; k < blockCount; k++) {
                    int frameOffset = this.readShort(true, false);
                    int unknownId1 = this.readUnsignedByte();
                    int unknownId2 = this.readUnsignedByte();
                    int unknownId3 = this.readUnsignedByte();
                    int unknownId4 = this.readUnsignedByte();
                    int unknownId5 = this.readUnsignedByte();
                    int unknownId6 = this.readUnsignedByte();
                    int unknownId7 = this.readUnsignedByte();
                    PartBlock block = new PartBlock(frameOffset, unknownId1, unknownId2, unknownId3, unknownId4,
                            unknownId5, unknownId6, unknownId7);
                    partBlocks.add(block);
                }
                PartChunk chunk = new PartChunk((int)chunkNumber, (int)chunkUnknown2, partBlocks);
                partChunks.add(chunk);
            }

            PartMetadata partMetadata = new PartMetadata(partUnknown1, partUnknown2, partUnknown3, partUnknown4,
                    partUnknown5, partUnknown6, partUnknown7);
            Part part = new Part(id, paletteIndex, frameIndex, frameCount, partChunks, partMetadata);
            this.parts.add(part);
        }

        this.close();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        // Not implemented
        return null;
    }
}
