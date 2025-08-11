package com.gamemode.tkviewer.render;

import com.gamemode.tkviewer.*;
import com.gamemode.tkviewer.Frame;
import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.file_handlers.DnaFileHandler;
import com.gamemode.tkviewer.file_handlers.EpfFileHandler;
import com.gamemode.tkviewer.file_handlers.PalFileHandler;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MobRenderer implements Renderer {

    public static int ALPHA = 0x0;

    public static enum ANIMATIONS {
        DEATH,
        FACE_UP,
        FACE_RIGHT,
        FACE_DOWN,
        FACE_LEFT,
        WALK_UP,
        WALK_RIGHT,
        WALK_DOWN,
        WALK_LEFT,
        HIT_UP,
        HIT_RIGHT,
        HIT_DOWN,
        HIT_LEFT,
        ATTACK_UP,
        ATTACK_RIGHT,
        ATTACK_DOWN,
        ATTACK_LEFT
    }

    Map<Integer, BufferedImage> mobs;

    public List<EpfFileHandler> mobEpfs;
    public PalFileHandler mobPal;
    public DnaFileHandler mobDna;
    public int manualPaletteIndex = 0;

    public MobRenderer() {
        DatFileHandler monDat = new DatFileHandler(Resources.getNtkDataDirectory() + File.separator + "mon.dat");

        mobs = new HashMap<Integer, BufferedImage>();

        this.mobEpfs = FileUtils.createEpfsFromDats("mon", false);
        this.mobPal = new PalFileHandler(monDat.getFile("monster.pal"));
        this.mobDna = new DnaFileHandler(monDat.getFile("monster.dna"));
    }

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

    public BufferedImage renderMob(int tileIndex, int paletteIndex) {
        int epfIndex = 0;

        int frameCount = 0;
        for (int i = 0; i < mobEpfs.size(); i++) {
            if (tileIndex < (frameCount + this.mobEpfs.get(i).frameCount)) {
                epfIndex = i;
                break;
            }

            frameCount += this.mobEpfs.get(i).frameCount;
        }

        Frame frame = this.mobEpfs.get(epfIndex).getFrame(tileIndex - frameCount);
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
        if (paletteIndex < 0 || paletteIndex > (this.mobPal.paletteCount - 1)) {
            paletteIndex = 0;
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

        return image;
    }

    public int[] getGlobalCanvasSize(int mobIndex) {
        // Returns [l, t, r, b] for all chunks in mob
        Mob mob = this.mobDna.mobs.get(mobIndex);

        // Determine Canvas Size
        int l, t, r, b;
        l = t = r = b = 0;
        for (int i = 0; i < mob.getChunkCount(); i++) {
            MobChunk chunk = mob.getChunks().get(i);
            int frameCount = chunk.getBlockCount();
            for (int j = 0; j < frameCount; j++) {
                MobBlock block = chunk.getBlocks().get(j);
                int frameIndex = (int) (mob.getFrameIndex() + block.getFrameOffset());

                Frame frame = FileUtils.getFrameFromEpfs(frameIndex, this.mobEpfs);
                if (frame == null) {
                    continue;
                }
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
        }

        return new int[]{l, t, r, b};
    }

    public List<EffectImage> renderAnimation(int mobIndex, ANIMATIONS animation, int paletteIndex) {
        return renderAnimation(mobIndex, animation.ordinal(), paletteIndex);
    }

    public List<EffectImage> renderAnimation(int mobIndex, int chunkIndex, int paletteIndex) {
        Mob mob = this.mobDna.mobs.get(mobIndex);
        MobChunk chunk = mob.getChunks().get(chunkIndex);

        int frameCount = chunk.getBlockCount();

        int[] dims = getGlobalCanvasSize(mobIndex);
        int l = dims[0];
        int t = dims[1];
        int r = dims[2];
        int b = dims[3];

        int effectWidth = r-l;
        int effectHeight = b-t;

        List<EffectImage> images = new ArrayList<EffectImage>();
        for (int i = 0; i < frameCount; i++) {
            BufferedImage frameImage = new BufferedImage(effectWidth, effectHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphicsObject = frameImage.createGraphics();
            MobBlock block = chunk.getBlocks().get(i);
            int frameIndex = (int)(mob.getFrameIndex() + block.getFrameOffset());

            BufferedImage tile = this.renderMob(frameIndex, paletteIndex >= 0 ? paletteIndex : mob.getPaletteId());
            Frame frame = FileUtils.getFrameFromEpfs(frameIndex, mobEpfs);
            if (frame == null) {
                continue;
            }

            float alpha = 1.0f - ((float) block.getTransparency() / 255.0f);
            graphicsObject.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
            graphicsObject.drawImage(
                    tile,
                    null,
                    (frame.getLeft() - l),
                    (frame.getTop() - t));

            images.add(new EffectImage(frameImage, block.getDuration(), null, null));
        }

        return images;
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
    public long getPaletteCount() {
        return this.mobPal.paletteCount;
    }

    @Override
    public Image[] getFrames(int index, int paletteIndex) {
        Mob mob = this.mobDna.mobs.get(index);
        int frameIndex = (int)mob.getFrameIndex();
        int maxFrameOffset = 0;
        for (MobChunk chunk : mob.getChunks()) {
            for (MobBlock block : chunk.getBlocks()) {
                int frameOffset = block.getFrameOffset();
                if (frameOffset > maxFrameOffset) {
                    maxFrameOffset = frameOffset;
                }
            }
        }
        int imageCount = maxFrameOffset + 1;
        Image[] frames = new Image[imageCount];
        for (int i = 0; i < imageCount; i++) {
            frames[i] = this.renderMob(frameIndex + i, paletteIndex >= 0 ? paletteIndex : mob.getPaletteId());
        }

        return frames;
    }

    @Override
    public int getFrameIndex(int index, int offset) {
        return (int)this.mobDna.mobs.get(index).getFrameIndex() + offset;
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

    @Override
    public void dispose() {
        for (EpfFileHandler epf : mobEpfs) {
            epf.close();
        }
        if (mobPal != null) {
            mobPal.close();
        }
        if (mobDna != null) {
            mobDna.close();
        }
    }
}
