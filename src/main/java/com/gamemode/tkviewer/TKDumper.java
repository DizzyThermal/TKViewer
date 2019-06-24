package com.gamemode.tkviewer;
import com.gamemode.tkviewer.file_handlers.*;
import com.gamemode.tkviewer.render.MapRenderer;
import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TKDumper {
    PartRenderer bodyRenderer;
    PartRenderer bowRenderer;
    PartRenderer coatRenderer;
    PartRenderer faceRenderer;
    PartRenderer fanRenderer;
    PartRenderer hairRenderer;
    PartRenderer mantleRenderer;
    PartRenderer spearRenderer;
    PartRenderer shoesRenderer;
    PartRenderer shieldRenderer;
    PartRenderer swordRenderer;

    TKDumper() {
        // Body
        loadBodyResources();
        dumpAllImages(this.bodyRenderer, "Body", "D:\\TKFrames\\Bodies\\");
        // Bow
        loadBowResources();
        dumpAllImages(this.bowRenderer, "Bow", "D:\\TKFrames\\Bows\\");
        // Coat
        loadCoatResources();
        dumpAllImages(this.coatRenderer, "Coat", "D:\\TKFrames\\Coats\\");
        // Face
        loadFaceResources();
        dumpAllImages(this.faceRenderer, "Face", "D:\\TKFrames\\Faces\\");
        // Hair
        loadHairResources();
        dumpAllImages(this.hairRenderer, "Hair", "D:\\TKFrames\\Hairs\\");
        // Shield
        loadShieldResources();
        dumpAllImages(this.shieldRenderer, "Shield", "D:\\TKFrames\\Shields\\");
        // Sword
        loadSwordResources();
        dumpAllImages(this.swordRenderer, "Sword", "D:\\TKFrames\\Swords\\");
        // Fan
        loadFanResources();
        dumpAllImages(this.fanRenderer, "Fan", "D:\\TKFrames\\Fans\\");
        // Mantle
        loadMantleResources();
        dumpAllImages(this.mantleRenderer, "Mantle", "D:\\TKFrames\\Mantles\\");
        // Spear
        loadSpearResources();
        dumpAllImages(this.spearRenderer, "Spear", "D:\\TKFrames\\Spears\\");
        // Shoes
        loadShoesResources();
        dumpAllImages(this.shoesRenderer, "Shoes", "D:\\TKFrames\\Shoes\\");
    }

    public static void main(String[] args) {
        TKDumper tkdumper = new TKDumper();
    }

    public void dumpAllImages(PartRenderer renderer, String type, String outputDirectory) {
        int count = renderer.getCount();
        int columns = 10;
        int rows;
        int maxWidth;
        int maxHeight;
        int x;
        int y;
        int col;
        for (int i = 0; i < count; i++) {
            File outputPath = new File(outputDirectory + type + "_" + i + ".png");
            Image[] images = renderer.getFrames(i);
            // int n = a / b + ((a % b == 0) ? 0 : 1);
            rows = (images.length / columns) + ((images.length % columns == 0) ? 0 : 1);
            maxWidth = 0;
            maxHeight = 0;
            for (Image image : images) {
                ImageIcon img = new ImageIcon(image);
                if (img.getIconWidth() > maxWidth) {
                    maxWidth = img.getIconWidth();
                }
                if (img.getIconHeight() > maxHeight) {
                    maxHeight = img.getIconHeight();
                }
            }
            BufferedImage finalImage = new BufferedImage(maxWidth * columns, maxHeight * rows, BufferedImage.TYPE_INT_RGB);
            Graphics g = finalImage.getGraphics();
            x = 0;
            y = 0;
            col = 1;
            for (Image image : images) {
                g.drawImage(image, x, y, null);
                col += 1;
                if (col > columns) {
                    col = 1;
                    y += maxHeight;
                    x = 0;
                } else {
                    x += maxWidth;
                }
            }
            try {
                ImageIO.write(finalImage, "jpg", outputPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void loadBodyResources() {
        // Extract Required Body Files
        FileUtils.extractBodyFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Body Resources
        bodyRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getBodyEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Body.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Body.dsc")));
    }

    public void loadBowResources() {
        // Extract Required Bow Files
        FileUtils.extractBowFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Bow Resources
        bowRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getBowEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Bow.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Bow.dsc")));
    }

    public void loadCoatResources() {
        // Extract Required Coat Files
        FileUtils.extractCoatFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Coat Resources
        coatRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getCoatEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Coat.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Coat.dsc")));
    }

    public void loadFaceResources() {
        // Extract Required Face Files
        FileUtils.extractFaceFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Face Resources
        faceRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getFaceEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Face.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Face.dsc")));
    }

    public void loadFanResources() {
        // Extract Required Fan Files
        FileUtils.extractFanFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Fan Resources
        fanRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getFanEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Fan.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Fan.dsc")));
    }

    public void loadHairResources() {
        // Extract Required Hair Files
        FileUtils.extractHairFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Hair Resources
        hairRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getHairEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Hair.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Hair.dsc")));
    }

    public void loadMantleResources() {
        // Extract Required Mantle Files
        FileUtils.extractMantleFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Mantle Resources
        mantleRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getMantleEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Mantle.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Mantle.dsc")));
    }

    public void loadSpearResources() {
        // Extract Required Spear Files
        FileUtils.extractSpearFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Spear Resources
        spearRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getSpearEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Spear.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Spear.dsc")));
    }

    public void loadShieldResources() {
        // Extract Required Shield Files
        FileUtils.extractShieldFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Shield Resources
        shieldRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getShieldEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Shield.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Shield.dsc")));
    }

    public void loadShoesResources() {
        // Extract Required Shoes Files
        FileUtils.extractShoesFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Shoes Resources
        shoesRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getShoesEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Shoes.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Shoes.dsc")));
    }

    public void loadSwordResources() {
        // Extract Required Sword Files
        FileUtils.extractSwordFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Part Renderer from Sword Resources
        swordRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getSwordEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Sword.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Sword.dsc")));
    }
}