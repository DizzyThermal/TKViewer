package com.gamemode.tkviewer;

import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.utilities.RenderUtils;

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

//    private static PartRenderer baramBodyRenderer = RenderUtils.createBaramBodyRenderer();
//    private static PartRenderer bowRenderer = RenderUtils.createBowRenderer();
//    private static PartRenderer coatRenderer = RenderUtils.createCoatRenderer();
//    private static PartRenderer faceRenderer = RenderUtils.createFaceRenderer();
//    private static PartRenderer fanRenderer = RenderUtils.createFanRenderer();
//    private static PartRenderer hairRenderer = RenderUtils.createHairRenderer();
//    private static PartRenderer mantleRenderer = RenderUtils.createMantleRenderer();
//    private static PartRenderer spearRenderer = RenderUtils.createSpearRenderer();
//    private static PartRenderer shoeRenderer = RenderUtils.createShoeRenderer();
//    private static PartRenderer shieldRenderer = RenderUtils.createShieldRenderer();
//    private static PartRenderer swordRenderer = RenderUtils.createSwordRenderer();

    public static void main(String[] args) {
        // Body
//        DatFileHandler datFileHandler = RenderUtils.createDatFileHandler();
//        System.out.println("Dumping NTK dat files to " + NTK_DAT_OUTPUT_DIR);
//        dumpAllFiles(datFileHandler, NTK_DAT_OUTPUT_DIR);
//        System.out.println("Done dumping dat files.");
//        System.out.println("Begin dumping NTK images.");
//        PartRenderer bodyRenderer = RenderUtils.createBodyRenderer();
//        dumpAllImages(bodyRenderer, "Body", NTK_FRAMES_OUTPUT_DIR);
//        System.out.println("Done dumping images.");
//
//        DatFileHandler baramDatFileHandler = RenderUtils.createBaramDatFileHandler();
//        System.out.println("Dumping BARAM dat files to " + BARAM_DAT_OUTPUT_DIR);
//        dumpAllFiles(baramDatFileHandler, BARAM_DAT_OUTPUT_DIR);
//        System.out.println("Done dumping dat files.");
        System.out.println("Begin dumping BARAM images.");
        PartRenderer baramBodyRenderer = RenderUtils.createBaramBodyRenderer();
        dumpAllImages(baramBodyRenderer, "Body", BARAM_FRAMES_OUTPUT_DIR);
        System.out.println("Done dumping images.");

        System.out.println("Begin dumping BARAM Classic images.");
        PartRenderer baramClassicBodyRenderer = RenderUtils.createBaramClassicBodyRenderer();
        dumpAllImages(baramClassicBodyRenderer, "C_Body", BARAM_FRAMES_OUTPUT_DIR);
        System.out.println("Done dumping images.");

//        dumpAllImages(baramBodyRenderer, "Body", OUTPUT_DIR);
//        // Body
//        dumpAllImages(bodyRenderer, "Body", OUTPUT_DIR);
//        // Bow
//        dumpAllImages(bowRenderer, "Bow", OUTPUT_DIR);
//        // Coat
//        dumpAllImages(coatRenderer, "Coat", OUTPUT_DIR);
//        // Face
//        dumpAllImages(faceRenderer, "Face", OUTPUT_DIR);
//        // Hair
//        dumpAllImages(hairRenderer, "Hair", OUTPUT_DIR);
//        // Shield
//        dumpAllImages(shieldRenderer, "Shield", OUTPUT_DIR);
//        // Sword
//        dumpAllImages(swordRenderer, "Sword", OUTPUT_DIR);
//        // Fan
//        dumpAllImages(fanRenderer, "Fan", OUTPUT_DIR);
//        // Mantle
//        dumpAllImages(mantleRenderer, "Mantle", OUTPUT_DIR);
//        // Spear
//        dumpAllImages(spearRenderer, "Spear", OUTPUT_DIR);
//        // Shoes
//        dumpAllImages(shoeRenderer, "Shoes", OUTPUT_DIR);
    }

    private static void dumpAllFiles(DatFileHandler datFileHandler, String baseDirectoryString) {
        System.out.println("Will export extracted dat to: " + baseDirectoryString);
        File outputDirectory = new File(baseDirectoryString);
        if (!outputDirectory.exists() && outputDirectory.mkdirs()) {
            System.err.println("Unable to create output directory: " + baseDirectoryString);
        }

        datFileHandler.exportFiles(baseDirectoryString);
    }

    private static void dumpAllImages(PartRenderer renderer, String type, String outputDirectoryString) {
        File outputDirectory = new File(outputDirectoryString);
        if (!outputDirectory.exists() && outputDirectory.mkdirs()) {
            System.err.println("Unable to create output directory: " + outputDirectoryString);
        }

        for (int i = 0; i < renderer.getCount(); i++) {
            Image[] images = renderer.getFrames(i);
            for (int j = 0; j < images.length; j++) {
                try {
                    File file = new File(outputDirectory + File.separator + type + File.separator + type + "-" + String.format("%05d", i) + "-" + String.format("%05d", j) + ".png");
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