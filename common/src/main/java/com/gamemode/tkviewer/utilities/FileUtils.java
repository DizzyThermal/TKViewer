package com.gamemode.tkviewer.utilities;

import com.gamemode.tkviewer.EffectImage;
import com.gamemode.tkviewer.Frame;
import com.gamemode.tkviewer.SObject;
import com.gamemode.tkviewer.Tile;
import com.gamemode.tkviewer.file_handlers.*;
import com.gamemode.tkviewer.render.MapRenderer;
import com.gamemode.tkviewer.render.SObjRenderer;
import com.gamemode.tkviewer.render.TileRenderer;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.third_party.GifSequenceWriter;

import javax.imageio.*;
import javax.imageio.stream.*;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.*;
import java.io.FileWriter;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gamemode.tkviewer.resources.Resources.TILE_DIM;
import static com.gamemode.tkviewer.utilities.Utils.pad;

// Static File Utilities Class
public class FileUtils {

    public FileUtils() {
    }

    public static List<EpfFileHandler> createEpfsFromDats(String epfPrefix, boolean isBaram) {
        return createEpfsFromDats(epfPrefix, epfPrefix, "epf", isBaram);
    }

    public static List<EpfFileHandler> createEpfsFromDats(String epfPrefix, String datPrefix, boolean isBaram) {
        return createEpfsFromDats(epfPrefix, datPrefix, "epf", isBaram);
    }

    public static List<EpfFileHandler> createEpfsFromDats(String epfPrefix, String datPrefix, String extension, boolean isBaram) {
        List<EpfFileHandler> epfFileHandlers = new ArrayList<EpfFileHandler>();

        List<DatFileHandler> datFileHandlers = getDatFileHandlers(datPrefix, isBaram);
        for (int i = 0; i < datFileHandlers.size(); i++) {
            ByteBuffer b = datFileHandlers.get(i).getFile(epfPrefix + i + "." + extension);
            EpfFileHandler epf = new EpfFileHandler(b, epfPrefix + i + "." + extension);
            epfFileHandlers.add(epf);
        }

        return epfFileHandlers;
    }

    public static List<EpfFileHandler> createEpfsFromFiles(File[] epfFiles) {
        List<EpfFileHandler> epfFileHandlers = new ArrayList<EpfFileHandler>();
        for (int i = 0; i < epfFiles.length; i++) {
            epfFileHandlers.add(new EpfFileHandler(epfFiles[i]));
        }

        return epfFileHandlers;
    }

    // Recursively deletes a directory
    public static boolean deleteDirectory(String directoryPath) {
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(new File(directoryPath));
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return false;
        }

        return true;
    }

    public static void exportGifFromImages(List<EffectImage> images, String outputFilePath) {
        try {
            ImageOutputStream output = new FileImageOutputStream(new File(outputFilePath));
            GifSequenceWriter gifWriter = new GifSequenceWriter(output, BufferedImage.TYPE_INT_ARGB, images.get(0).getDelay(), true);
            for (int i = 0; i < images.size(); i++) {
                gifWriter.writeToSequence(images.get(i).getImage(), images.get(i).getDelay());
            }

            gifWriter.close();
            output.close();
        } catch (IOException ioe) {
            System.out.println("Error writing");
        }
    }

    public static File[] getEpfs(String dataDirectory, String prefix) {
        File[] files = new File(dataDirectory).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(prefix + "\\d+\\.[Ee][Pp][Ff]");
            }
        });
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName(), prefix);
                int n2 = extractNumber(o2.getName(), prefix);
                return n1 - n2;
            }

            private int extractNumber(String name, String prefix) {
                int i = 0;
                try {
                    int s = name.indexOf(prefix.substring(prefix.length() - 1)) + 1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch (Exception e) {
                    i = 0;
                }
                return i;
            }
        });

        return files;
    }

    public static List<DatFileHandler> getDatFileHandlers(String prefix, boolean isBaram) {
        List<DatFileHandler> datFileHandlers = new ArrayList<DatFileHandler>();

        File[] datFiles = getDats(prefix, isBaram);
        for (File datFile : datFiles) {
            datFileHandlers.add(new DatFileHandler(datFile, isBaram));
        }

        return datFileHandlers;
    }

    public static File[] getDats(String prefix, boolean isBaram) {
        if (isBaram) {
            return getDats(Resources.BARAM_DATA_DIRECTORY, prefix);
        }
        return getDats(Resources.getNtkDataDirectory(), prefix);
    }

    public static File[] getDats(String dataDirectory, String prefix) {
        File[] dats = new File(dataDirectory).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches("(?i)" + prefix + "\\d+\\.dat");
            }
        });
        Arrays.sort(dats, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                int n1 = extractNumber(o1.getName(), prefix);
                int n2 = extractNumber(o2.getName(), prefix);
                return n1 - n2;
            }

            private int extractNumber(String name, String prefix) {
                int i = 0;
                try {
                    int s = name.indexOf(prefix.substring(prefix.length() - 1)) + 1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch (Exception e) {
                    i = 0;
                }
                return i;
            }
        });

        return dats;
    }

    // Grab files named: Face#.epf and sort by number
    public static File[] getFaceEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Face");
    }

    // Grab files named: Hair#.epf and sort by number
    public static File[] getHairEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Hair");
    }

    // Grab files named: tile#.epf and sort by number
    public static File[] getTileEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "tile");
    }

    // Grab files named: tilec#.epf and sort by number
    public static File[] getTileCEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "tilec");
    }

    public static Frame getFrameFromEpfs(int index, List<EpfFileHandler> epfFiles) {
        int epfIndex = -1;

        int frameCount = 0;
        for (int i = 0; i < epfFiles.size(); i++) {
            if (index < (frameCount + epfFiles.get(i).frameCount)) {
                epfIndex = i;
                break;
            }

            frameCount += epfFiles.get(i).frameCount;
        }

        Frame returnFrame = null;
        if (epfIndex != -1) {
            int relativeFrameIndex = index - frameCount;
            try {
                returnFrame = epfFiles.get(epfIndex).getFrame(relativeFrameIndex);
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                aioobe.printStackTrace();
            }
        }

        return returnFrame;
    }

    public static void writeBufferedImageToFile(BufferedImage image, String outputPath) {
        try {
            ImageIO.write(image, "png", new File(outputPath));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void writeImageGridToFile(List<List<BufferedImage>> imageGrid, String outputPath) {
        try {
            int sheetWidth = 0;
            int sheetHeight = 0;

            for (int i = 0; i < imageGrid.size(); i++) {
                List<BufferedImage> imageList = imageGrid.get(i);
                if (imageList.size() == 0) {
                    continue;
                }
                Image first = (Image)imageList.get(0);
                int width = first.getWidth(null);
                int height = first.getHeight(null);
                sheetWidth = Math.max(sheetWidth, imageList.size() * width);
                sheetHeight += height;
            }

            BufferedImage sheet = new BufferedImage(sheetWidth, sheetHeight, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2d = sheet.createGraphics();

            int y = 0;
            for (int i = 0; i < imageGrid.size(); i++) {
                List<BufferedImage> imageList = imageGrid.get(i);
                if (imageList.size() == 0) {
                    continue;
                }
                Image first = (Image)imageList.get(0);

                // All images within one chunk are to be same size
                int width = first.getWidth(null);
                int height = first.getHeight(null);
                for (int j = 0; j < imageList.size(); j++) {
                    g2d.drawImage(imageList.get(j), width * j, y, null);
                }
                y += height;
            }
            g2d.dispose();

            ImageIO.write(sheet, "png", new File(outputPath));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void generateGroundTileImages(Path outputDirectory) {
        if (!outputDirectory.toFile().exists()) {
            outputDirectory.toFile().mkdirs();
        }

        // Note: "tile" is for Ground tiles, "tilec" is for Static Object tiles
        TileRenderer tileRenderer = new TileRenderer("tile");

        // TileRenderer gets its count from the Tile.tbl file
        for (int i = 0; i < tileRenderer.getCount(); i++) {
            File tileFile = Paths.get(outputDirectory.toString(), pad(i, 5) + ".png").toFile();
            if (!tileFile.exists()) {
                try {
                    ImageIO.write(tileRenderer.renderTile(i), "png", tileFile);
                } catch (IOException ioe) {
                    System.out.println("Error writing");
                }
            }
        }
    }

    public static void generateStaticObjectTileImages(Path outputDirectory) {
        if (!outputDirectory.toFile().exists()) {
            outputDirectory.toFile().mkdirs();
        }

        // Note: "tile" is for Ground tiles, "tilec" is for Static Object tiles
        SObjRenderer sObjRenderer = new SObjRenderer();

        for (int i = 0; i < sObjRenderer.getTileSObjTbl().objectCount; i++) {
            File tileFile = Paths.get(outputDirectory.toString(), pad(i, 5) + ".png").toFile();
            if (!tileFile.exists()) {
                try {
                    if (sObjRenderer.getTileSObjTbl().objects.get(i).getHeight() > 0) {
                        BufferedImage sObjTile = sObjRenderer.renderSObject(i);
                        ImageIO.write(sObjTile, "png", tileFile);
                    } else {
                        ImageIO.write(sObjRenderer.getTileRenderer().renderTile(0), "png", tileFile);
                    }
                } catch (IOException ioe) {
                    System.out.println("Error writing");
                }
            }
        }
    }

    public static void generateStencilsFromMaps(Path mapDirectory, Path outputDirectory) {
        if (!outputDirectory.toFile().exists()) {
            outputDirectory.toFile().mkdirs();
        }

        TileRenderer tileRenderer =
                new TileRenderer(FileUtils.createEpfsFromFiles(FileUtils.getTileEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "tile.pal")), new TileTblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl")));
        // Static Object Renderer (for C (Static Object -- SObj) Tiles)
        SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(new File(Resources.DATA_DIRECTORY, "SObj.tbl"));
        SObjRenderer sObjRenderer = new SObjRenderer(new TileRenderer(FileUtils.createEpfsFromFiles(FileUtils.getTileCEpfs(Resources.DATA_DIRECTORY)), new PalFileHandler(new File(Resources.DATA_DIRECTORY, "TileC.pal")), new TileTblFileHandler(new File(Resources.DATA_DIRECTORY, "TILEC.TBL"))), sObjTblFileHandler);
        // Map Renderer from TileRenderer and SObjRenderer
        MapRenderer mapRenderer = new MapRenderer(tileRenderer, sObjRenderer);

        for (File mapFile : mapDirectory.toFile()
                .listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        boolean matches = false;
                        return name.matches("[Tt][Kk]\\d+\\.[Cc][Mm][Pp]");
                    }
                })) {
            CmpFileHandler mapCmp = new CmpFileHandler(mapFile);
            Boolean[][] sObjNodes = mapRenderer.renderMapStencil(mapCmp);

            StringBuilder file = new StringBuilder();
            int newWidth = (mapCmp.mapWidth * 2 + 1);
            int newHeight = (mapCmp.mapHeight * 2 + 1);
            //file.append(newWidth + "," + newHeight + "\n");
            for (int j = 0; j < newHeight; j++) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < newWidth; i++) {
                    line.append(sObjNodes[i][j] ? "1" : "0");
                }
                file.append(line.toString() + "\r\n");
            }

            String mapId = mapFile.getName().split("\\.")[0].substring(2);
            File mapStencil = Paths.get(outputDirectory.toString(), "TK" + mapId + ".gridstencil").toFile();
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(mapStencil));
                writer.write(file.toString());

                writer.close();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
    }
}
