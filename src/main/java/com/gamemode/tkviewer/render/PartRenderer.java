package com.gamemode.tkviewer.render;

import com.gamemode.tkviewer.file_handlers.DscFileHandler;
import com.gamemode.tkviewer.file_handlers.EpfFileHandler;
import com.gamemode.tkviewer.file_handlers.PalFileHandler;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.Palette;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;

import java.awt.*;
import java.awt.image.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PartRenderer implements Renderer {

    public static int ALPHA = 0x0;

    Map<Integer, BufferedImage> parts;

    public List<EpfFileHandler> partEpfs;
    public PalFileHandler partPal;
    public DscFileHandler partDsc;
    public int manualPaletteIndex = 0;

    public PartRenderer(List<EpfFileHandler> partEpfs, PalFileHandler partPal, DscFileHandler partDsc) {
        parts = new HashMap<Integer, BufferedImage>();

        this.partEpfs = partEpfs;
        this.partPal = partPal;
        this.partDsc = partDsc;
    }

    public PartRenderer(List<EpfFileHandler> partEpfs, PalFileHandler partPal, int manualPaletteIndex) {
        parts = new HashMap<Integer, BufferedImage>();

        this.partEpfs = partEpfs;
        this.partPal = partPal;
        this.manualPaletteIndex = manualPaletteIndex;
    }

    public BufferedImage renderPart(int partIndex, int frameIndex, int frameOffset) {
        // Return Part if cached.
        if (parts.containsKey(frameIndex + frameOffset)) {
            return parts.get(frameIndex + frameOffset);
        }

        int epfIndex = 0;

        int frameCount = 0;
        for (int i = 0; i < partEpfs.size(); i++) {
            if ((frameIndex + frameOffset) < (frameCount + this.partEpfs.get(i).frameCount)) {
                epfIndex = i;
                break;
            }

            frameCount += this.partEpfs.get(i).frameCount;
        }

        Frame frame = this.partEpfs.get(epfIndex).getFrame(frameIndex + frameOffset - frameCount);
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
        if (this.partDsc != null) {
            paletteIndex = (int)this.partDsc.parts.get(partIndex).getPaletteId();
        }
        Palette palette = this.partPal.palettes.get(paletteIndex);
        IndexColorModel icm = new IndexColorModel(
                8,
                256,
                palette.getRedBytes(),
                palette.getGreenBytes(),
                palette.getBlueBytes(),
                Transparency.TRANSLUCENT);

        DataBufferByte buffer = new DataBufferByte(frame.getRawData().array(), frame.getRawData().capacity());
        WritableRaster raster = Raster.createPackedRaster(buffer, width, height, 8, null);

        image = new BufferedImage(icm, raster, icm.isAlphaPremultiplied(), null);
        for (int y = 0; y < image.getHeight(); ++y) {
            for (int x = 0; x < image.getWidth(); ++x) {
                if (!frame.getStencil().rows.get(y)[x]) {
                    image.setRGB(x, y, ALPHA);
                }
            }
        }

        this.parts.put(frameIndex + frameOffset, image);
        return image;
    }

    @Override
    public int getCount() {
        return (int)this.partDsc.partCount;
    }

    @Override
    public Image[] getFrames(int index) {
        Image[] frames = new Image[(int)this.partDsc.parts.get(index).getFrameCount()];
        for (int i = 0; i < this.partDsc.parts.get(index).getFrameCount(); i++) {
            frames[i] = this.renderPart(index, (int)this.partDsc.parts.get(index).getFrameIndex(), i);
        }

        return frames;
    }

    @Override
    public int getFrameIndex(int index, int offset) {
        return (int)this.partDsc.parts.get(index).getFrameIndex() + offset;
    }

    @Override
    public String getInfo(int index) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<html>");

        // Frame Info
        Frame frame = FileUtils.getFrameFromEpfs(index, this.partEpfs);
        stringBuilder.append("Frame Info:<br>");
        stringBuilder.append("&nbsp;&nbsp;Dimensions: " + frame.getWidth() + "x" + frame.getHeight() + "<br>");
        stringBuilder.append("&nbsp;&nbsp;LTRB: (" + frame.getLeft() + ", " + frame.getTop() + ", " + frame.getRight() + ", " + frame.getBottom() + ")<br>");
        stringBuilder.append("&nbsp;&nbsp;PixelDataOffset: " + frame.getPixelDataOffset() + "<br>");
        stringBuilder.append("&nbsp;&nbsp;StencilDataOffset: " + frame.getStencilDataOffset() + "<br>");

        stringBuilder.append("</html>");
        return stringBuilder.toString();
    }
}
