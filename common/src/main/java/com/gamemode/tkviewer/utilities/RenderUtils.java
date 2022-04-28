package com.gamemode.tkviewer.utilities;

import com.gamemode.tkviewer.EffectImage;
import com.gamemode.tkviewer.Frame;
import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.file_handlers.EpfFileHandler;
import com.gamemode.tkviewer.file_handlers.PalFileHandler;
import com.gamemode.tkviewer.render.*;
import com.gamemode.tkviewer.PivotData;
import com.gamemode.tkviewer.resources.Resources;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RenderUtils {
    public static DatFileHandler CHAR_DAT = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "char.dat");

    /**
     * Private constructor to prevent instantiation of static utility class
     */
    private RenderUtils() {}

    public static PivotData getPivotData(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();

        int left = width / 2;
        int top = height / 2;

        return new PivotData(left, top, width, height);
    }

    public static PivotData getPivotData(List<Frame> frames) {
        // Determine Canvas Size
        int left, top, right, bottom;
        left = top = 10000;
        right = bottom = -10000;
        for (Frame frame : frames) {
            if (frame == null) {
                continue;
            }

            if (frame.getLeft() < left) {
                left = frame.getLeft();
            }
            if (frame.getTop() < top) {
                top = frame.getTop();
            }
            if (frame.getRight() > right) {
                right = frame.getRight();
            }
            if (frame.getBottom() > bottom) {
                bottom = frame.getBottom();
            }
        }

        int width = right-left;
        int height = bottom-top;

        return new PivotData(Math.abs(left), Math.abs(top), width, height);
    }

    public static List<EffectImage> aggregateAnimations (List < List <EffectImage>> effImages){
        List<Frame> allFrames = new ArrayList<>();
        for (List<EffectImage> subListImages : effImages) {
            allFrames.addAll(subListImages.stream().map(EffectImage::getFrame).collect(Collectors.toList()));
        }
        PivotData pivotData = RenderUtils.getPivotData(allFrames);
        int maxWidth = pivotData.getCanvasWidth();
        int maxHeight = pivotData.getCanvasHeight();

        // Correct Images according to maxWidth and maxHeight
        for (int i = 0; i < effImages.size(); i++) {
            for (int j = 0; j < effImages.get(i).size(); j++) {
                EffectImage effImage = effImages.get(i).get(j);
                effImage.setImage(resizeImage(effImage.getImage(), maxWidth, maxHeight, pivotData,
                        effImage.getFrame(), effImage.getPivotData()));
            }
        }

        List<EffectImage> mergedImages = effImages.get(0);
        for (int i = 1; i < effImages.size(); i++) {
            mergedImages = mergeEffectImages(mergedImages, effImages.get(i));
        }

        return mergedImages;
    }

    /**
     * Draws images2 on top of images1 - images all must be equal size!
     */
    public static List<EffectImage> mergeEffectImages(List<EffectImage> images1, List<EffectImage> images2) {
        List<EffectImage> returnEffectImages = new ArrayList<EffectImage>();

        int count = Math.max(images1.size(), images2.size());
        int width = Math.max(images1.get(0).getImage().getWidth(), images2.get(0).getImage().getWidth());
        int height = Math.max(images1.get(0).getImage().getHeight(), images2.get(0).getImage().getHeight());
        for (int i = 0; i < count; i++) {
            BufferedImage newImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D graphicsObject = newImage.createGraphics();
            graphicsObject.drawImage(images1.get(i % images1.size()).getImage(),null,0,0);
            graphicsObject.drawImage(images2.get(i % images2.size()).getImage(),null,0,0);

            returnEffectImages.add(new EffectImage(newImage, images1.get(i % images1.size()).getDelay(), null, null));
        }

        return returnEffectImages;
    }

    public static BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight, PivotData pivotData,
                                            Frame frame, PivotData framePivotData) {
        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphicsObject = newImage.createGraphics();
        int frameLeft = (pivotData.getPivotX() - framePivotData.getPivotX());
        int frameTop = (pivotData.getPivotY() - framePivotData.getPivotY());
        graphicsObject.drawImage(image, null, frameLeft, frameTop);

        return newImage;
    }

    public static PartRenderer createRenderer(String fileSubstring, String dataDirectory){
        return new PartRenderer(fileSubstring, dataDirectory);
    }
    public static PartRenderer createBaramBodyRenderer () {
        System.out.println("Creating baram body renderer");
        return new PartRenderer("Body", Resources.BARAM_DATA_DIRECTORY);
    }
    public static PartRenderer createBaramClassicBodyRenderer () {
        System.out.println("Creating baram classic body renderer");
        return new PartRenderer("C_Body", Resources.BARAM_DATA_DIRECTORY);
    }
    public static PartRenderer createBodyRenderer () { return new PartRenderer("Body"); }
    public static PartRenderer createBowRenderer () {
        return new PartRenderer("Bow");
    }
    public static PartRenderer createCoatRenderer () {
        return new PartRenderer("Coat");
    }
    public static EffectRenderer createEffectRenderer () {
        return new EffectRenderer();
    }
    public static PartRenderer createFaceRenderer () {
        return new PartRenderer("Face");
    }
    public static PartRenderer createFaceDecRenderer () { return new PartRenderer("FaceDec"); }
    public static PartRenderer createFanRenderer () {
        return new PartRenderer("Fan");
    }
    public static PartRenderer createHairRenderer () { return new PartRenderer("Hair");}
    public static PartRenderer createHelmetRenderer () {
        return new PartRenderer("Helmet");
    }
    public static TileRenderer createItemRenderer () {
        DatFileHandler charDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "char.dat");
        DatFileHandler miscDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "misc.dat");

        EpfFileHandler itemEpf = new EpfFileHandler(miscDat.getFile("ITEM.EPF"),"ITEM.EPF");
        PalFileHandler itemPal = new PalFileHandler(charDat.getFile("ITEM.PAL"));

        return new TileRenderer(new ArrayList<EpfFileHandler>(Arrays.asList(itemEpf)), itemPal, 0);
    }
    public static TileRenderer createLegendResourceRenderer () {
        DatFileHandler charDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "char.dat");
        DatFileHandler miscDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "misc.dat");

        EpfFileHandler epf = new EpfFileHandler(miscDat.getFile("SYMBOLS.EPF"),"SYMBOLS.EPF");
        PalFileHandler pal = new PalFileHandler(charDat.getFile("ITEM.PAL"));

        return new TileRenderer(new ArrayList<EpfFileHandler>(Arrays.asList(epf)), pal, 0);
    }
    public static PartRenderer createMantleRenderer () {
        return new PartRenderer("Mantle");
    }
    public static MapRenderer createMapRenderer () { return new MapRenderer(); }
    public static ArrayList<TileRenderer> createMiniMapResourceRenderers () {
        ArrayList<TileRenderer> miniMapResourceRenderers = new ArrayList<TileRenderer>();
        String[] mmrExts = {"PLAYER", "SYMBOL", "TITLE"};
        DatFileHandler mnmDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "mnm.dat");
        for (String mmrExt : mmrExts) {
            EpfFileHandler epf = new EpfFileHandler(mnmDat.getFile("MN" + mmrExt + ".epf"),"MN" + mmrExt + ".epf");
            PalFileHandler pal = new PalFileHandler(mnmDat.getFile("MN" + mmrExt + ".pal"));

            miniMapResourceRenderers.add(new TileRenderer(new ArrayList<EpfFileHandler>(Arrays.asList(epf)), pal, 0));
        }

        return miniMapResourceRenderers;
    }
    public static MobRenderer createMobRenderer () {
        return new MobRenderer();
    }
    public static PartRenderer createSpearRenderer () {
        return new PartRenderer("Spear");
    }
    public static PartRenderer createShieldRenderer () { return new PartRenderer("Shield"); }
    public static PartRenderer createShoeRenderer () {
        return new PartRenderer("Shoes");
    }
    public static PartRenderer createSwordRenderer () {
        return new PartRenderer("Sword");
    }
    public static ArrayList<TileRenderer> createWorldMapRenderers () {
        ArrayList<TileRenderer> worldMapRenderers = new ArrayList<TileRenderer>();
        String[] wmExts = {"", "2", "3", "4", "kru"};
        DatFileHandler wmDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "wm.dat");
        for (String wmExt : wmExts) {
            EpfFileHandler epf = new EpfFileHandler(wmDat.getFile("WM" + wmExt + ".epf"),"WM" + wmExt + ".epf");
            PalFileHandler pal = new PalFileHandler(wmDat.getFile("WM" + wmExt + ".pal"));

            worldMapRenderers.add(new TileRenderer(new ArrayList<EpfFileHandler>(Arrays.asList(epf)), pal, 0));
        }

        return worldMapRenderers;
    }
}
