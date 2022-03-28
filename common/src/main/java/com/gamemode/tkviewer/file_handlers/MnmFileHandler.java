package com.gamemode.tkviewer.file_handlers;

import java.io.File;
import java.nio.ByteBuffer;

public class MnmFileHandler extends FileHandler {

    public String mapName;

    public MnmFileHandler(String filepath) {
        this(new File(filepath));
    }

    public MnmFileHandler(File file) {
        super(file);

        // Seek into JPEG
        this.seek(0x300, true);

        // Find end of JPEG
        while (true) {
            Integer fileByte = this.readUnsignedByte();
            if (fileByte == 0xFF) {
                Integer nextByte = this.readUnsignedByte();
                if (nextByte == 0xD9) {
                    // End of JPEG (FF D9)
                    break;
                } else {
                    this.seek(-1, false);
                }
            }

        }

        // Map Name
        int mapNameLength = this.readInt(true, false).intValue();
        mapName = this.readString(mapNameLength, true, true);

        // TODO: Read in NPCs and Warps

        this.close();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        // Not implemented
        return null;
    }
}
