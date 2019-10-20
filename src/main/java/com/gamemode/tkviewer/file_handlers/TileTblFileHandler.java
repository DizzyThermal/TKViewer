package com.gamemode.tkviewer.file_handlers;

import java.io.File;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class TblFileHandler extends FileHandler {

    private final byte TBL_MASK = 0x7F;

    public static final int HEADER_SIZE = 0x4;
    public static final int FRAME_SIZE = 0x2;

    public long tileCount;

    public List<Integer> paletteIndices;

    ByteBuffer rawBytes;
    boolean decoded = false;

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
        init(decode);
    }

    public TblFileHandler(ByteBuffer tblBytes) { this(tblBytes, false); }
    public TblFileHandler(ByteBuffer tblBytes, boolean decode) {
        super(tblBytes, decode);
        init(decode);
    }

    public void init(boolean decode) {
        this.decoded = decode;

        if (decode) {
            this.readInEncoded(file);
        } else {
            this.readInPlain(file);
        }
    }

    public void readInPlain(File file) {
        this.tileCount = this.readInt(true, true);
        this.rawBytes = ByteBuffer.allocate(HEADER_SIZE + (FRAME_SIZE * (int)this.tileCount));
        this.rawBytes.order(ByteOrder.LITTLE_ENDIAN);
        this.rawBytes.putInt((int)this.tileCount);

        this.paletteIndices = new ArrayList<Integer>();
        for (int i = 0; i < this.tileCount; i++) {
            int lsb = this.readUnsignedByte();
            this.rawBytes.put((byte)lsb);
            int msb = this.readUnsignedByte();
            this.rawBytes.put((byte)msb);

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
        this.rawBytes = ByteBuffer.allocate(HEADER_SIZE + (FRAME_SIZE * (int)this.tileCount));
        this.rawBytes.order(ByteOrder.LITTLE_ENDIAN);
        this.rawBytes.putInt((int)this.tileCount);

        this.paletteIndices = new ArrayList<Integer>();
        for (int i = 0; i < this.tileCount; i++) {
            // Unsigned Byte DOES NOT EXIST IN JAVA (wut?) - so we need to use an int
            int lsb = decoded.array()[HEADER_SIZE + (i * FRAME_SIZE)] & 0xFF;
            this.rawBytes.put((byte)lsb);
            int msb = decoded.array()[HEADER_SIZE + (i * FRAME_SIZE) + 1] & 0xFF;
            this.rawBytes.put((byte)msb);

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

    @Override
    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = null;

        if (this.decoded) {
            // Not Implemented
        } else {
            // Not Implemented
        }

        return byteBuffer;
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
