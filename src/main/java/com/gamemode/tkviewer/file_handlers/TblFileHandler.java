package com.gamemode.tkviewer.file_handlers;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class TblFileHandler extends FileHandler {

    private final byte TBL_MASK = 0x7F;

    public long tileCount;

    public List<Integer> paletteIndices;

    public TblFileHandler(String filepath) {
        this(new File(filepath));
    }

    public TblFileHandler(String filepath, boolean decode) {
        this(new File(filepath), decode);
    }

    public TblFileHandler(File file) {
        this(file, false);
    }

    public TblFileHandler(File file, boolean decode) {
        super(file);

        if (decode) {
            this.readInEncoded(file);
        } else {
            this.readInPlain(file);
        }
    }

    public void readInPlain(File file) {
        this.tileCount = this.readInt(true, true);

        this.paletteIndices = new ArrayList<Integer>();
        for (int i = 0; i < this.tileCount; i++) {
            int lsb = this.readUnsignedByte();
            int msb = this.readUnsignedByte();

            this.paletteIndices.add(((msb & this.TBL_MASK) << 8) | lsb);
        }

        this.close();
    }

    public void readInEncoded(File file) {
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

        ByteBuffer decoded = ByteBuffer.wrap(decodedBytes).order(ByteOrder.LITTLE_ENDIAN);

        this.tileCount = decoded.getInt(0);

        this.paletteIndices = new ArrayList<Integer>();
        for (int i = 0; i < this.tileCount; i++) {
            int lsb = decoded.array()[4 + (i*2)] & 0xFF;
            int msb = decoded.array()[4 + (i*2) + 1] & 0xFF;

            this.paletteIndices.add(((msb & this.TBL_MASK) << 8) | lsb);
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

    public boolean compareTo(TblFileHandler tbl2) {
        if (this.tileCount != tbl2.tileCount) {
            return false;
        }

        for (int i = 0; i < tileCount; i++) {
            if (this.paletteIndices.get(i).intValue() != tbl2.paletteIndices.get(i).intValue()) {
                return false;
            }
        }

        return true;
    }
}
