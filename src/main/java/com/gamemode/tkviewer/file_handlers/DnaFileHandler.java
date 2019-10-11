package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Mob;
import com.gamemode.tkviewer.resources.MobBlock;
import com.gamemode.tkviewer.resources.MobChunk;

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
            int unknown1 = this.readUnsignedByte();
            int paletteIndex = this.readShort(true, true);

            List<MobChunk> mobChunks = new ArrayList<MobChunk>();
            for (int j = 0; j < chunkCount; j++) {
                int blockCount = this.readShort(true, true);
                List<MobBlock> mobBlocks = new ArrayList<MobBlock>();
                for (int k = 0; k < blockCount; k++) {
                    int unknownId1 = this.readShort(true, true);
                    int unknownId2 = this.readShort(true, true);
                    int unknownId3 = this.readShort(true, true);
                    int unknownId4 = this.readShort(true, true);
                    int unknownId5 = this.readUnsignedByte();

                    mobBlocks.add(new MobBlock(unknownId1, unknownId2, unknownId3, unknownId4, unknownId5));
                }
                mobChunks.add(new MobChunk(blockCount, mobBlocks));
            }

            this.mobs.add(new Mob(frameIndex, chunkCount, (byte)unknown1, paletteIndex, mobChunks));
        }

        this.close();
    }
}
