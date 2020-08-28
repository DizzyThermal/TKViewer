package com.gamemode.tkviewer.resources;

import com.gamemode.tkviewer.file_handlers.EpfFileHandler;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class Stencil {

    public static final int MASK = 0x80; // Hex representation of binary: 10000000

    public ByteBuffer rawStencilData;
    public List<boolean[]> rows;

    public Stencil(EpfFileHandler epfFileHandler, Long stencilDataOffset, Dimension dimensions) {
        // Seek to Stencil Data
        epfFileHandler.seek(EpfFileHandler.HEADER_SIZE + stencilDataOffset, true);

        // Rows are boolean arrays (true/false) representing whether or not to draw an individual pixel of *that* row.
        rows = new ArrayList<boolean[]>();
        ArrayList<Byte> rawStencilDataArray = new ArrayList<Byte>();

        // Absolute offset in pixelData *after* stencilDataOffset.
        for (int i = 0; i < dimensions.getHeight(); i++) {
            List<Integer> bytes = new ArrayList<Integer>();

            // Read row (until we encounter a 0x00 byte)
            while(true) {
                int stencilValue = epfFileHandler.readUnsignedByte();
                rawStencilDataArray.add((byte)stencilValue);

                if (stencilValue == 0) {
                    break;
                }

                bytes.add(stencilValue);
            }

            // Create row initialized to 'false' for each entry.
            boolean[] row = new boolean[(int)dimensions.getWidth()];

            // If bytes is not empty, interpret each stencilValue.
            if (!bytes.isEmpty()) {
                int rowOffset = 0;
                for(int j = 0; j < bytes.size(); j++) {
                    int stencilValue = bytes.get(j);

                    // Check if first bit of byte (8 bits) is '1' (or greater than 0x80)
                    boolean shouldDraw = false;
                    if (bytes.get(j) > this.MASK) {
                        shouldDraw = true;
                    }

                    if (shouldDraw) {
                        stencilValue = stencilValue ^ this.MASK;
                    }

                    // Add shouldDraw (true/false), "stencilValue" times to this row.
                    for (int k = 0; k < stencilValue; k++) {
                        row[rowOffset + k] = shouldDraw;
                    }
                    rowOffset += stencilValue;
                }
            }

            rows.add(row);
        }

        rawStencilData = ByteBuffer.allocate(rawStencilDataArray.size());
        for (Byte b : rawStencilDataArray) {
            rawStencilData.put(b);
        }
    }

    public ByteBuffer toByteBuffer() {
        return this.rawStencilData;
    }
}
