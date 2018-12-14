package com.gamemode.tkviewer.file_handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class DatFileHandler extends FileHandler {

    public long fileCount;
    public Map<String, ByteBuffer> files = new HashMap<String, ByteBuffer>();

    public DatFileHandler(String filepath) {
        this(new File(filepath));
    }

    public DatFileHandler(File file) {
        super(file);

        this.fileCount = this.readInt(true, true) - 1;
        for (int i = 0; i < this.fileCount; i++) {
            long dataLocation = this.readInt(true, true);
            int totalRead = 13;
            int readLength = lengthUntilZero();
            String fileName = this.readString(readLength, true);
            if (readLength < totalRead) {
                this.seek(totalRead-readLength, false);
            }
            long nextFileLocation = this.filePosition;
            long fileSize = this.readInt(true, true) - dataLocation;
            this.seek(dataLocation, true);
            ByteBuffer fileData = this.readBytes(fileSize, true);
            files.put(fileName, fileData);
            this.seek(nextFileLocation, true);
        }

        this.close();
    }

    public void exportFiles(String outputDirectory) {
        outputDirectory = outputDirectory.replaceAll("\\\\", "/");
        File outputDirectoryFile = new File(outputDirectory);
        if (!outputDirectoryFile.exists()) {
            boolean result = outputDirectoryFile.mkdirs();
            if (!result) {
                System.out.println("Unable to create output directory");
            }
        }
        for (Map.Entry<String, ByteBuffer> entry : this.files.entrySet()) {
            File outputFile = new File(outputDirectoryFile, entry.getKey().trim());
            if (!outputFile.exists()) {
                try {
                    boolean result = outputFile.createNewFile();
                    if (!result) {
                        System.out.println("Unable to create file: " + entry.getKey());
                    }
                } catch (IOException ioe) {
                    System.out.println("Unable to create file: " + ioe);
                }
            }
            try {
                FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
                fileOutputStream.write(entry.getValue().array());
                fileOutputStream.flush();
                fileOutputStream.close();
            } catch (IOException ioe) {
                System.out.println("Unable to open file: " + ioe);
            }

        }
    }

    public int lengthUntilZero() {
        long currentPosition = this.filePosition;
        int length = 0;
        while(true) {
            byte b = this.readSignedByte();
            if (b != 0) {
                length++;
            } else {
                break;
            }
        }

        this.seek(currentPosition, true);
        return length;
    }
}
