package com.gamemode.tkviewer.render;

import com.gamemode.tkviewer.file_handlers.*;
import com.gamemode.tkviewer.resources.*;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.utilities.FileUtils;

import java.awt.*;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EffectRenderer implements Renderer {

    public static int ALPHA = 0x0;

    Map<Integer, BufferedImage> effects;
    TileRenderer tileRenderer;

    List<EpfFileHandler> effectEpfs;
    PalFileHandler effectPal;
    EfxTblFileHandler effectEfxTbl;
    FrmFileHandler effectFrm;

    public EffectRenderer() {
        DatFileHandler efxDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "efx.dat");

        this.effectEpfs = FileUtils.createEpfsFromDats("EFFECT", "efx");
        this.effectPal = new PalFileHandler(efxDat.getFile("EFFECT.PAL"));
        this.effectEfxTbl = new EfxTblFileHandler(efxDat.getFile("effect.tbl"));
        this.effectFrm = new FrmFileHandler(efxDat.getFile("EFFECT.FRM"));
    }

    public EffectRenderer(List<EpfFileHandler> effectEpfs, PalFileHandler effectPal, EfxTblFileHandler effectEfxTbl, FrmFileHandler effectFrm) {
        this.effectEpfs = effectEpfs;
        this.effectPal = effectPal;
        this.effectEfxTbl = effectEfxTbl;
        this.effectFrm = effectFrm;

        this.tileRenderer = new TileRenderer(effectEpfs, effectPal, effectFrm);
    }

    public List<EffectImage> renderEffect(int effectIndex) {
        // Determine Frame Range for Effect
        List<EffectFrame> effectFrames = this.effectEfxTbl.effects.get(effectIndex).getEffectFrames();
        int effectFrameCount = effectFrames.size();

        // Determine Canvas Size
        int l, t, r, b;
        l = t = r = b = 0;
        for (int i = 0; i < effectFrameCount; i++) {
            EffectFrame effectFrame = effectFrames.get(i);
            Frame frame = FileUtils.getFrameFromEpfs(effectFrame.getFrameIndex(), this.effectEpfs);
            if (frame == null) {
                continue;
            }
//            if (frame)
            if (frame.getLeft() < l) {
                l = frame.getLeft();
            }
            if (frame.getTop() < t) {
                t = frame.getTop();
            }
            if (frame.getRight() > r) {
                r = frame.getRight();
            }
            if (frame.getBottom() > b) {
                b = frame.getBottom();
            }
        }

        int effectWidth = r-l;
        int effectHeight = b-t;

        List<EffectImage> images = new ArrayList<EffectImage>();
        for (int i = 0; i < effectFrameCount; i++) {
            BufferedImage frameImage = new BufferedImage(effectWidth, effectHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphicsObject = frameImage.createGraphics();
            EffectFrame effectFrame = effectFrames.get(i);

            BufferedImage tile = this.renderEffectImage(effectFrame.getFrameIndex(), this.effectFrm.paletteIndices.get(effectFrame.getFrameIndex()));
            Frame frame = FileUtils.getFrameFromEpfs(effectFrame.getFrameIndex(), effectEpfs);
            if (frame == null) {
                continue;
            }

            graphicsObject.drawImage(
                    tile,
                    null,
                    (frame.getLeft() - l),
                    (frame.getTop() - t));

            images.add(new EffectImage(frameImage, effectFrame.getFrameDelay()));
        }

        return images;
    }

    public BufferedImage renderEffectImage(int tileIndex, int paletteIndex) {
        int epfIndex = 0;

        int frameCount = 0;
        for (int i = 0; i < effectEpfs.size(); i++) {
            if (tileIndex < (frameCount + this.effectEpfs.get(i).frameCount)) {
                epfIndex = i;
                break;
            }

            frameCount += this.effectEpfs.get(i).frameCount;
        }

        Frame frame = this.effectEpfs.get(epfIndex).getFrame(tileIndex - frameCount);
        int width = frame.getWidth();
        int height = frame.getHeight();

        BufferedImage image = null;
        if (width == 0 || height == 0) {
            // Send back a TILE_DIM black square
            image = new BufferedImage(Resources.TILE_DIM, Resources.TILE_DIM, BufferedImage.TYPE_INT_ARGB);
            int[] pixels = new int[Resources.TILE_DIM * Resources.TILE_DIM];
            for (int i = 0; i < (Resources.TILE_DIM * Resources.TILE_DIM); i++) {
                pixels[i] = ALPHA;
            }

            image.setRGB(0, 0, Resources.TILE_DIM, Resources.TILE_DIM, pixels, 0, Resources.TILE_DIM);
            return image;
        }
        // Else
        if (paletteIndex < 0 || paletteIndex > (this.effectPal.paletteCount - 1)) {
                paletteIndex = 0;
        }
        Palette palette = this.effectPal.palettes.get(paletteIndex);
        IndexColorModel icm = new IndexColorModel(
                8,
                256,
                palette.getRedBytes(),
                palette.getGreenBytes(),
                palette.getBlueBytes(),
                Transparency.TRANSLUCENT);

        DataBufferByte buffer = new DataBufferByte(frame.getRawPixelData().array(), frame.getRawPixelData().capacity());
        WritableRaster raster = Raster.createPackedRaster(buffer, width, height, 8, null);

        image = new BufferedImage(icm, raster, icm.isAlphaPremultiplied(), null);
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                if (!frame.getStencil().rows.get(y)[x]) {
                    image.setRGB(x, y, ALPHA);
                }
            }
        }

        return image;
    }

    @Override
    public int getCount(boolean useEpfCount) {
        return (int)this.effectEfxTbl.effectCount;
    }

    @Override
    public int getCount() {
        // Return FRM count if used, else TBL count
        return getCount(false);
    }

    @Override
    public Image[] getFrames(int index) {
        List<EffectImage> effects = this.renderEffect(index);
        Image[] frames = new Image[effects.size()];
        for (int i = 0; i < frames.length; i++) {
            frames[i] = effects.get(i).getImage();
        }

        return frames;
    }

    @Override
    public int getFrameIndex(int index, int offset) {
        return index;
    }

    @Override
    public String getInfo(int index) {
        StringBuilder stringBuilder = new StringBuilder();

        // Frame Info
        Frame frame = FileUtils.getFrameFromEpfs(index, this.effectEpfs);
        stringBuilder.append("<html>");
        stringBuilder.append("Frame Info:<br>");
        stringBuilder.append("  Left: " + frame.getLeft() + "<br>");
        stringBuilder.append("  Top: " + frame.getTop() + "<br>");
        stringBuilder.append("  Right: " + frame.getRight() + "<br>");
        stringBuilder.append("  Bottom: " + frame.getBottom() + "<br>");
        stringBuilder.append("</html>");

        return stringBuilder.toString();
    }

    @Override
    public void dispose() {
        for (EpfFileHandler epf : effectEpfs) {
            epf.close();
        }
        if (effectPal != null) {
            effectPal.close();
        }
        if (effectEfxTbl != null) {
            effectEfxTbl.close();
        }
        if (effectFrm != null) {
            effectFrm.close();
        }
    }
}
