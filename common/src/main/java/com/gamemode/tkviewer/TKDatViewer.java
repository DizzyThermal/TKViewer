package com.gamemode.tkviewer;

import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.resources.Resources;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

/**
 * Prints the content of all .dat files in NTK Data Directory.
 */
public class TKDatViewer {

    private static final String DAT_REGEX = ".*.dat$";
    private static final String BREAK = "------------------";
    private static final int SPACE_PAD = 30;

    // File output size unit, set 'outputSize' below
    private enum OutputSize { B, KB, MB }
    private static final OutputSize outputSize = OutputSize.B;
    private static int biggest = 0;
    public static void main(String[] args) {
        writeAllDatInfoToFile("C:\\NTK_DatInfo.txt");
    }

    private static void exportAllDats(String outputDirectory) {
        File outputDataDirectory = new File(outputDirectory);
        if (!outputDataDirectory.exists()) {
            outputDataDirectory.mkdirs();
        }

        File dataDirectory = new File(Resources.NTK_DATA_DIRECTORY);
        if (dataDirectory.exists() && dataDirectory.isDirectory()) {
            File[] datFiles = dataDirectory.listFiles((dir, name) -> name.matches(DAT_REGEX));

            if (datFiles != null) {
                for (File datFile : datFiles) {
                    DatFileHandler datFileHandler = new DatFileHandler(datFile);
                    datFileHandler.printDatFiles();
                    datFileHandler.exportFiles(outputDirectory);
                }
            }
        }
    }

    private static void printAllDats() {
        File dataDirectory = new File(Resources.NTK_DATA_DIRECTORY);
        if (dataDirectory.exists() && dataDirectory.isDirectory()) {
            File[] datFiles = dataDirectory.listFiles((dir, name) -> name.matches(DAT_REGEX));

            if (datFiles != null) {
                for (File datFile : datFiles) {
                    DatFileHandler datFileHandler = new DatFileHandler(datFile);
                    printDatInfo(datFileHandler);
                }
            }
        }
    }

    private static String getOutputString(String fileName, Long fileLength) {
        String fileSizeString = " (" + fileLength + " " + outputSize.toString() + ")";
        StringBuilder outputString = new StringBuilder("  - " + fileName);

        for (int i = 0; i < SPACE_PAD-fileName.length()-fileSizeString.length(); i++) {
            outputString.append(" ");
        }

        outputString.append(fileSizeString);

        return outputString.toString();
    }

    private static void printDatInfo(DatFileHandler datFileHandler) {
        System.out.println(datFileHandler.file.getName() + " (" + datFileHandler.fileCount + " files)");
        System.out.println(BREAK);
        biggest = 0;
        for (Map.Entry<String, ByteBuffer> entry : datFileHandler.files.entrySet()) {
            if (entry.getKey().length() > biggest) {
                biggest = entry.getKey().length();
            }
            String fileName = entry.getKey();
            long fileLength = entry.getValue().array().length;

            switch(outputSize) {
                case KB:
                    fileLength = Math.round(fileLength / 1024.0);
                    break;
                case MB:
                    fileLength = Math.round(fileLength / 1048576.0);
                    break;
            }

            System.out.println(getOutputString(fileName, fileLength));
        }
        System.out.println();
    }

    private static void writeAllDatInfoToFile(String outputPath) {
        try {
            Path path = Paths.get(outputPath);

            try (BufferedWriter writer = Files.newBufferedWriter(path))
            {
                File dataDirectory = new File(Resources.NTK_DATA_DIRECTORY);
                if (dataDirectory.exists() && dataDirectory.isDirectory()) {
                    File[] datFiles = dataDirectory.listFiles((dir, name) -> name.matches(DAT_REGEX));

                    if (datFiles != null) {
                        for (File datFile : datFiles) {
                            DatFileHandler datFileHandler = new DatFileHandler(datFile);
                            writer.write(datFileHandler.file.getName() + " (" + datFileHandler.fileCount + " files)");
                            writer.newLine();
                            writer.write(BREAK);
                            writer.newLine();
                            for (Map.Entry<String, ByteBuffer> entry : datFileHandler.files.entrySet()) {
                                String fileName = entry.getKey();
                                long fileLength = entry.getValue().array().length;

                                switch(outputSize) {
                                    case KB:
                                        fileLength = Math.round(fileLength / 1024.0);
                                        break;
                                    case MB:
                                        fileLength = Math.round(fileLength / 1048576.0);
                                        break;
                                }
                                writer.write(getOutputString(fileName, fileLength));
                                writer.newLine();
                            }
                            writer.newLine();
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}