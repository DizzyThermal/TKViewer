package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Mob;
import com.gamemode.tkviewer.resources.MobBlock;
import com.gamemode.tkviewer.resources.MobChunk;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
            int unknown1 = this.readUnsignedByte();
            int paletteIndex = this.readShort(true, true);

            List<MobChunk> mobChunks = new ArrayList<MobChunk>();
            for (int j = 0; j < chunkCount; j++) {
                int blockCount = this.readShort(true, true);
                List<MobBlock> mobBlocks = new ArrayList<MobBlock>();
                for (int k = 0; k < blockCount; k++) {
                    int frameOffset = this.readShort(true, false);
                    int duration = this.readShort(true, false);
                    int unknownId1 = this.readShort(true, true);
                    int transparency = this.readUnsignedByte();
                    int unknownId2 = this.readUnsignedByte();
                    int unknownId3 = this.readUnsignedByte();

                    MobBlock block = new MobBlock(frameOffset, duration, unknownId1, transparency, unknownId2, unknownId3);
                    mobBlocks.add(block);
                }
                MobChunk chunk = new MobChunk(blockCount, mobBlocks);
                mobChunks.add(chunk);
            }

            this.mobs.add(new Mob(frameIndex, chunkCount, (byte)unknown1, paletteIndex, mobChunks));
        }

        this.close();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        // Not implemented
        return null;
    }
}
