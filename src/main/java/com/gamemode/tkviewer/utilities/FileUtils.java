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

    public static void cmpFileToTmxFile(final File cmpFile, final File tmxFile) {
        CmpFileHandler cmp = new CmpFileHandler(cmpFile);

        try {
            FileWriter writer = new FileWriter(tmxFile);
            writer.write("");   // Clear File

            // Map Header
            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<map version=\"1.2\" tiledversion=\"1.2.4\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"" + cmp.mapWidth + "\" height=\"" + cmp.mapHeight + "\" tilewidth=\"48\" tileheight=\"48\" infinite=\"0\" nextlayerid=\"3\" nextobjectid=\"4\">\n");
            writer.append(" <tileset firstgid=\"1\" source=\"NTK.tsx\"/>\n");
            writer.append(" <tileset firstgid=\"50000\" source=\"NTK_Objects.tsx\"/>\n");

            // Ground Layer
            writer.append(" <layer id=\"1\" name=\"Ground\" width=\"" + cmp.mapWidth + "\" height=\"" + cmp.mapHeight + "\">\n");
            writer.append("  <data encoding=\"csv\">\n");

            int tileCount = 0;
            for (int i = 0; i < cmp.mapWidth; i++) {
                for (int j = 0; j < cmp.mapHeight; j++) {
                    writer.append(cmp.mapTiles.get(tileCount).getAbTile() + 2 + ((i == (cmp.mapWidth - 1) && j == (cmp.mapHeight - 1)) ? "" : ","));
                    tileCount++;
                }
                writer.append("\n");
            }

            writer.append("  </data>\n");
            writer.append(" </layer>\n");

            // Static Object Layer
            boolean oldWay = false;
            if (oldWay) {
                writer.append(" <layer id=\"2\" name=\"Objects\" width=\"" + cmp.mapWidth + "\" height=\"" + cmp.mapHeight + "\">\n");
                writer.append("  <data encoding=\"csv\">\n");

                tileCount = 0;
                for (int i = 0; i < cmp.mapHeight; i++) {
                    for (int j = 0; j < cmp.mapWidth; j++) {
                        if (cmp.mapTiles.get(tileCount).getSObjTile() > 0) {
                            writer.append((50000 + cmp.mapTiles.get(tileCount).getSObjTile()) + ((i == (cmp.mapWidth - 1) && j == (cmp.mapHeight - 1)) ? "" : ","));
                        } else {
                            writer.append(0 + ((i == (cmp.mapWidth - 1) && j == (cmp.mapHeight - 1)) ? "" : ","));
                        }
                        tileCount++;
                    }
                    writer.append("\n");
                }

                writer.append("  </data>\n");
                writer.append(" </layer>\n");
            } else {
                writer.append(" <objectgroup id=\"2\" name=\"Objects\" visible=\"1\">\n");

                int objCount = 0;
                tileCount = 0;
                for (int i = 0; i < cmp.mapHeight; i++) {
                    for (int j = 0; j < cmp.mapWidth; j++) {
                        if (cmp.mapTiles.get(tileCount).getSObjTile() > 0) {
                            writer.append("  <object id=\"" + (objCount++) + "\" type=\"PROP\" gid=\"" + (50000 + cmp.mapTiles.get(tileCount).getSObjTile()) + "\" x=\"" + (j * 48) + "\" y=\"" + ((i + 1) * 48) + "\"/>\n");
                        }
                        tileCount++;
                    }
                }

                writer.append(" </objectgroup>\n");
            }

            // Collisions
            SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(sObjBytes);

            writer.append(" <objectgroup id=\"3\" name=\"Collisions\">\n");

            int objCount = 0;
            tileCount = 0;
            for (int i = 0; i < cmp.mapHeight; i++) {
                for (int j = 0; j < cmp.mapWidth; j++) {
                    // Static Objects
                    if (cmp.mapTiles.get(tileCount).getSObjTile() > 0) {
                        // TODO: Determine if this needs +1 or not
                        int sObjId = cmp.mapTiles.get(tileCount).getSObjTile();
                        if (sObjTblFileHandler.objects.get(sObjId).getMovementDirection() > 0) {
                            switch (sObjTblFileHandler.objects.get(sObjId).getMovementDirection()) {
                                case 0x1:
                                    writer.append("  <object id=\"" + objCount++ + "\" x=\"" + (j * 48) + "\" y=\"" + ((i + 1) * 48) + "\" width=\"48\" height=\"2\"/>\n");
                                    break;
                                case 0x2:
                                    writer.append("  <object id=\"" + objCount++ + "\" x=\"" + (j * 48) + "\" y=\"" + (i * 48) + "\" width=\"48\" height=\"2\"/>\n");
                                    break;
                                case 0x4:
                                    writer.append("  <object id=\"" + objCount++ + "\" x=\"" + (j * 48) + "\" y=\"" + (i * 48) + "\" width=\"2\" height=\"48\"/>\n");
                                    break;
                                case 0x8:
                                    writer.append("  <object id=\"" + objCount++ + "\" x=\"" + ((j + 1) * 48) + "\" y=\"" + (i * 48) + "\" width=\"2\" height=\"48\"/>\n");
                                    break;
                                case 0xF:
                                    writer.append("  <object id=\"" + objCount++ + "\" x=\"" + (j * 48) + "\" y=\"" + (i * 48) + "\" width=\"48\" height=\"48\"/>\n");
                                    break;
                            }
                        }
                    }

                    // Non-Passable Tiles
                    if (cmp.mapTiles.get(tileCount).getPassableTile() == 1) {
                        writer.append("  <object id=\"" + objCount++ + "\" x=\"" + (j * 48) + "\" y=\"" + (i * 48) + "\" width=\"48\" height=\"48\"/>\n");
                    }
                    tileCount++;
                }
            }

            writer.append(" </objectgroup>\n");


            writer.append("</map>\n");

            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void generateSObjTileSet(File outputFile) {
        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write("");   // Clear File

            SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(sObjBytes);

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<tileset version=\"1.2\" tiledversion=\"1.2.4\" name=\"SObjects\" tilewidth=\"48\" tileheight=\"576\" tilecount=\"" + sObjTblFileHandler.objectCount + "\" columns=\"5\">\n");
            writer.append(" <grid orientation=\"orthogonal\" width=\"1\" height=\"1\"/>\n");

            for (int i = 0; i < sObjTblFileHandler.objectCount; i++) {
                writer.append(" <tile id=\"" + i + "\">\n");
                int height = 1;
                if (sObjTblFileHandler.objects.get(i).getHeight() > 1) {
                    height = sObjTblFileHandler.objects.get(i).getHeight();
                }
                writer.append("  <image width=\"48\" height=\"" + (48 * height) + "\" source=\"objects/" + pad(i, 5) + ".png\"/>\n");
                if (sObjTblFileHandler.objects.get(i).getMovementDirection() > 0) {
                    writer.append("  <objectgroup draworder=\"index\">\n");
                    switch (sObjTblFileHandler.objects.get(i).getMovementDirection()) {
                        case 0x1:
                            writer.append("   <object id=\"1\" x=\"0\" y=\"" + (48 * height) + "\" width=\"48\"/>\n");
                            break;
                        case 0x2:
                            writer.append("   <object id=\"1\" x=\"0\" y=\"" + (48 * (height - 1)) + "\" width=\"48\"/>\n");
                            break;
                        case 0x4:
                            writer.append("   <object id=\"1\" x=\"0\" y=\"" + (48 * (height - 1)) + "\" height=\"48\"/>\n");
                            break;
                        case 0x8:
                            writer.append("   <object id=\"1\" x=\"48\" y=\"" + (48 * (height - 1)) + "\" height=\"48\"/>\n");
                            break;
                        case 0xF:
                            writer.append("   <object id=\"1\" x=\"0\" y=\"" + (48 * (height - 1)) + "\" width=\"48\" height=\"48\"/>\n");
                            break;
                    }
                    writer.append("  </objectgroup>\n");
                }
                writer.append(" </tile>\n");
            }

            writer.append("</tileset>\n");
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void generateSObjTileSet2(File outputFile) {
        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write("");   // Clear File

            SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(sObjBytes);

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<tileset version=\"1.2\" tiledversion=\"1.2.4\" name=\"SObjects\" tilewidth=\"48\" tileheight=\"576\" tilecount=\"" + sObjTblFileHandler.objectCount + "\" columns=\"5\">\n");
            writer.append(" <grid orientation=\"orthogonal\" width=\"1\" height=\"1\"/>\n");

            for (int i = 0; i < sObjTblFileHandler.objectCount; i++) {
                int height = sObjTblFileHandler.objects.get(i).getHeight();
                if (height == 0) {
                    height = 1;
                }

                writer.append(" <tile id=\"" + i + "\">\n");
                writer.append("  <image width=\"48\" height=\"" + (48 * height) + "\" source=\"../objects/" + pad(i, 5) + ".png\"/>\n");
                writer.append(" </tile>\n");
            }

            writer.append("</tileset>\n");
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void generateTileSet(File outputFile) {
        try {
            FileWriter writer = new FileWriter(outputFile);
            writer.write("");   // Clear File

            TileTblFileHandler tileTblFileHandler = new TileTblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl"));

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<tileset version=\"1.2\" tiledversion=\"1.2.4\" name=\"Tiles\" tilewidth=\"48\" tileheight=\"48\" tilecount=\"" + tileTblFileHandler.tileCount + "\" columns=\"5\">\n");
            writer.append(" <grid orientation=\"orthogonal\" width=\"1\" height=\"1\"/>\n");

            for (int i = 0; i < tileTblFileHandler.tileCount; i++) {
                writer.append(" <tile id=\"" + i + "\">\n");
                writer.append("  <image width=\"48\" height=\"48\" source=\"../tiles/" + pad(i, 5) + ".png\"/>\n");
                writer.append(" </tile>\n");
            }

            writer.append("</tileset>\n");
            writer.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    public static void generateTileImages(Path outputDirectory) {
        if (!outputDirectory.toFile().exists()) {
            outputDirectory.toFile().mkdirs();
        }

        TileRenderer tileRenderer =
                new TileRenderer(FileUtils.createEpfsFromFiles(FileUtils.getTileEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "tile.pal")), new TileTblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl")));

        for (int i = 0; i < tileRenderer.getCount(); i++) {
            File tileFile = Paths.get(outputDirectory.toString(), pad(i, 5) + ".png").toFile();
            try {
                ImageIO.write(tileRenderer.renderTile(i), "png", tileFile);
            } catch (IOException ioe) {
                System.out.println("Error writing");
            }
        }
    }

    public static void generateSObjTileImages(Path outputDirectory) {
        if (!outputDirectory.toFile().exists()) {
            outputDirectory.toFile().mkdirs();
        }

        TileRenderer tileRenderer =
                new TileRenderer(FileUtils.createEpfsFromFiles(FileUtils.getTileEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "tile.pal")), new TileTblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl")));
        SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(new File(Resources.DATA_DIRECTORY, "SObj.tbl"));
        SObjRenderer sObjRenderer = new SObjRenderer(new TileRenderer(FileUtils.createEpfsFromFiles(FileUtils.getTileCEpfs(Resources.DATA_DIRECTORY)), new PalFileHandler(new File(Resources.DATA_DIRECTORY, "TileC.pal")), new TileTblFileHandler(new File(Resources.DATA_DIRECTORY, "TILEC.TBL"))), sObjTblFileHandler);

        for (int i = 0; i < sObjTblFileHandler.objectCount; i++) {
            File tileFile = Paths.get(outputDirectory.toString(), pad(i, 5) + ".png").toFile();
            try {
                if (sObjTblFileHandler.objects.get(i).getHeight() > 0) {
                    BufferedImage sObjTile = sObjRenderer.renderSObject(i);
                    ImageIO.write(sObjTile, "png", tileFile);
                } else {
                    ImageIO.write(tileRenderer.renderTile(0), "png", tileFile);
                }
            } catch (IOException ioe) {
                System.out.println("Error writing");
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
