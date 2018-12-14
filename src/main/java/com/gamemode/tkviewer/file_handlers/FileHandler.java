package com.gamemode.tkviewer.file_handlers;

import com.google.common.primitives.UnsignedBytes;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.InflaterInputStream;

public class FileHandler {

    public long filePosition = 0;
    public File file;
    public RandomAccessFile fileInputStream;

    public FileHandler(String filePath) {
        this(new File(filePath));
    }

    public FileHandler(File file) {
        this.file = file;
        try {
            this.fileInputStream = new RandomAccessFile(this.file, "r");
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e);
        }
    }

    public void seek(long position, boolean absolute) {
        try {
            if (absolute) {
                this.filePosition = position;
            } else {
                this.filePosition += position;
            }
            this.fileInputStream.seek(this.filePosition);
        } catch (IOException ioe) {
            System.out.println("Unable to seek in file: " + ioe);
        }
    }

    public ByteBuffer read(boolean littleEndian) {
        ByteBuffer byteBuffer = null;
        try {
            long fileLength = (int) this.fileInputStream.length() - this.filePosition;
            byte[] content = new byte[(int) fileLength];
            fileInputStream.readFully(content, 0, (int) fileLength);
            this.filePosition += fileLength + this.filePosition;
            byteBuffer = ByteBuffer.wrap(content);
        } catch (IOException ioe) {
            System.out.println("Unable to read from file: " + ioe);
        }

        if (littleEndian) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        return byteBuffer;
    }

    public ByteBuffer readCompressed(boolean littleEndian) {
        ByteArrayOutputStream uncompressedOutputStream = new ByteArrayOutputStream();

        try {
            long fileLength = (int) this.fileInputStream.length() - this.filePosition;
            byte[] content = new byte[(int) fileLength];
            fileInputStream.readFully(content, 0, (int) fileLength);
            this.filePosition += fileLength + this.filePosition;

            InflaterInputStream inflaterInputStream = new InflaterInputStream(new ByteArrayInputStream(content));
            try {
                byte[] buffer = new byte[2048];
                int len;
                while((len = inflaterInputStream.read(buffer)) > 0) {
                    uncompressedOutputStream.write(buffer, 0, len);
                }
            } catch (IOException e) {
                throw new AssertionError(e);
            }
        } catch (IOException ioe) {
            System.out.println("Unable to read from file: " + ioe);
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(uncompressedOutputStream.toByteArray());

        if (littleEndian) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        return byteBuffer;
    }

    public Integer readUnsignedByte() {
        int unsignedByte = 0x00;

        try {
            unsignedByte = fileInputStream.readUnsignedByte();
            this.filePosition++;
        } catch (IOException ioe) {
            System.out.println("Unable to read from file: " + ioe);
        }

        return unsignedByte;
    }

    public byte readSignedByte() {
        byte b = 0x00;

        try {
            b = fileInputStream.readByte();
            this.filePosition++;
        } catch (IOException ioe) {
            System.out.println("Unable to read from file: " + ioe);
        }

        return b;
    }

    public ByteBuffer readBytes(long length, boolean littleEndian) {
        byte[] content = new byte[(int) length];
        try {
            fileInputStream.readFully(content, 0, (int) length);
            this.filePosition += length;
        } catch (IOException ioe) {
            System.out.println("Unable to read from file: " + ioe);
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(content);

        if (littleEndian) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        return byteBuffer;
    }

    public Long readInt(boolean littleEndian, boolean unsigned) {
        byte[] content = new byte[4];
        try {
            fileInputStream.readFully(content, 0, 4);
            this.filePosition += 4;
        } catch (IOException ioe) {
            System.out.println("Unable to read from file: " + ioe);
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(content);
        if (littleEndian) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        if (unsigned) {
            return (((long) byteBuffer.getInt() & 0xFFFFFFFFL));
        } else {
            return (long)byteBuffer.getInt();
        }
    }

    public Integer readShort(boolean littleEndian, boolean unsigned) {
        byte[] content = new byte[2];
        try {
            fileInputStream.readFully(content, 0, 2);
            this.filePosition += 2;
        } catch (IOException ioe) {
            System.out.println("Unable to read from file: " + ioe);
        }

        ByteBuffer byteBuffer = ByteBuffer.wrap(content);
        if (littleEndian) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        if (unsigned) {
            return (((int) byteBuffer.getShort() & 0xFFFF));
        } else {
            return (int) byteBuffer.getShort();
        }
    }

    public String readString(int length, boolean littleEndian) {
        ByteBuffer byteBuffer = this.readBytes(length, littleEndian);
        if (littleEndian) {
            byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        }

        return new String(byteBuffer.array());
    }

    public void close() {
        try {
            this.fileInputStream.close();
        } catch (IOException ioe) {
            System.out.println("Unable to close file: " + ioe);
        }
    }
}
