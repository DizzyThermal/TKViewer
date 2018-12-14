package com.gamemode.tkviewer.file_handlers;

//import java.awt.*;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.Stencil;

import java.awt.*;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EpfFileHandler extends FileHandler {

    public static final int HEADER_SIZE = 0xC;

    private static final int FRAME_SIZE = 0x10;

    public int frameCount;
    public int width;
    public int height;
    public int bitBLT;
    public long pixelDataLength;
    public Map<Integer, Frame> frames_map;

    public EpfFileHandler(String filepath) {
        this(new File(filepath));
    }

    public EpfFileHandler(File file) {
        super(file);

        frames_map = new HashMap<Integer, Frame>();

        this.frameCount = this.readShort(true, false);
        this.width = this.readShort(true, false);
        this.height = this.readShort(true, false);
        this.bitBLT = this.readShort(true, false);
        this.pixelDataLength = this.readInt(true, true);
    }

    public Frame getFrame(int index) {
        // Return Frame if Cached
        if (this.frames_map.containsKey(index)) {
            return this.frames_map.get(index);
        }

        // Seek to Frame and read
        this.seek((HEADER_SIZE + this.pixelDataLength + (index * FRAME_SIZE)), true);
        int top = this.readShort(true, false);
        int left = this.readShort(true, false);
        int bottom = this.readShort(true, false);
        int right = this.readShort(true, false);

        int width = (right - left);
        int height = (bottom - top);

        long pixelDataOffset = this.readInt(true, true);
        long stencilDataOffset = this.readInt(true, true);

        // Seek to Pixel Data and Stencil Data
        this.seek(HEADER_SIZE + pixelDataOffset, true);
        ByteBuffer rawData = this.readBytes((width * height), true);
        Stencil stencil = new Stencil(this, stencilDataOffset, new Dimension(width, height));

        Frame frame = new Frame(top, left, bottom, right, width, height, pixelDataOffset, stencilDataOffset, rawData, stencil);
        this.frames_map.put(index, frame);

        return this.frames_map.get(index);
    }

    public static List<EpfFileHandler> createEpfsFromFiles(File[] epfFiles) {
        List<EpfFileHandler> epfFileHandlers = new ArrayList<EpfFileHandler>();
        for (int i = 0; i < epfFiles.length; i++) {
            epfFileHandlers.add(new EpfFileHandler(epfFiles[i]));
        }

        return epfFileHandlers;
    }
}
