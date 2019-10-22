package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.*;

import java.io.File;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;

public class PalFileHandler extends FileHandler {

    // Changes in init() for Packed PALs
    private int HEADER_SIZE = 0x0;

    private final int ANIMATION_COUNT_POS = 24;
    private final int COLOR_COUNT = 256;

    public long paletteCount = 1;

    public List<Palette> palettes;

    public PalFileHandler(String filepath) {
        this(new File(filepath));
    }

    public PalFileHandler(ByteBuffer bytes) {
        super(bytes);
        init();
    }

    public PalFileHandler(File file) {
        super(file);
        init();
    }

    public void init() {
        String header = this.readString(9, true);
        this.seek(0, true);

        if (!header.equals("DLPalette")) {
            this.paletteCount = this.readInt(true, true);
            HEADER_SIZE = 0x4;
        }

        palettes = new ArrayList<Palette>();
        for (int i = 0; i < this.paletteCount; i++) {
            String palHeader = this.readString(9, true);
            ByteBuffer unknownBytes1 = this.readBytes(15, true);
            int animationColorCount = this.readSignedByte();

            ByteBuffer unknownBytes2 = this.readBytes(7, true);
            List<Integer> animationOffsets = new ArrayList<Integer>();
            for (int j = 0; j < (int) animationColorCount; j++) {
                animationOffsets.add(this.readShort(true, true));
            }

            List<Color> colors = new ArrayList<Color>();
            for (int j = 0; j < this.COLOR_COUNT; j++) {
                colors.add(new Color(this.readInt(false, true)));
            }

            PaletteMetadata paletteMetadata = new PaletteMetadata(palHeader, unknownBytes1, unknownBytes2);
            palettes.add(new Palette(animationColorCount, animationOffsets, colors, paletteMetadata));
        }

        this.close();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        int byteBufferSize = HEADER_SIZE;
        for (Palette pal : palettes) {
            byteBufferSize += 32;                                   // DLPalette + unknownBytes1 + animationColorCount + unknownBytes2
            byteBufferSize += (pal.getAnimationColorCount() * 2);   // Animation Color Counts
            byteBufferSize += (256 * 4);                            // 256 Colors (4 bytes each)
        }

        ByteBuffer returnBuffer = ByteBuffer.allocate(byteBufferSize);
        returnBuffer.order(ByteOrder.LITTLE_ENDIAN);

        if (HEADER_SIZE > 0) {
            returnBuffer.putInt((int)paletteCount);
        }

        for (int i = 0; i < palettes.size(); i++) {
            Palette pal = palettes.get(i);

            // Header (DLPalette)
            returnBuffer.put(pal.getPaletteMetadata().getHeader().getBytes());

            // Unknown ByteBuffer 1
            returnBuffer.put(pal.getPaletteMetadata().getUnknownBytes1());

            // Animation Color Count
            returnBuffer.put((byte)pal.getAnimationColorCount());

            // Unknown ByteBuffer 2
            returnBuffer.put(pal.getPaletteMetadata().getUnknownBytes2());

            // Animation Colors
            for (int j = 0; j < pal.getAnimationColorCount(); j++) {
                Integer animationColor = pal.getAnimationColorOffsets().get(j);
                Short animationColorShort = (short)animationColor.intValue();
                returnBuffer.putShort(animationColorShort);
            }

            // Colors (256)
            for (int j = 0; j < pal.getColors().size(); j++) {
                Color color = pal.getColors().get(j);
                returnBuffer.put((byte)color.getRed().intValue());
                returnBuffer.put((byte)color.getGreen().intValue());
                returnBuffer.put((byte)color.getBlue().intValue());
                returnBuffer.put((byte)color.getAlpha().intValue());
            }
        }

        return returnBuffer;
    }
}
