package com.gamemode.tkviewer.render;

import com.gamemode.tkviewer.*;
import com.gamemode.tkviewer.Frame;
import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.file_handlers.DscFileHandler;
import com.gamemode.tkviewer.file_handlers.EpfFileHandler;
import com.gamemode.tkviewer.file_handlers.PalFileHandler;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;
import com.gamemode.tkviewer.utilities.RenderUtils;

import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.util.*;
import java.util.List;


public class PartRenderer implements Renderer {

    public static int ALPHA = 0x0;

    public static enum PART_RENDERER_TYPE {
        BODIES,
        BOWS,
        COATS,
        FACES,
        FANS,
        HAIR,
        HELMETS,
        MANTLES,
        SPEARS,
        SHOES,
        SHIELDS,
        SWORDS
    }

    public static enum BODY_ANIMATIONS {
        WALK_UP,                    //  0
        WALK_RIGHT,                 //  1
        WALK_DOWN,                  //  2
        WALK_LEFT,                  //  3
        WALK_UP_WITH_WEAPON,        //  4
        WALK_RIGHT_WITH_WEAPON,     //  5
        WALK_DOWN_WITH_WEAPON,      //  6
        WALK_LEFT_WITH_WEAPON,      //  7
        RIDE_MOUNT_UP,              //  8
        RIDE_MOUNT_RIGHT,           //  9
        RIDE_MOUNT_DOWN,            // 10
        RIDE_MOUNT_LEFT,            // 11
        SWING_UP_1H,                // 12
        SWING_RIGHT_1H,             // 13
        SWING_DOWN_1H,              // 14
        SWING_LEFT_1H,              // 15
        SWING_UP_2H,                // 16
        SWING_RIGHT_2H,             // 17
        SWING_DOWN_2H,              // 18
        SWING_LEFT_2H,              // 19
        SHOOT_UP,                   // 20
        SHOOT_RIGHT,                // 21
        SHOOT_DOWN,                 // 22
        SHOOT_LEFT,                 // 23
        KNEEL_UP,                   // 24
        KNEEL_RIGHT,                // 25
        KNEEL_DOWN,                 // 26
        KNEEL_LEFT,                 // 27
        SUMMON_UP,                  // 28
        SUMMON_RIGHT,               // 29
        SUMMON_DOWN,                // 30
        SUMMON_LEFT,                // 31
        SCRATCH_CHIN,               // 32
        LEAN_UP,                    // 33
        LEAN_RIGHT,                 // 34
        LEAN_DOWN,                  // 35
        LEAN_LEFT,                  // 36
        TRIUMPH,                    // 37
        UNKNOWN_EMOTE_1,            // 38
        UNKNOWN_EMOTE_2,            // 39
        UNKNOWN_EMOTE_3,            // 40
        UNKNOWN_EMOTE_4,            // 41
        UNKNOWN_EMOTE_5,            // 42
        UNKNOWN_EMOTE_6,            // 43
        UNKNOWN_EMOTE_7,            // 44
        UNKNOWN_EMOTE_8,            // 45
        UNKNOWN_EMOTE_9,            // 46
        SHRUG,                      // 47
        UNKNOWN_EMOTE_10,           // 48
        DANCE,                      // 49
        UNKNOWN_EMOTE_11,           // 50
        LEAN_UP_2,                  // 51
        LEAN_RIGHT_2,               // 52
        LEAN_DOWN_2,                // 53
        LEAN_LEFT_2,                // 54
        FACE_UP_2,                  // 55
        FACE_RIGHT_2,               // 56
        FACE_DOWN_2,                // 57
        FACE_LEFT_2,                // 58
        HOLD_KITE_UP,               // 59
        HOLD_KITE_RIGHT,            // 60
        HOLD_KITE_DOWN,             // 61
        HOLD_KITE_LEFT,             // 62
        SWING_UP_1H_2,              // 63
        SWING_RIGHT_1H_2,           // 64
        SWING_DOWN_1H_2,            // 65
        SWING_LEFT_1H_2             // 66
    }

    Map<Integer, BufferedImage> parts;

    public List<EpfFileHandler> partEpfs;
    public PalFileHandler partPal;
    public DscFileHandler partDsc;
    public int manualPaletteIndex = 0;

    public PartRenderer(String partName) {
        this(partName, new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "char.dat"), false);
    }

    public PartRenderer(String partName, String dataDirectory) {
        this(partName, new DatFileHandler(dataDirectory + File.separator + "char.dat", dataDirectory == Resources.BARAM_DATA_DIRECTORY), dataDirectory == Resources.BARAM_DATA_DIRECTORY);
    }

    public PartRenderer(String partName, DatFileHandler charDat, boolean isBaram) {
        parts = new HashMap<Integer, BufferedImage>();

        System.out.println("Creating EPFs from partName: " + partName);
        this.partEpfs = FileUtils.createEpfsFromDats(partName, isBaram);
        System.out.println("Creating PALs from partName: " + partName);
        this.partPal = new PalFileHandler(charDat.getFile(partName + ".pal"));
        System.out.println("Creating DSCs from partName: " + partName);
        this.partDsc = new DscFileHandler(charDat.getFile(partName + ".dsc"), isBaram);
    }

    public PartRenderer(List<EpfFileHandler> partEpfs, PalFileHandler partPal, DscFileHandler partDsc) {
        parts = new HashMap<Integer, BufferedImage>();

        this.partEpfs = partEpfs;
        this.partPal = partPal;
        this.partDsc = partDsc;
    }

    public PartRenderer(String tkDataDirectory, PART_RENDERER_TYPE rendererType) {
        parts = new HashMap<Integer, BufferedImage>();

        String epfPrefix = null;
        String palName = null;
        String dscName = null;

        switch (rendererType) {

            case BODIES:
                epfPrefix = "Body";
                palName = "Body.pal";
                dscName = "Body.dsc";
                break;

            case BOWS:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Bow.dsc";
                break;

            case COATS:
                epfPrefix = "Coat";
                palName = "Coat.pal";
                dscName = "Coat.dsc";
                break;

            case FACES:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Face.dsc";
                break;

            case FANS:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Bow.dsc";
                break;

            case HAIR:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Bow.dsc";
                break;

            case HELMETS:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Bow.dsc";
                break;

            case MANTLES:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Bow.dsc";
                break;

            case SPEARS:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Bow.dsc";
                break;

            case SHOES:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Bow.dsc";
                break;

            case SHIELDS:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Bow.dsc";
                break;

            case SWORDS:
                epfPrefix = "Bow";
                palName = "Bow.pal";
                dscName = "Bow.dsc";
                break;
        }
        this.partEpfs = FileUtils.createEpfsFromFiles(FileUtils.getEpfs(tkDataDirectory, Objects.requireNonNull(epfPrefix)));
        this.partPal = new PalFileHandler(tkDataDirectory + File.separator + Objects.requireNonNull(palName));
        this.partDsc = new DscFileHandler(tkDataDirectory + File.separator + Objects.requireNonNull(dscName), false);

    }

    public PartRenderer(List<EpfFileHandler> partEpfs, PalFileHandler partPal, int manualPaletteIndex) {
        parts = new HashMap<Integer, BufferedImage>();

        this.partEpfs = partEpfs;
        this.partPal = partPal;
        this.manualPaletteIndex = manualPaletteIndex;
    }

    public String getEpfNameForFrame( int frameOffset) {
        long frameIndex = this.partDsc.parts.get(frameOffset).getFrameIndex();

        int epfIndex = 0;

        int frameCount = 0;
        for (int i = 0; i < partEpfs.size(); i++) {
            if ((frameIndex + frameOffset) < (frameCount + this.partEpfs.get(i).frameCount)) {
                epfIndex = i;
                break;
            }

            frameCount += this.partEpfs.get(i).frameCount;
        }

        return this.partEpfs.get(epfIndex).filePath;
    }

    public Frame getFrame(int frameIndex, int frameOffset) {
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
        return frame;
    }

    public BufferedImage renderPart(int partIndex, int frameIndex, int frameOffset, int paletteIndex) {
        // Return Part if cached.
        if (parts.containsKey(frameIndex + frameOffset)) {
            return parts.get(frameIndex + frameOffset);
        }

        Frame frame = getFrame(frameIndex, frameOffset);

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

        Palette palette = this.partPal.palettes.get(paletteIndex);
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

        this.parts.put(frameIndex + frameOffset, image);
        return image;
    }

    public List<EffectImage> renderAnimation(int partIndex, BODY_ANIMATIONS animation) {
        return renderAnimation(partIndex, animation.ordinal());
    }

    public List<EffectImage> renderAnimation(int partIndex, int chunkIndex) {
        return renderAnimation(partIndex, chunkIndex, -1);
    }

    public List<EffectImage> renderAnimation(int partIndex, int chunkIndex, int manualPaletteIndex) {
        Part part = this.partDsc.parts.get(partIndex);
        PartChunk chunk = part.getChunks().get(chunkIndex);

        int frameCount = chunk.getBlocks().size();

        List<Frame> frames = new ArrayList<>();
        for (int i = 0; i < frameCount; i++) {
            PartBlock block = chunk.getBlocks().get(i);
            int frameIndex = (int) (part.getFrameIndex() + block.getFrameOffset());
            frames.add(FileUtils.getFrameFromEpfs(frameIndex, this.partEpfs));
        }

        PivotData pivotData = RenderUtils.getPivotData(frames);
        List<EffectImage> images = new ArrayList<EffectImage>();
        for (int i = 0; i < frameCount; i++) {
            if (pivotData.getCanvasWidth() <= 0 || pivotData.getCanvasHeight() <= 0) {
                continue;
            }

            BufferedImage canvasImage = new BufferedImage(pivotData.getCanvasWidth(), pivotData.getCanvasHeight(), BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphicsObject = canvasImage.createGraphics();
            PartBlock block = chunk.getBlocks().get(i);
            int frameIndex = (int) part.getFrameIndex();
            int frameOffset = block.getFrameOffset();

            int paletteIndex = manualPaletteIndex;
            if (paletteIndex < 0) {
                paletteIndex = (int) part.getPaletteId();
            }
            BufferedImage partImage = this.renderPart(partIndex, frameIndex, frameOffset, paletteIndex);
            Frame frame = FileUtils.getFrameFromEpfs(frameIndex + frameOffset, partEpfs);
            if (frame == null) {
                continue;
            }

            int frameLeft = pivotData.getPivotX() + frame.getLeft();
            int frameTop = pivotData.getPivotY() + frame.getTop();
            graphicsObject.drawImage(partImage, null, frameLeft, frameTop);

            int defaultDuration = 64 * 16; // (ms)
            images.add(new EffectImage(canvasImage, defaultDuration, pivotData, frame));
        }

        return images;
    }

    public Dimension getMaxDimensions(int frameOffset) {
        Dimension returnDim = new Dimension(0, 0);

        List<EpfFileHandler> epfs = this.partEpfs;
        for (int i = 0; i < epfs.size(); i++) {
            EpfFileHandler epf = epfs.get(i);
            for (int j = 0; j < epf.frameCount; j++) {
                Frame frame = epf.getFrame(j);

                if (frame.getWidth() > returnDim.getWidth()) {
                    returnDim.setSize(frame.getWidth(), returnDim.getHeight());
                }
                if (frame.getHeight() > returnDim.getHeight()) {
                    returnDim.setSize(returnDim.getWidth(), frame.getHeight());
                }
            }
        }

        return returnDim;
    }

    public Dimension getMaxDimensionsForOffset(int frameOffset) {
        Dimension returnDim = new Dimension(0, 0);

        for (int i = 0; i < this.partDsc.partCount; i++) {
            Part part = this.partDsc.parts.get(i);
            Frame frame = getFrame((int) part.getFrameIndex(), frameOffset);

            if (frame.getWidth() > returnDim.getWidth()) {
                returnDim.setSize(frame.getWidth(), returnDim.getHeight());
            }
            if (frame.getHeight() > returnDim.getHeight()) {
                returnDim.setSize(returnDim.getWidth(), frame.getHeight());
            }
        }

        return returnDim;
    }

    @Override
    public int getCount(boolean useEpfCount) {
        int output = 0;

        if (!useEpfCount) {
            output = (int) this.partDsc.partCount;
        } else {
            for (EpfFileHandler epf : this.partEpfs) {
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
        Image[] frames = new Image[(int) this.partDsc.parts.get(index).getFrameCount()];
        for (int i = 0; i < this.partDsc.parts.get(index).getFrameCount(); i++) {
            frames[i] = this.renderPart(index,
                    (int) this.partDsc.parts.get(index).getFrameIndex(),
                    i,
                    (int) this.partDsc.parts.get(index).getPaletteId());
        }

        return frames;
    }

    @Override
    public int getFrameIndex(int index, int offset) {
        return (int) this.partDsc.parts.get(index).getFrameIndex() + offset;
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

    @Override
    public void dispose() {
        for (EpfFileHandler epf : partEpfs) {
            epf.close();
        }
        if (partPal != null) {
            partPal.close();
        }
        if (partDsc != null) {
            partDsc.close();
        }
    }
}
