package com.gamemode.tkviewer.utilities;

import com.gamemode.tkviewer.file_handlers.*;
import com.gamemode.tkviewer.render.*;
import com.gamemode.tkviewer.resources.*;
import com.gamemode.tkviewer.third_party.*;

import javax.imageio.*;
import javax.imageio.stream.*;
import java.awt.image.*;
import java.io.FileWriter;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.gamemode.tkviewer.utilities.Utils.pad;

// Static File Utilities Class
public class FileUtils {

    public static ByteBuffer sObjBytes = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "tile.dat").getFile("SObj.tbl");

    public FileUtils() {
    }

    public static List<EpfFileHandler> createEpfsFromDats(String epfPrefix) {
        return createEpfsFromDats(epfPrefix, epfPrefix,"epf");
    }
    public static List<EpfFileHandler> createEpfsFromDats(String epfPrefix, String datPrefix) {
        return createEpfsFromDats(epfPrefix, datPrefix, "epf");
    }
    public static List<EpfFileHandler> createEpfsFromDats(String epfPrefix, String datPrefix, String extension) {
        List<EpfFileHandler> epfFileHandlers = new ArrayList<EpfFileHandler>();

        List<DatFileHandler> datFileHandlers = getDatFileHandlers(datPrefix);
        for (int i = 0; i < datFileHandlers.size(); i++) {
            ByteBuffer b = datFileHandlers.get(i).getFile(epfPrefix + i + "." + extension);
            EpfFileHandler epf = new EpfFileHandler(b);
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

    public static List<DatFileHandler> getDatFileHandlers(String prefix) {
        List<DatFileHandler> datFileHandlers = new ArrayList<DatFileHandler>();

        File[] datFiles = getDats(Resources.NTK_DATA_DIRECTORY, prefix);
        for (File datFile : datFiles) {
            datFileHandlers.add(new DatFileHandler(datFile));
        }

        return datFileHandlers;
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

    public static void generateGroundTileSets(Path outputDirectory) {
        FileUtils.generateGroundTileSets(outputDirectory, 1000);
    }

    public static void generateGroundTileSets(Path outputDirectory, int tilesetDelimiter) {
        if (!outputDirectory.toFile().exists()) {
            outputDirectory.toFile().mkdirs();
        }

        DatFileHandler tileDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "tile.dat");
        TileTblFileHandler tileTblFileHandler = new TileTblFileHandler(tileDat.getFile("tile.tbl"));

        // Break the tilesets up by, tileset_delim
        for (int i = 0; i < (tileTblFileHandler.tileCount / tilesetDelimiter) + 1; i++) {
            File groundTileSetFile = Paths.get(outputDirectory.toString(),
                    "ground_tiles_" + pad(i, 2) + ".tsx").toFile();
            boolean isLastTileSet = !((i + 1) < (tileTblFileHandler.tileCount / tilesetDelimiter));
            int tileCount = isLastTileSet?((int)(tileTblFileHandler.tileCount % tilesetDelimiter)):tilesetDelimiter;

            try {
                FileWriter writer = new FileWriter(groundTileSetFile);
                // Clear the file
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.append("<tileset version=\"1.4\" tiledversion=\"1.4.2\" name=\"tiles_" + pad(i, 2) + "\" tilewidth=\"48\" tileheight=\"48\" tilecount=\"" + tileCount + "\" columns=\"0\">\n");
                writer.append("\t<grid orientation=\"orthogonal\" width=\"1\" height=\"1\"/>\n");

                for (int j = 0; j < tilesetDelimiter; j++) {
                    int tileId = (i * tilesetDelimiter) + j;
                    if (tileId == tileTblFileHandler.tileCount) {
                        break;
                    }
                    writer.append("\t<tile id=\"" + j + "\">\n");
                    writer.append("\t\t<image width=\"48\" height=\"48\" source=\"tiles/" + pad(tileId, 5) + ".png\"/>\n");
                    writer.append("\t</tile>\n");
                }

                writer.append("</tileset>\n");

                writer.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    public static void generateStaticObjectTileSets(Path outputDirectory) {
        FileUtils.generateStaticObjectTileSets(outputDirectory, 1000);
    }

    public static void generateStaticObjectTileSets(Path outputDirectory, int tilesetDelimiter) {
        if (!outputDirectory.toFile().exists()) {
            outputDirectory.toFile().mkdirs();
        }

        DatFileHandler tileDat = new DatFileHandler(Resources.NTK_DATA_DIRECTORY + File.separator + "tile.dat");
        SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(tileDat.getFile("SObj.tbl"));

        // Break the tilesets up by, tileset_delim
        for (int i = 0; i < (sObjTblFileHandler.objectCount / tilesetDelimiter) + 1; i++) {
            File groundTileSetFile = Paths.get(outputDirectory.toString(),
                    "object_tiles_" + pad(i, 2) + ".tsx").toFile();
            boolean isLastTileSet = !((i + 1) < (sObjTblFileHandler.objectCount / tilesetDelimiter));
            int tileCount = isLastTileSet?((int)(sObjTblFileHandler.objectCount % tilesetDelimiter)):tilesetDelimiter;

            try {
                FileWriter writer = new FileWriter(groundTileSetFile);
                // Clear the file
                writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                writer.append("<tileset version=\"1.4\" tiledversion=\"1.4.2\" name=\"objects_" + pad(i, 2) + "\" tilewidth=\"48\" tileheight=\"480\" tilecount=\"" + tileCount + "\" columns=\"0\">\n");
                writer.append("\t<grid orientation=\"orthogonal\" width=\"1\" height=\"1\"/>\n");

                for (int j = 0; j < tilesetDelimiter; j++) {
                    int objectId = (i * tilesetDelimiter) + j;
                    if (objectId == sObjTblFileHandler.objectCount) {
                        break;
                    }
                    writer.append("\t<tile id=\"" + j + "\">\n");
                    int objectHeight = sObjTblFileHandler.objects.get(objectId).getHeight();
                            writer.append("\t\t<image width=\"48\" height=\"" + (objectHeight * 48) + "\" source=\"objects/" + pad(objectId, 5) + ".png\"/>\n");
                    writer.append("\t</tile>\n");
                }

                writer.append("</tileset>\n");

                writer.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

    private static List<String> collectUsedGroundTileSets(CmpFileHandler cmpFileHandler, int tileDelimiter) {
        // Create and Sort Tile Sets
        List<String> usedTileSets = new ArrayList<String>();
        for (Tile tile : cmpFileHandler.mapTiles) {
            if (tile.getAbTile() >= 0) {
                int tileSetIndex = tile.getAbTile() / tileDelimiter;
                String tileSetString = "\t<tileset firstgid=\"?\" source=\"ground_tiles_" + pad(tileSetIndex, 2) + ".tsx\"/>\n";
                if (usedTileSets.isEmpty()) {
                    usedTileSets.add(tileSetString);
                }
                if (!usedTileSets.contains(tileSetString)) {
                    usedTileSets.add(tileSetString);
                }
            }
        }
        Collections.sort(usedTileSets);

        // Inject GIDs
        for (int i = 0; i < usedTileSets.size(); i++) {
            int firstGid = (i * tileDelimiter) + 1;
            usedTileSets.set(i, usedTileSets.get(i).replace("firstgid=\"?\"", "firstgid=\"" + firstGid + "\""));
        }

        return usedTileSets;
    }

    private static List<String> collectUsedStaticObjectTileSets(CmpFileHandler cmpFileHandler, int tileDelimiter, int groundTileSetOffset) {
        // Create and Sort Tile Sets
        List<String> usedTileSets = new ArrayList<String>();
        for (Tile tile : cmpFileHandler.mapTiles) {
            if (tile.getSObjTile() >= 0) {
                int tileSetIndex = tile.getSObjTile() / tileDelimiter;
                String tileSetString = "\t<tileset firstgid=\"?\" source=\"object_tiles_" + pad(tileSetIndex, 2) + ".tsx\"/>\n";
                if (usedTileSets.isEmpty()) {
                    usedTileSets.add(tileSetString);
                }
                if (!usedTileSets.contains(tileSetString)) {
                    usedTileSets.add(tileSetString);
                }
            }
        }
        Collections.sort(usedTileSets);

        // Inject GIDs
        for (int i = 0; i < usedTileSets.size(); i++) {
            int firstGid = ((i + groundTileSetOffset) * tileDelimiter) + 1;
            usedTileSets.set(i, usedTileSets.get(i).replace("firstgid=\"?\"", "firstgid=\"" + firstGid + "\""));
        }
        return usedTileSets;
    }

    public static Integer getTileGetGid(Integer tileSetIndex, List<String> usedTileSets, Pattern p) {
        for (String tileSetString : usedTileSets) {
            if (tileSetString.contains("_" + pad(tileSetIndex, 2) + ".tsx")) {
                Matcher m = p.matcher(tileSetString);

                if (m.find()) {
                    return Integer.parseInt(m.group(1));
                }
            }
        }

        return -1;
    }

    public static void cmpFileToTmxFile(final File cmpFile, final File tmxFile) {
        FileUtils.cmpFileToTmxFile(cmpFile, tmxFile, 1000);
    }

    public static void cmpFileToTmxFile(final File cmpFile, final File tmxFile, int tileDelimiter) {
        CmpFileHandler cmp = new CmpFileHandler(cmpFile);

        try {
            Pattern p = Pattern.compile("firstgid=\"([0-9]+)\"");

            FileWriter writer = new FileWriter(tmxFile);
            writer.write("");   // Clear File

            // Map Header
            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<map version=\"1.4\" tiledversion=\"1.4.2\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"" + cmp.mapWidth + "\" height=\"" + cmp.mapHeight + "\" tilewidth=\"48\" tileheight=\"48\" infinite=\"0\" nextlayerid=\"3\" nextobjectid=\"4\">\n");

            // Insert Used Ground Tile Sets
            List<String> usedGroundTileSets = collectUsedGroundTileSets(cmp, tileDelimiter);
            for (String tileSetString : usedGroundTileSets) {
                writer.append(tileSetString);
            }

            // Insert Used Object Tile Sets
            List<String> usedStaticObjectTileSets = collectUsedStaticObjectTileSets(cmp, tileDelimiter, usedGroundTileSets.size());
            for (String tileSetString : usedStaticObjectTileSets) {
                writer.append(tileSetString);
            }

            // Ground Layer
            writer.append("\t<layer id=\"1\" name=\"Ground\" width=\"" + cmp.mapWidth + "\" height=\"" + cmp.mapHeight + "\">\n");
            writer.append("\t\t<data encoding=\"csv\">\n");

            int tileCount = 0;
            for (int i = 0; i < cmp.mapHeight; i++) {
                for (int j = 0; j < cmp.mapWidth; j++) {
                    int abTile = cmp.mapTiles.get(tileCount).getAbTile();
                    int tileSetIndex = abTile / tileDelimiter;
                    int tileGid = FileUtils.getTileGetGid(tileSetIndex, usedGroundTileSets, p);
                    int relativeId = tileGid + (abTile - (tileSetIndex * tileDelimiter)) + 1;
                    writer.append(((relativeId > 1) ? relativeId : 0) + (((j == (cmp.mapWidth - 1) && i == (cmp.mapHeight - 1)) ? "" : ",")));
                    tileCount++;
                }
                writer.append("\n");
            }

            writer.append("\t\t</data>\n");
            writer.append("\t</layer>\n");

            // Object Layer
            writer.append("\t<layer id=\"1\" name=\"Objects\" width=\"" + cmp.mapWidth + "\" height=\"" + cmp.mapHeight + "\">\n");
            writer.append("\t\t<data encoding=\"csv\">\n");

            int objectCount = 0;
            for (int i = 0; i < cmp.mapHeight; i++) {
                for (int j = 0; j < cmp.mapWidth; j++) {
                    int sObjTile = cmp.mapTiles.get(objectCount).getSObjTile();
                    int tileSetIndex = sObjTile / tileDelimiter;
                    int tileGid = FileUtils.getTileGetGid(tileSetIndex, usedStaticObjectTileSets, p);
                    int relativeId = tileGid + (sObjTile - (tileSetIndex * tileDelimiter));
                    writer.append(((sObjTile >= 0) ? relativeId : 0) + (((j == (cmp.mapWidth - 1) && i == (cmp.mapHeight - 1)) ? "" : ",")));
                    objectCount++;
                }
                writer.append("\n");
            }

            writer.append("\t\t</data>\n");
            writer.append("\t</layer>\n");


            writer.append("</map>");

            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
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
