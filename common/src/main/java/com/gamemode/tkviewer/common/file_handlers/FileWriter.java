package com.gamemode.tkviewer.common.file_handlers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;

public class FileWriter {

    RandomAccessFile fileHandle;

    public FileWriter(Path filePath) {
        this(filePath.toFile());
    }

    public FileWriter(File file) {
        try {
            this.fileHandle = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }
    }

    public void close() {
        try {
            this.fileHandle.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void write(byte[] bytes) {
        try {
            this.fileHandle.write(bytes);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public void writeInt(Integer intValue, boolean littleEndian) {
        if (littleEndian) {
            try {
                this.fileHandle.writeInt(intValue);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            try {
                int a = intValue & 0xFF;
                int b = (intValue >> 8) & 0xFF;
                int c = (intValue >> 16) & 0xFF;
                int d = (intValue >> 24) & 0xFF;
                this.fileHandle.writeByte(a);
                this.fileHandle.writeByte(b);
                this.fileHandle.writeByte(c);
                this.fileHandle.writeByte(d);
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }
}
