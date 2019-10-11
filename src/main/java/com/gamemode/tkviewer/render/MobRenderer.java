package com.gamemode.tkviewer.render;

import com.gamemode.tkviewer.file_handlers.DnaFileHandler;
import com.gamemode.tkviewer.file_handlers.EpfFileHandler;
import com.gamemode.tkviewer.file_handlers.PalFileHandler;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.MobChunk;
import com.gamemode.tkviewer.resources.Palette;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobRenderer implements Renderer {

    public static int ALPHA = 0x0;

    Map<Integer, BufferedImage> mobs;

    public List<EpfFileHandler> mobEpfs;
    public PalFileHandler mobPal;
    public DnaFileHandler mobDna;
    public int manualPaletteIndex = 0;

    public MobRenderer(List<EpfFileHandler> mobEpfs, PalFileHandler mobPal, DnaFileHandler mobDna) {
        mobs = new HashMap<Integer, BufferedImage>();

        this.mobEpfs = mobEpfs;
        this.mobPal = mobPal;
        this.mobDna = mobDna;
    }

    public MobRenderer(List<EpfFileHandler> mobEpfs, PalFileHandler mobPal, int manualPaletteIndex) {
        mobs = new HashMap<Integer, BufferedImage>();

        this.mobEpfs = mobEpfs;
        this.mobPal = mobPal;
        this.manualPaletteIndex = manualPaletteIndex;
    }

    public BufferedImage renderMob(int mobIndex) {
        // Return Mob if cached.
        if (mobs.containsKey(mobIndex)) {
            return mobs.get(mobIndex);
        }

        int epfIndex = 0;

        int frameCount = 0;
        for (int i = 0; i < mobEpfs.size(); i++) {
            if ((mobIndex) < (frameCount + this.mobEpfs.get(i).frameCount)) {
                epfIndex = i;
                break;
            }

            frameCount += this.mobEpfs.get(i).frameCount;
        }

        Frame frame = this.mobEpfs.get(epfIndex).getFrame(mobIndex - frameCount);
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
        int paletteIndex = this.manualPaletteIndex;
        if (this.mobDna != null) {
            paletteIndex = (int)this.mobDna.mobs.get(mobIndex).getPaletteId();
        }
        Palette palette = this.mobPal.palettes.get(paletteIndex);
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

        this.mobs.put(mobIndex, image);
        return image;
    }

    @Override
    public int getCount(boolean useEpfCount) {
        int output = 0;

        if (!useEpfCount) {
            output = (int)this.mobDna.mobCount;
        } else {
            for (EpfFileHandler epf : this.mobEpfs) {
                output += epf.frameCount;
            }
        }

        return output;
    }

    @Override
    public int getCount() {
        return getCount(false);
    }

    @Override
    public Image[] getFrames(int index) {
        int imageCount = this.mobDna.mobs.get(index).getChunkCount();
        for (int i = 0; i < this.mobDna.mobs.get(index).getChunkCount(); i++) {
            MobChunk chunk = this.mobDna.mobs.get(index).getChunks().get(i);
            imageCount += chunk.getBlockCount();
        }
        Image[] frames = new Image[imageCount];
        for (int i = 0; i < imageCount; i++) {
            frames[i] = this.renderMob(index + i);
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
        Frame frame = FileUtils.getFrameFromEpfs(index, this.mobEpfs);
        stringBuilder.append("<html>");
        stringBuilder.append("Frame Info:<br>");
        stringBuilder.append("  Left: " + frame.getLeft() + "<br>");
        stringBuilder.append("  Top: " + frame.getTop() + "<br>");
        stringBuilder.append("  Right: " + frame.getRight() + "<br>");
        stringBuilder.append("  Bottom: " + frame.getBottom() + "<br>");
        stringBuilder.append("</html>");

        return stringBuilder.toString();
    }
}
