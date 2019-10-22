package com.gamemode.tkviewer.render;

import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.file_handlers.EpfFileHandler;
import com.gamemode.tkviewer.file_handlers.FrmFileHandler;
import com.gamemode.tkviewer.file_handlers.PalFileHandler;
import com.gamemode.tkviewer.file_handlers.TileTblFileHandler;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.Palette;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileRenderer implements Renderer {

    public static int ALPHA = 0x0;

    Map<Integer, BufferedImage> tiles;

    List<EpfFileHandler> tileEpfs;
    PalFileHandler tilePal;
    TileTblFileHandler tileTbl;
    FrmFileHandler tileFrm;
    public int manualPaletteIndex = 0;

    public TileRenderer() { this("tile", "tile", "tile"); }
    public TileRenderer(String epfPrefix) { this(epfPrefix, epfPrefix + ".pal", epfPrefix + ".tbl"); }

    public TileRenderer(String epfPrefix, String palName, String tblName) {
        DatFileHandler tileDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "tile.dat");

        tiles = new HashMap<Integer, BufferedImage>();

        this.tileEpfs = FileUtils.createEpfsFromDats(epfPrefix);
        this.tilePal = new PalFileHandler(tileDat.getFile(palName));
        this.tileTbl = new TileTblFileHandler(tileDat.getFile(tblName));
    }

    public TileRenderer(List<EpfFileHandler> tileEpfs, PalFileHandler tilePal, TileTblFileHandler tileTbl) {
        tiles = new HashMap<Integer, BufferedImage>();

        this.tileEpfs = tileEpfs;
        this.tilePal = tilePal;
        this.tileTbl = tileTbl;
    }

    public TileRenderer(List<EpfFileHandler> tileEpfs, PalFileHandler tilePal, FrmFileHandler tileFrm) {
        tiles = new HashMap<Integer, BufferedImage>();

        this.tileEpfs = tileEpfs;
        this.tilePal = tilePal;
        this.tileFrm = tileFrm;
    }

    public TileRenderer(List<EpfFileHandler> tileEpfs, PalFileHandler tilePal, int manualPaletteIndex) {
        tiles = new HashMap<Integer, BufferedImage>();

        this.tileEpfs = tileEpfs;
        this.tilePal = tilePal;
        this.manualPaletteIndex = manualPaletteIndex;
    }

    public BufferedImage renderTile(int tileIndex) {
        // Return Tile if cached.
        if (tiles.containsKey(tileIndex)) {
            return tiles.get(tileIndex);
        }

        int epfIndex = 0;

        int frameCount = 0;
        for (int i = 0; i < tileEpfs.size(); i++) {
            if (tileIndex < (frameCount + this.tileEpfs.get(i).frameCount)) {
                epfIndex = i;
                break;
            }

            frameCount += this.tileEpfs.get(i).frameCount;
        }

        Frame frame = this.tileEpfs.get(epfIndex).getFrame(tileIndex - frameCount);
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
        if (this.isFrmHandled()) {
            paletteIndex = this.tileFrm.paletteIndices.get(tileIndex);
        } else if (this.tileTbl != null) {
            paletteIndex = this.tileTbl.paletteIndices.get(tileIndex).getPaletteIndex();
        }
        if (paletteIndex > this.tilePal.paletteCount) {
            paletteIndex = 0;
        }
        Palette palette = this.tilePal.palettes.get(paletteIndex);
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

        this.tiles.put(tileIndex, image);
        return image;
    }

    private boolean isFrmHandled() {
        return (this.tileFrm != null);
    }

    private boolean isTblHandled() { return (this.tileTbl != null); }

    @Override
    public int getCount(boolean useEpfCount) {
        // Return FRM count if used, or TBL count, lastly EPF count (overrideable)
        int output = 0;

        if (this.isFrmHandled() && !useEpfCount) {
            output = (int)this.tileFrm.effectCount;
        } else if (this.isTblHandled() && !useEpfCount) {
            output = (int)this.tileTbl.tileCount;
        } else {
            for (EpfFileHandler epf : this.tileEpfs) {
                output += epf.frameCount;
            }
        }

        return output;
    }

    @Override
    public int getCount() {
        // Return FRM count if used, else TBL count
        return getCount(false);
    }

    @Override
    public Image[] getFrames(int index) {
        Image[] frames = new Image[1];
        frames[0] = this.renderTile(index);

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
        Frame frame = FileUtils.getFrameFromEpfs(index, this.tileEpfs);
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
        for (EpfFileHandler epf : tileEpfs) {
            epf.close();
        }
        if (tilePal != null) {
            tilePal.close();
        }
        if (tileTbl != null) {
            tileTbl.close();
        }
        if (tileFrm != null) {
            tileFrm.close();
        }
    }
}
