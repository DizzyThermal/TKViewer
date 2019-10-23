package com.gamemode.tkviewer;

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
public class BekSpear {
    public static void main(String[] args) {
        // Create PartRenderers
        PartRenderer bodyRenderer = new PartRenderer("Body");
        PartRenderer spearRenderer = new PartRenderer("Spear");
        PartRenderer faceRenderer = new PartRenderer("Face");
        PartRenderer hairRenderer = new PartRenderer("Hair");
        PartRenderer faceDecRenderer = new PartRenderer("FaceDec");

        List<List<EffectImage>> effImages = new ArrayList<>();

        effImages.add(bodyRenderer.renderAnimation(48, 6));
        effImages.add(spearRenderer.renderAnimation(24, 2));
        effImages.add(faceRenderer.renderAnimation(7, 2));
        effImages.add(hairRenderer.renderAnimation(14, 2));
        effImages.add(faceDecRenderer.renderAnimation(1, 2));

        aggregateAnimations(effImages, "C:\\Users\\Reid\\Desktop\\test.gif");
    }

    public static void aggregateAnimations (List < List < EffectImage >> effImages, String outputFilePath){
        List<Frame> allFrames = new ArrayList<>();
        for (List<EffectImage> subListImages : effImages) {
            allFrames.addAll(subListImages.stream().map(EffectImage::getFrame).collect(Collectors.toList()));
        }
        PivotData pivotData = RenderUtils.getPivotData(allFrames);
        int maxWidth = pivotData.getCanvasWidth();
        int maxHeight = pivotData.getCanvasHeight();

        // Correct Images according to maxWidth and maxHeight
        for (int i = 0; i < effImages.get(0).size(); i++) {
            for (int j = 0; j < effImages.size(); j++) {
                EffectImage effImage = effImages.get(j).get(i);
                effImage.setImage(resizeImage(effImage.getImage(), maxWidth, maxHeight, pivotData,
                        effImage.getFrame(), effImage.getPivotData()));
            }
        }

        List<EffectImage> mergedImages = effImages.get(0);
        for (int i = 1; i < effImages.size(); i++) {
            mergedImages = mergeEffectImages(mergedImages, effImages.get(i));
        }
        FileUtils.exportGifFromImages(mergedImages, outputFilePath);
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
                                            Frame frame, PivotData framePivotData) {
        BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphicsObject = newImage.createGraphics();
        int frameLeft = (pivotData.getPivotX() - framePivotData.getPivotX());
        int frameTop = (pivotData.getPivotY() - framePivotData.getPivotY());
        graphicsObject.drawImage(image, null, frameLeft, frameTop);

        return newImage;
    }
}