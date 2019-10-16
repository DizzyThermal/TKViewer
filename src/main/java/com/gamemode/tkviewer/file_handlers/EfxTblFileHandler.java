package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Effect;
import com.gamemode.tkviewer.resources.EffectFrame;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class EfxTblFileHandler extends FileHandler {

    private final byte TBL_MASK = 0x7F;

    public static final int HEADER_SIZE = 0x4;
    public static final int FRAME_SIZE = 0x2;

    public long effectCount;

    public List<Effect> effects;

    ByteBuffer rawBytes;

    public EfxTblFileHandler(String filepath) {
        this(new File(filepath));
    }

    public EfxTblFileHandler(File file) {
        super(file);

        int offset = 0;
        byte[] decodedBytes = new byte[(int)file.length() / 2];
        ByteBuffer encodedBytes;

        for (int i = 0; i < (file.length() / 8); i++) {
            encodedBytes = this.readBytes(8, true);
            ByteBuffer decodedByteByffer = this.decodeBytes(offset, encodedBytes);
            for (int j = 0; j < 4; j++) {
                decodedBytes[(i*4) + j] = decodedByteByffer.array()[3-j];
            }
            offset += 4;
        }

        this.close();

        rawBytes = ByteBuffer.wrap(decodedBytes).order(ByteOrder.LITTLE_ENDIAN);
//        try {
//            RandomAccessFile stream = new RandomAccessFile(new File("C:\\Users\\Stephen\\Desktop\\effect.dec.tbl"), "rw");
//            FileChannel fc = stream.getChannel();
//            fc.write(rawBytes);
//            stream.close();
//            fc.close();
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }

        this.effectCount = rawBytes.getInt();

        this.effects = new ArrayList<Effect>();
        for (int i = 0; i < this.effectCount; i++) {
            System.out.println("i: " + i);
            if (i ==70) {
                System.out.println();
            }
            int effectIndex = rawBytes.getInt();
            if (i != effectIndex) {
                System.out.println(i + " != " + effectIndex);
            }
            int frameCount = rawBytes.getInt();

            if (frameCount == 0) {
                rawBytes.get(new byte[8]); // skip 4
                frameCount = rawBytes.getInt(); // different frame count location
                rawBytes.get(new byte[8]); // skip 12
            } else {
                rawBytes.get(new byte[20]);
            }

            // Frame Data
            List<EffectFrame> effectFrames = new ArrayList<EffectFrame>();
            for (int j = 0; j < frameCount; j++) {
                System.out.println("  j: " + j);
                int frameIndex = rawBytes.getInt();
                if (frameIndex == -1) {
                    while (frameIndex != i + 1) {
                        if (frameIndex == -1) {
                            rawBytes.get(new byte[12]);
                        } else {
                            int frameDelay = rawBytes.getInt();
                            int paletteIndex = rawBytes.getInt();
                            int unknown2 = rawBytes.getInt();
                            effectFrames.add(new EffectFrame(frameIndex, frameDelay, paletteIndex, unknown2));
                            System.out.println("effectFrames.size():" + effectFrames.size());
                        }
                        frameIndex = rawBytes.getInt();
                    }
                    rawBytes.position(rawBytes.position()-4);
                    break;
                }
                int frameDelay = rawBytes.getInt();
                int paletteIndex = rawBytes.getInt();
                int unknown2 = rawBytes.getInt();

                effectFrames.add(new EffectFrame(frameIndex, frameDelay, paletteIndex, unknown2));
                System.out.println("effectFrames.size():" + effectFrames.size());
            }

            effects.add(new Effect(effectIndex, effectFrames.size(), effectFrames));
        }
    }

    public ByteBuffer decodeBytes(int offset, ByteBuffer encodedBytes) {
        // Decode 8-bytes to 4-byte int
        ByteBuffer decodedBytes = ByteBuffer.allocate(4);

        char[] arr = { 75, 25, 31, 29, 26, 9, 12, 12, 83, 73, 19, 17, 29, 23, 6, 29, 9, 6, 8, 27, 28, 1, 30, 29, 3, 5, 9 };

        for (int idx = 0; idx < 26; idx++) {
            arr[idx+1] ^= arr[idx];
        }

        long pre = (-0x1234568 - offset);
        pre = pre & 0x00000000FFFFFFFFL;
        long reverse_idx = pre % 0x1B;

        for (int idx = 0; idx < 8; idx++) {
            byte unsignedByte = encodedBytes.array()[idx];
            unsignedByte ^= arr[(int)reverse_idx];
            encodedBytes.put(idx, unsignedByte);

            pre = (reverse_idx + 26);
            pre = pre & 0x00000000FFFFFFFFL;
            reverse_idx = pre % 0x1B;
        }

        encodedBytes.order(ByteOrder.BIG_ENDIAN);
        long first = encodedBytes.getInt() & 0xFFFFFFFFL;
        long second = encodedBytes.getInt() & 0xFFFFFFFFL;

        decodedBytes.putInt((int)(first ^ (first ^ second) & 0x55555555));

        return decodedBytes;
    }

    public ByteBuffer toByteBuffer() {
        return this.rawBytes;
    }
}
