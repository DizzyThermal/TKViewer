package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.SObject;

import java.io.File;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

public class SObjTblFileHandler extends FileHandler {

    public long objectCount;

    public List<SObject> objects;

    public SObjTblFileHandler(String filepath) {
        this(new File(filepath));
    }

    public SObjTblFileHandler(File file) {
        super(file);

        this.objectCount = this.readInt(true, true);

        this.seek(2, false);

        this.objects = new ArrayList<SObject>();
        for (int i = 0; i < this.objectCount; i++) {
            // Unknown
            this.seek(5, false);

            byte movementDirection = this.readSignedByte();
            byte height = this.readSignedByte();

            List<Integer> tileIndices = new ArrayList<>();
            for (int j = 0; j < (int) height; j++) {
                tileIndices.add(this.readShort(true, true));
            }

            this.objects.add(new SObject(movementDirection, height, tileIndices));
        }

        this.close();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        // Not implemented
        return null;
    }
}
