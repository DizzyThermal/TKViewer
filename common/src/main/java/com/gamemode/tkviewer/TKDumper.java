package com.gamemode.tkviewer;
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

    private static final String OUTPUT_DIR = "C:\\TKFrames";

    private static PartRenderer bodyRenderer = RenderUtils.createBodyRenderer();
    private static PartRenderer bowRenderer = RenderUtils.createBowRenderer();
    private static PartRenderer coatRenderer = RenderUtils.createCoatRenderer();
    private static PartRenderer faceRenderer = RenderUtils.createFaceRenderer();
    private static PartRenderer fanRenderer = RenderUtils.createFanRenderer();
    private static PartRenderer hairRenderer = RenderUtils.createHairRenderer();
    private static PartRenderer mantleRenderer = RenderUtils.createMantleRenderer();
    private static PartRenderer spearRenderer = RenderUtils.createSpearRenderer();
    private static PartRenderer shoeRenderer = RenderUtils.createShoeRenderer();
    private static PartRenderer shieldRenderer = RenderUtils.createShieldRenderer();
    private static PartRenderer swordRenderer = RenderUtils.createSwordRenderer();

    public static void main(String[] args) {
        // Body
        dumpAllImages(bodyRenderer, "Body", OUTPUT_DIR);
        // Bow
        dumpAllImages(bowRenderer, "Bow", OUTPUT_DIR);
        // Coat
        dumpAllImages(coatRenderer, "Coat", OUTPUT_DIR);
        // Face
        dumpAllImages(faceRenderer, "Face", OUTPUT_DIR);
        // Hair
        dumpAllImages(hairRenderer, "Hair", OUTPUT_DIR);
        // Shield
        dumpAllImages(shieldRenderer, "Shield", OUTPUT_DIR);
        // Sword
        dumpAllImages(swordRenderer, "Sword", OUTPUT_DIR);
        // Fan
        dumpAllImages(fanRenderer, "Fan", OUTPUT_DIR);
        // Mantle
        dumpAllImages(mantleRenderer, "Mantle", OUTPUT_DIR);
        // Spear
        dumpAllImages(spearRenderer, "Spear", OUTPUT_DIR);
        // Shoes
        dumpAllImages(shoeRenderer, "Shoes", OUTPUT_DIR);
    }

    private static void dumpAllImages(PartRenderer renderer, String type, String outputDirectoryString) {
        File outputDirectory = new File(outputDirectoryString);
        if (!outputDirectory.exists() && outputDirectory.mkdirs()) {
            System.err.println("Unable to create output directory: " + outputDirectoryString);
        }

        for (int i = 0; i < renderer.getCount(); i++) {
            Image[] images = renderer.getFrames(i);
            for (Image image : images) {
                try {
                    ImageIO.write((BufferedImage)image, "png", new File(outputDirectory + File.separator + type + File.separator + type + "-" + String.format("%05d", i) + ".png"));
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }
        }
    }
}