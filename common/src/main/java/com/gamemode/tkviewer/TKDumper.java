package com.gamemode.tkviewer;

import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.resources.Resources;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Dumps Image Frames from TKViewer types
 */
public class TKDumper {

    private static final String NTK_FRAMES_OUTPUT_DIR = "E:\\Reversing\\NTK\\frames_ntk";
    private static final String BARAM_FRAMES_OUTPUT_DIR = "E:\\Reversing\\NTK\\frames_baram";
    private static final String NTK_DAT_OUTPUT_DIR = "E:\\Reversing\\NTK\\dat_files_ntk";
    private static final String BARAM_DAT_OUTPUT_DIR = "E:\\Reversing\\NTK\\dat_files_baram";

    public static void main(String[] args) {
        (new File(NTK_FRAMES_OUTPUT_DIR)).mkdirs();
        (new File(BARAM_FRAMES_OUTPUT_DIR)).mkdirs();
        (new File(NTK_DAT_OUTPUT_DIR)).mkdirs();
        (new File(BARAM_DAT_OUTPUT_DIR)).mkdirs();

        extractDats(Resources.NTK_DATA_DIRECTORY, NTK_DAT_OUTPUT_DIR, false);
        extractDats(Resources.BARAM_DATA_DIRECTORY, BARAM_DAT_OUTPUT_DIR, true);

        System.out.println("===============BEGIN TK RENDERING===========");
        dumpAllImages(new PartRenderer("Sword", Resources.NTK_DATA_DIRECTORY), "Sword", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Spear", Resources.NTK_DATA_DIRECTORY), "Spear", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Body", Resources.NTK_DATA_DIRECTORY), "Body", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Fan", Resources.NTK_DATA_DIRECTORY), "Fan", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Shield", Resources.NTK_DATA_DIRECTORY), "Shield", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Bow", Resources.NTK_DATA_DIRECTORY), "Bow", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Coat", Resources.NTK_DATA_DIRECTORY), "Coat", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Face", Resources.NTK_DATA_DIRECTORY), "Face", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Hair", Resources.NTK_DATA_DIRECTORY), "Hair", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Mantle", Resources.NTK_DATA_DIRECTORY), "Mantle", NTK_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Shoes", Resources.NTK_DATA_DIRECTORY), "Shoes", NTK_FRAMES_OUTPUT_DIR);
        System.out.println("===============END TK RENDERING===========");

        System.out.println("===============BEGIN BARAM RENDERING===========");
        dumpAllImages(new PartRenderer("C_Body", Resources.BARAM_DATA_DIRECTORY), "C_Body", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("C_Spear", Resources.BARAM_DATA_DIRECTORY), "C_Spear", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("C_Sword", Resources.BARAM_DATA_DIRECTORY), "C_Sword", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("C_Riding", Resources.BARAM_DATA_DIRECTORY), "C_Riding", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("C_Shield", Resources.BARAM_DATA_DIRECTORY), "C_Shield", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("C_Fan", Resources.BARAM_DATA_DIRECTORY), "C_Fan", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("H_Body", Resources.BARAM_DATA_DIRECTORY), "H_Body", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Body", Resources.BARAM_DATA_DIRECTORY), "Body", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Bow", Resources.BARAM_DATA_DIRECTORY), "Bow", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("BowF", Resources.BARAM_DATA_DIRECTORY), "BowF", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Coat", Resources.BARAM_DATA_DIRECTORY), "Coat", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Face", Resources.BARAM_DATA_DIRECTORY), "Face", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Fan", Resources.BARAM_DATA_DIRECTORY), "Fan", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Hair", Resources.BARAM_DATA_DIRECTORY), "Hair", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("HairB", Resources.BARAM_DATA_DIRECTORY), "HairB", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Mantle", Resources.BARAM_DATA_DIRECTORY), "Mantle", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("MantleF", Resources.BARAM_DATA_DIRECTORY), "MantleF", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("MantleB", Resources.BARAM_DATA_DIRECTORY), "MantleB", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Shield", Resources.BARAM_DATA_DIRECTORY), "Shield", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Shoes", Resources.BARAM_DATA_DIRECTORY), "Shoes", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("ShoesB", Resources.BARAM_DATA_DIRECTORY), "ShoesB", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("ShoesF", Resources.BARAM_DATA_DIRECTORY), "ShoesF", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Spear", Resources.BARAM_DATA_DIRECTORY), "Spear", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("Sword", Resources.BARAM_DATA_DIRECTORY), "Sword", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("SwordF", Resources.BARAM_DATA_DIRECTORY), "SwordF", BARAM_FRAMES_OUTPUT_DIR);
        dumpAllImages(new PartRenderer("SwordB", Resources.BARAM_DATA_DIRECTORY), "SwordB", BARAM_FRAMES_OUTPUT_DIR);
        System.out.println("===============END BARAM RENDERING===========");

    }

    private static void extractDats(String dataDirectory, String dumpDirectory, boolean isBaram) {
        File dataDirectoryFile = new File(dataDirectory);
        File[] datFiles = dataDirectoryFile.listFiles((dir, name) -> name.contains(".dat"));
        for (File datFile : datFiles) {
            DatFileHandler datFileHandler = new DatFileHandler(datFile.getPath(), isBaram);
            File datDumpDirectory = new File(dumpDirectory + File.separator + datFile.getName());
            datDumpDirectory.mkdirs();
            datFileHandler.exportFiles(datDumpDirectory.getPath());
        }
    }

    private static void dumpAllFiles(DatFileHandler datFileHandler, String baseDirectoryString) {
        System.out.println("Will export extracted dat to: " + baseDirectoryString);
        File outputDirectory = new File(baseDirectoryString);
        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        datFileHandler.exportFiles(baseDirectoryString);
    }

    private static void dumpAllImages(PartRenderer renderer, String type, String outputDirectoryString) {
        File outputDirectory = new File(outputDirectoryString + File.separator + type);
        if (!outputDirectory.exists() && outputDirectory.mkdirs()) {
            System.err.println("Unable to create output directory: " + outputDirectoryString);
        }

        for (int i = 0; i < renderer.getCount(); i++) {
            Image[] images = renderer.getFrames(i);
            String epfName = renderer.getEpfNameForFrame(i);
            for (int j = 0; j < images.length; j++) {
                try {
                    File file = new File(
                            outputDirectory + File.separator
                                    + epfName + File.separator
                                    + String.format("%05d", i) + File.separator
                                    + String.format("%05d", j) + ".png");
                    file.getParentFile().mkdirs();
                    file.createNewFile();
                    ImageIO.write((BufferedImage) images[j], "png", file);
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
}