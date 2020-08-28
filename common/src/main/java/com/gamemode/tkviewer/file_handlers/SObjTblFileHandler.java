package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.SObject;

import java.io.File;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

public class SObjTblFileHandler extends FileHandler {

    public long objectCount;

    public ByteBuffer unknownBytes;

    public List<SObject> objects;

    public SObjTblFileHandler(String filepath) {
        this(new File(filepath));
    }

    public SObjTblFileHandler(ByteBuffer bytes) {
        super(bytes);
        init();
    }

    public SObjTblFileHandler(File file) {
        super(file);
        init();
    }

    public void init() {
        this.objectCount = this.readInt(true, true);

        this.unknownBytes = this.readBytes(2, true);

        this.objects = new ArrayList<SObject>();
        for (int i = 0; i < this.objectCount; i++) {
            // Unknown
            ByteBuffer unknownBytesObject = this.readBytes(5, true);

            byte movementDirection = (byte)this.readSignedByte();
            byte height = (byte)this.readSignedByte();

            List<Integer> tileIndices = new ArrayList<>();
            for (int j = 0; j < (int) height; j++) {
                tileIndices.add(this.readShort(true, true));
            }

            this.objects.add(new SObject(movementDirection, height, tileIndices, unknownBytesObject));
        }

        this.close();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        // Not implemented
        return null;
    }
}
