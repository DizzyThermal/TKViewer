package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Effect;
import com.gamemode.tkviewer.resources.EffectFrame;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
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
        this(new File(filepath), true);
    }
    public EfxTblFileHandler(String filepath, boolean decode) {
        this(new File(filepath), decode);
    }

    public EfxTblFileHandler(ByteBuffer bytes) { this(bytes, true); }
    public EfxTblFileHandler(ByteBuffer bytes, boolean decode) {
        super(bytes);
        init(decode);
    }

    public EfxTblFileHandler(File file) { this(file, true); }
    public EfxTblFileHandler(File file, boolean decode) {
        super(file);
        init(decode);
    }

    public void init(boolean decode) {
        byte[] bytes;
        if (decode) {
            int offset = 0;
            bytes = new byte[this.getLength() / 2];
            ByteBuffer encodedBytes;

            for (int i = 0; i < (this.getLength() / 8); i++) {
                encodedBytes = this.readBytes(8, true);
                ByteBuffer decodedByteByffer = this.decodeBytes(offset, encodedBytes);
                for (int j = 0; j < 4; j++) {
                    bytes[(i * 4) + j] = decodedByteByffer.array()[3 - j];
                }
                offset += 4;
            }
        } else {
            bytes = this.readBytes((int)this.getLength(), true).array();
        }

        this.close();

        rawBytes = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);

        this.effectCount = rawBytes.getInt();

        this.effects = new ArrayList<Effect>();
        for (int i = 0; i < this.effectCount; i++) {
            int effectIndex = rawBytes.getInt();
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

    @Override
    public ByteBuffer toByteBuffer() {
        // Not implemented - NEED TO BE ENCODED.
        return null;
    }
}
