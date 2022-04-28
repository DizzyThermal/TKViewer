package com.gamemode.tkviewer.file_handlers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class DatFileHandler extends FileHandler {
    boolean isBaram;
    public String filePath;
    public long fileCount;
    public Map<String, ByteBuffer> files = new LinkedHashMap<>();

    public DatFileHandler(Path filepath) {
        this(filepath.toFile(), false);
    }

    public DatFileHandler(String filepath) {
        this(new File(filepath), false);
    }

    public DatFileHandler(String filepath, boolean isBaram) {
        this(new File(filepath), isBaram);
    }

    public DatFileHandler(File file) {
        this(file, false);
    }

    public DatFileHandler(File file, boolean isBaram) {
        super(file);
        this.isBaram = isBaram;
        this.filePath = this.file.getPath();
        System.out.println(this.filePath + ": " + "Reading file count from index: " + this.filePosition);
        this.fileCount = this.readInt(true, true) - 1;
        System.out.println(this.filePath + ": " + "Got fileCount of " + this.fileCount);
        for (int i = 0; i < this.fileCount; i++) {
            long dataLocation = this.readInt(true, true);
            int totalRead = isBaram ? 32 : 13;
            int readLength = lengthUntilZero();
            String fileName = this.readString(readLength, true);
            if (readLength < totalRead) {
                this.seek(totalRead - readLength, false);
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
//        this.fileCount = this.readInt(true, true) - 1;
//        System.out.println(this.filePath + ": " + "Got fileCount of " + this.fileCount);
//        for (int i = 0; i < this.fileCount; i++) {
//            long dataBeginLocationIndex = this.filePosition;
//            long dataBeginLocation = this.readInt(true, true);
//            int maxFilenameLength = isBaram ? 32 : 13;
//            if (isBaram) {
//                System.out.println("This is a baram file, will use filename  length of 32.");
//            }
//            int readLength = lengthUntilZero();
//            String fileName = this.readString(readLength, true);
//            System.out.println(this.filePath + ": " + fileName);
//            System.out.println("  readLength: " + readLength);
//
//            if (readLength < maxFilenameLength) {
//                this.seek(maxFilenameLength - readLength, false);
//            }
//            long nextDataBeginLocationIndex = this.filePosition;
//            System.out.println("  nextDataBeginLocationIndex: " + nextDataBeginLocationIndex);
//
//            long nextDataBeginLocation = this.readInt(true, true);
//            System.out.println("  nextDataBeginLocation: " + nextDataBeginLocation);
//            long fileSize = nextDataBeginLocation - dataBeginLocation;
//            System.out.println("  fileSize: " + fileSize);
//            System.out.println("  readLength: " + readLength);
//            System.out.println("  dataBeginLocationIndex: " + dataBeginLocationIndex);
//            System.out.println("  dataBeginLocation: " + dataBeginLocation);
//            this.seek(dataBeginLocation, true);
//            ByteBuffer fileData = this.readBytes(fileSize, true);
//            files.put(fileName, fileData);
//            this.seek(nextDataBeginLocationIndex, true);
//        }
//
//        this.close();
//    }

    public void writeDatFile(Path outputPath) {
        FileWriter fileOutputStream;
        fileOutputStream = new FileWriter(outputPath);

        // Write File Count ( + 1 )
        fileOutputStream.writeInt(Math.toIntExact(this.fileCount + 1), false);

        // Write Table of Contents
        long filePointer = 8 + (this.fileCount * 17) + 13; // Header (File Count + TOC)
        for (Map.Entry<String, ByteBuffer> entry : this.files.entrySet()) {
            // Data Location
            fileOutputStream.writeInt(Math.toIntExact(filePointer), false);

            // File Name (Pad 13 bytes)
            byte[] fileName = new byte[13];
            byte[] fileNameBytes = entry.getKey().getBytes();
            for (byte b = 0; b < fileName.length; b++) {
                if (b < fileNameBytes.length) {
                    fileName[b] = fileNameBytes[b];
                } else {
                    fileName[b] = 0;
                }
            }
            fileOutputStream.write(fileName);

            // Update File Pointer
            filePointer += entry.getValue().array().length;
        }
        fileOutputStream.writeInt(Math.toIntExact(filePointer), false);
        fileOutputStream.write(new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00});

        // Dump Binary Data
        for (Map.Entry<String, ByteBuffer> entry : this.files.entrySet()) {
            fileOutputStream.write(entry.getValue().array());
        }

        // Close File
        fileOutputStream.close();
    }

    public void printDatFiles() {
        System.out.println(this.file.getName());
        for (Map.Entry<String, ByteBuffer> entry : this.files.entrySet()) {
            System.out.println("  " + entry.getKey());
        }
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

    public ByteBuffer getFile(String key) {
        return this.getFile(key, true);
    }

    public ByteBuffer getFile(String key, boolean caseInsensitive) {
        for (Map.Entry<String, ByteBuffer> entry : this.files.entrySet()) {
            if (caseInsensitive && entry.getKey().toLowerCase().equals(key.toLowerCase())) {
                System.out.println("Returning file: " + entry.getKey());
                return entry.getValue();
            } else if (!caseInsensitive && entry.getKey().equals(key)) {
                System.out.println("Returning file: " + entry.getKey());
                return entry.getValue();
            }
        }

        if (caseInsensitive) {

        }

        return null;
    }

    private int lengthUntilZero() {
        long currentPosition = this.filePosition;
        int length = 0;
        while (true) {
            byte b = (byte) this.readSignedByte();
            if (b != 0) {
                length++;
            } else {
                break;
            }
        }

        this.seek(currentPosition, true);
        return length;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        // Not implemented
        return null;
    }
}
