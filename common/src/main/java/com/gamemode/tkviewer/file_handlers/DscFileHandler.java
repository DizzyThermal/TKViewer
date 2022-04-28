package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.Part;
import com.gamemode.tkviewer.PartBlock;
import com.gamemode.tkviewer.PartChunk;
import com.gamemode.tkviewer.PartMetadata;

import java.io.File;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

public class DscFileHandler extends FileHandler {

    private static final int HEADER = 0x17;

    public long partCount;

    public List<Part> parts;

    public DscFileHandler(String filepath, boolean isBaram) {
        this(new File(filepath), isBaram);
    }

    public DscFileHandler(ByteBuffer bytes, boolean isBaram) {
        super(bytes);
        init(isBaram);
    }

    public DscFileHandler(File file, boolean isBaram) {
        super(file);
        init(isBaram);
    }

    public void init(boolean isBaram) {
        // Seek past header
        this.seek(DscFileHandler.HEADER, true);

        this.partCount = this.readInt(true, true);

        this.parts = new ArrayList<Part>();
        for (int i = 0; i < this.partCount; i++) {

            long id = this.readInt(true, true);
            long paletteIndex = this.readInt(true, true);

            long frameIndex = this.readInt(true, true);
            long frameCount = this.readInt(true, true);

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
                    if (isBaram) {
                        int unknownId8 = this.readUnsignedByte();
                        int unknownId9 = this.readUnsignedByte();
                        int unknownId10 = this.readUnsignedByte();
                    }
                    PartBlock block = new PartBlock(frameOffset, unknownId1, unknownId2, unknownId3, unknownId4,
                            unknownId5, unknownId6, unknownId7);
                    partBlocks.add(block);
                }
                PartChunk chunk = new PartChunk((int) chunkNumber, (int) chunkUnknown2, partBlocks);
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
