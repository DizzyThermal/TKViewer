package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.TblIndex;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class TileTblFileHandler extends FileHandler {

    private final int TBL_MASK     = 0b01111111;
    private final int MSB_MASK     = 0b11111111;

    private final int REV_TBL_MASK = 0b0111111100000000;
    private final int LSB_MASK     = 0b11111111;

    private final long UINT32_MASK = 0xFFFFFFFFL;
    private final int  ENC_MASK2   = 0x55555555;

    public static final int HEADER_SIZE = 0x4;
    public static final int FRAME_SIZE = 0x2;

    public long tileCount;

    public List<TblIndex> paletteIndices;

    boolean decoded = false;

    public TileTblFileHandler(String filepath) {
        this(new File(filepath));
    }
    public TileTblFileHandler(String filepath, boolean decode) {
        this(new File(filepath), decode);
    }

    public TileTblFileHandler(File file) {
        this(file, false);
    }
    public TileTblFileHandler(File file, boolean decode) {
        super(file);
        init(decode);
    }

    public TileTblFileHandler(ByteBuffer tblBytes) { this(tblBytes, false); }
    public TileTblFileHandler(ByteBuffer tblBytes, boolean decode) {
        super(tblBytes, decode);
        init(decode);
    }

    public void init(boolean decode) {
        this.decoded = decode;

        if (decode) {
            this.readInEncoded();
        } else {
            this.readInPlain();
        }
    }

    public Integer paletteIndexFromlsbMsb(int lsb, int msb) {
        return ((msb & this.TBL_MASK) << 8) | lsb;
    }

    public Integer unknownFlagFromMsb(int msb) {
        return (msb & this.MSB_MASK) >> 7;
    }

    public Integer lsbFromPaletteIndex(int paletteIndex) {
        int lsb = (paletteIndex & this.LSB_MASK);

        return lsb;
    }

    public Integer msbFromPaletteIndex(int paletteIndex, int unknownFlag) {
        int msb = ((paletteIndex & this.REV_TBL_MASK) >> 8) | (unknownFlag << 7);

        return msb;
    }

    public void readInPlain() {
        this.tileCount = this.readInt(true, true);

        this.paletteIndices = new ArrayList<TblIndex>();
        for (int i = 0; i < this.tileCount; i++) {
            int lsb = this.readUnsignedByte();
            int msb = this.readUnsignedByte();
            int paletteIndex = paletteIndexFromlsbMsb(lsb, msb);
            int unknownFlag = unknownFlagFromMsb(msb);

            this.paletteIndices.add(new TblIndex(paletteIndex, unknownFlag));
        }

        this.close();
    }

    public void readInEncoded() {
        int offset = 0;
        byte[] decodedBytes = new byte[this.getLength() / 2];
        ByteBuffer encodedBytes;

        for (int i = 0; i < (this.getLength() / 8); i++) {
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

        this.paletteIndices = new ArrayList<TblIndex>();
        for (int i = 0; i < this.tileCount; i++) {
            // Unsigned Byte DOES NOT EXIST IN JAVA (wut?) - so we need to use an int
            int lsb = decoded.array()[HEADER_SIZE + (i * FRAME_SIZE)] & 0xFF;
            int msb = decoded.array()[HEADER_SIZE + (i * FRAME_SIZE) + 1] & 0xFF;

            int paletteIndex = paletteIndexFromlsbMsb(lsb, msb);
            int unknownFlag = unknownFlagFromMsb(msb);

            this.paletteIndices.add(new TblIndex(paletteIndex, unknownFlag));
        }
    }

    public ByteBuffer decodeBytes(int offset, ByteBuffer encodedBytes) {
        // Decode 8-bytes to 4-byte int
        ByteBuffer decodedBytes = ByteBuffer.allocate(4);

        // Algorithm for creating key
        // char[] arr = { 75, 25, 31, 29, 26, 9, 12, 12, 83, 73, 19, 17, 29, 23, 6, 29, 9, 6, 8, 27, 28, 1, 30, 29, 3, 5, 9 };
        // for (int idx = 0; idx < 26; idx++) {
        //     arr[idx+1] ^= arr[idx];
        // }

        char[] arr = { 75, 82, 77, 80, 74, 67, 79, 67, 16, 89, 74, 91, 70, 81, 87, 74, 67, 69, 77, 86, 74, 75, 85, 72, 75, 78, 71 };

        long pre = (-0x1234568 - offset);
        pre = pre & this.UINT32_MASK;
        long reverse_idx = pre % 0x1B;

        for (int idx = 0; idx < 8; idx++) {
            byte unsignedByte = encodedBytes.array()[idx];
            unsignedByte ^= arr[(int)reverse_idx];
            encodedBytes.put(idx, unsignedByte);

            pre = (reverse_idx + 26);
            pre = pre & this.UINT32_MASK;
            reverse_idx = pre % 0x1B;
        }

        encodedBytes.order(ByteOrder.BIG_ENDIAN);

        // Get First and Second integers (unsigned)
        long first = encodedBytes.getInt() & this.UINT32_MASK;
        long second = encodedBytes.getInt() & this.UINT32_MASK;

        long third = (first ^ (first ^ second)) & this.ENC_MASK2;
        decodedBytes.putInt((int)third);

        return decodedBytes;
    }

    public ByteBuffer encodeBytes(int offset, ByteBuffer decodedBytes) {
        // Encode 4-bytes to 8-byte long
        ByteBuffer encodedBytes = ByteBuffer.allocate(8);

        char[] arr = { 75, 82, 77, 80, 74, 67, 79, 67, 16, 89, 74, 91, 70, 81, 87, 74, 67, 69, 77, 86, 74, 75, 85, 72, 75, 78, 71 };

        long pre = (-0x1234568 - offset);
        pre = pre & this.UINT32_MASK;
        long reverse_idx = pre % 0x1B;

        for (int idx = 0; idx < 8; idx++) {
            byte unsignedByte = encodedBytes.array()[idx];
            unsignedByte ^= arr[(int)reverse_idx];
            encodedBytes.put(idx, unsignedByte);

            pre = (reverse_idx + 26);
            pre = pre & this.UINT32_MASK;
            reverse_idx = pre % 0x1B;
        }

        encodedBytes.order(ByteOrder.BIG_ENDIAN);
        long first = encodedBytes.getInt() & this.UINT32_MASK;
        long second = encodedBytes.getInt() & this.UINT32_MASK;

        decodedBytes.putInt((int)(first ^ (first ^ second) & this.ENC_MASK2));

        return decodedBytes;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        ByteBuffer byteBuffer = null;

        if (this.decoded) {
            // Not Implemented
        } else {
            int byteBufferSize = (HEADER_SIZE + (FRAME_SIZE * (int)this.tileCount));
            byteBuffer = ByteBuffer.allocate(byteBufferSize);
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);

            // Tile Count
            byteBuffer.putInt((int)this.tileCount);

            // Palette Indicies
            for (int i = 0; i < this.tileCount; i++) {
                TblIndex tblIndex = this.paletteIndices.get(i);

                byte lsb = (byte)lsbFromPaletteIndex(tblIndex.getPaletteIndex()).intValue();
                byte msb = (byte)msbFromPaletteIndex(tblIndex.getPaletteIndex(), tblIndex.getUnknownFlag()).intValue();

                byteBuffer.put(lsb);
                byteBuffer.put(msb);
            }
        }

        return byteBuffer;
    }

    public boolean compareTo(TileTblFileHandler tbl2) {
        if (this.tileCount != tbl2.tileCount) {
            return false;
        }

        for (int i = 0; i < tileCount; i++) {
            if (this.paletteIndices.get(i).getPaletteIndex() != tbl2.paletteIndices.get(i).getPaletteIndex()) {
                return false;
            }
        }

        return true;
    }
}
