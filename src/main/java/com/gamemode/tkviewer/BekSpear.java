package com.gamemode.tkviewer.ignore;

import com.gamemode.tkviewer.render.PartRenderer;
import com.gamemode.tkviewer.resources.EffectImage;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.PivotData;
import com.gamemode.tkviewer.utilities.FileUtils;
import com.gamemode.tkviewer.utilities.RenderUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Stab Right with Bek Spear.
 *
 * Body[0] = Body 0
 *   Chunk[17] = Stab Right
 *
 * Face[0] = Face 0
 *   Chunk[17] = Stab Right
 *
 * Hair[0] = Hair 0
 *  *   Chunk[17] = Stab Right
 *
 * Spear[10] = Bek Spear
 *  *   Chunk[5] = Stab Right
 */
public class TKViewer_BekSpear {
    public static void main(String[] args) {
        // Create PartRenderers
        PartRenderer bodyRenderer = new PartRenderer("Body");
        PartRenderer spearRenderer = new PartRenderer("Spear");
        PartRenderer faceRenderer = new PartRenderer("Face");
        PartRenderer hairRenderer = new PartRenderer("Hair");

        List<EffectImage> bodyImages = bodyRenderer.renderAnimation(445, 17);
        List<EffectImage> spearImages = spearRenderer.renderAnimation(11, 5);
        List<EffectImage> faceImages = faceRenderer.renderAnimation(0, 17);
        List<EffectImage> hairImages = hairRenderer.renderAnimation(0, 17);

        List<Frame> allFrames = new ArrayList<>();

        allFrames.addAll(bodyImages.stream().map(EffectImage::getFrame).collect(Collectors.toList()));
        allFrames.addAll(spearImages.stream().map(EffectImage::getFrame).collect(Collectors.toList()));
        allFrames.addAll(faceImages.stream().map(EffectImage::getFrame).collect(Collectors.toList()));
        allFrames.addAll(hairImages.stream().map(EffectImage::getFrame).collect(Collectors.toList()));

        PivotData pivotData = RenderUtils.getPivotData(allFrames);

        int maxWidth = pivotData.getCanvasWidth();
        int maxHeight = pivotData.getCanvasHeight();

        // Correct Images according to maxWidth and maxHeight
        for (int i = 0; i < bodyImages.size(); i++) {
            // Correct Body Images
            EffectImage bodyImage = bodyImages.get(i);
            bodyImage.setImage(resizeImage(bodyImage.getImage(), maxWidth, maxHeight, pivotData,
                    bodyImage.getFrame()));

            // Correct Face Images
            EffectImage faceImage = faceImages.get(i);
            faceImage.setImage(resizeImage(faceImage.getImage(), maxWidth, maxHeight, pivotData,
                    faceImage.getFrame()));

            // Correct Spear Images
            EffectImage spearImage = spearImages.get(i);
            spearImage.setImage(resizeImage(spearImage.getImage(), maxWidth, maxHeight, pivotData,
                    spearImage.getFrame()));

            // Correct Hair Images
            EffectImage hairImage = hairImages.get(i);
            hairImage.setImage(resizeImage(hairImage.getImage(), maxWidth, maxHeight, pivotData,
                    hairImage.getFrame()));
        }

        List<EffectImage> faceHairImages = mergeEffectImages(faceImages, hairImages);
        List<EffectImage> bodyFaceImages = mergeEffectImages(bodyImages, faceHairImages);
        List<EffectImage> mergedEffects = mergeEffectImages(bodyFaceImages, spearImages);
        FileUtils.exportGifFromImages(mergedEffects, "C:\\Users\\Stephen\\Desktop\\test.gif");
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
            graphicsObject.drawImage(images1.get(i).getImage(),null,0,0);
            graphicsObject.drawImage(images2.get(i).getImage(),null,0,0);

            returnEffectImages.add(new EffectImage(newImage, images1.get(i).getDelay(), null, null));
        }

        return returnEffectImages;
    }

    public static BufferedImage resizeImage(BufferedImage image, int newWidth, int newHeight, PivotData pivotData,
                                            Frame frame) {
        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphicsObject = newImage.createGraphics();
        int frameLeft = pivotData.getPivotX() + frame.getLeft();
        int frameTop = pivotData.getPivotY() + frame.getTop();
        graphicsObject.drawImage(image, null, frameLeft, frameTop);

        return newImage;
    }
}