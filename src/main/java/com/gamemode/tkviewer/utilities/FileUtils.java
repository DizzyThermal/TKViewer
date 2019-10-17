package com.gamemode.tkviewer.utilities;

import com.gamemode.tkviewer.file_handlers.*;
import com.gamemode.tkviewer.render.MapRenderer;
import com.gamemode.tkviewer.render.SObjRenderer;
import com.gamemode.tkviewer.render.TileRenderer;
import com.gamemode.tkviewer.resources.EffectImage;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.third_party.GifSequenceWriter;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.*;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.gamemode.tkviewer.utilities.Utils.pad;

// Static File Utilities Class
public class FileUtils {
    public FileUtils() {
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

    // Grab files named: Bow#.epf and sort by number
    public static File[] getBowEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Bow");
    }

    // Grab files named: Body#.epf and sort by number
    public static File[] getBodyEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Body");
    }

    // Grab files named: Coat#.epf and sort by number
    public static File[] getCoatEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Coat");
    }

    // Grab files named: EFFECT#.epf and sort by number
    public static File[] getEffectEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "EFFECT");
    }

    // Grab files named: Face#.epf and sort by number
    public static File[] getFaceEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Face");
    }

    // Grab files named: Fan#.epf and sort by number
    public static File[] getFanEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Fan");
    }

    // Grab files named: Hair#.epf and sort by number
    public static File[] getHairEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Hair");
    }

    // Grab files named: Helmet#.epf and sort by number
    public static File[] getHelmetEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Helmet");
    }

    // Grab files named: Mantle#.epf and sort by number
    public static File[] getMantleEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Mantle");
    }

    // Grab files named: mon#.epf and sort by number
    public static File[] getMobEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "mon");
    }

    // Grab files named: Spear#.epf and sort by number
    public static File[] getSpearEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Spear");
    }

    // Grab files named: Shoes#.epf and sort by number
    public static File[] getShoesEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Shoes");
    }

    // Grab files named: Shield#.epf and sort by number
    public static File[] getShieldEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Shield");
    }

    // Grab files named: Sword#.epf and sort by number
    public static File[] getSwordEpfs(String dataDirectory) {
        return getEpfs(dataDirectory, "Sword");
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

    public static void extractBodyFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Body.dsc")) {
                    return true;
                } else if (name.equals("Body.pal")) {
                    return true;
                } else if (name.matches("Body\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_BODY_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Body.pal, Body.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Body*.epf
            for (File bodyFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("body\\d+\\.dat");
                }
            })) {
                new DatFileHandler(bodyFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractBowFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Bow.dsc")) {
                    return true;
                } else if (name.equals("Bow.pal")) {
                    return true;
                } else if (name.matches("Bow\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_BOW_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Bow.pal, Bow.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Bow*.epf
            for (File bowFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("bow\\d+\\.dat");
                }
            })) {
                new DatFileHandler(bowFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractCoatFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Coat.dsc")) {
                    return true;
                } else if (name.equals("Coat.pal")) {
                    return true;
                } else if (name.matches("Coat\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_COAT_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Coat.pal, Coat.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Coat*.epf
            for (File coatFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("[Cc]oat\\d+\\.[Dd][Aa][Tt]");
                }
            })) {
                new DatFileHandler(coatFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractEffectFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("EFFECT.FRM")) {
                    return true;
                } else if (name.equals("EFFECT.PAL")) {
                    return true;
                } else if (name.matches("EFFECT\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_EFFECT_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // EFFECT.PAL, EFFECT.FRM
            DatFileHandler efxDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "efx.dat");
            efxDat.exportFiles(dataDirectory);

            // Face*.epf
            for (File faceFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("efx\\d+\\.dat");
                }
            })) {
                new DatFileHandler(faceFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractFaceFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Face.dsc")) {
                    return true;
                } else if (name.equals("Face.pal")) {
                    return true;
                } else if (name.matches("Face\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_FACE_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Face.pal, Face.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Face*.epf
            for (File faceFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("face\\d+\\.dat");
                }
            })) {
                new DatFileHandler(faceFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractFanFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Fan.dsc")) {
                    return true;
                } else if (name.equals("Fan.pal")) {
                    return true;
                } else if (name.matches("Fan\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_FAN_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Fan.pal, Fan.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Fan*.epf
            for (File fanFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("fan\\d+\\.dat");
                }
            })) {
                new DatFileHandler(fanFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractHairFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Hair.dsc")) {
                    return true;
                } else if (name.equals("Hair.pal")) {
                    return true;
                } else if (name.matches("Hair\\d+\\.[Ee][Pp][Ff]")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_HAIR_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Hair.pal, Hair.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Hair*.epf
            for (File hairFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("hair\\d+\\.dat");
                }
            })) {
                new DatFileHandler(hairFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractHelmetFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Helmet.dsc")) {
                    return true;
                } else if (name.equals("Helmet.pal")) {
                    return true;
                } else if (name.matches("Helmet\\d+\\.[Ee][Pp][Ff]")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_HELMET_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Helmet.pal, Helmet.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Helmet*.epf
            for (File helmetFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("helmet\\d+\\.dat");
                }
            })) {
                new DatFileHandler(helmetFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractMapFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("SObj.tbl")) {
                    return true;
                } else if (name.equals("tile.pal")) {
                    return true;
                } else if (name.equals("tile.tbl")) {
                    return true;
                } else if (name.equals("TileC.pal")) {
                    return true;
                } else if (name.equals("TILEC.TBL")) {
                    return true;
                } else if (name.matches("tile\\d+\\.epf")) {
                    return true;
                } else if (name.matches("tilec\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_MAP_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // tile.pal, tile.tbl, TileC.pal, TILEC.TBL, SObj.tbl (5 files)
            DatFileHandler tileDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "tile.dat");
            tileDat.exportFiles(dataDirectory);

            // tile*.epf
            for (File tileFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("tile\\d+\\.dat");
                }
            })) {
                new DatFileHandler(tileFile).exportFiles(dataDirectory);
            }

            // tilec*.epf
            for (File tileCFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("tilec\\d+\\.dat");
                }
            })) {
                new DatFileHandler(tileCFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractMantleFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Mantle.dsc")) {
                    return true;
                } else if (name.equals("Mantle.pal")) {
                    return true;
                } else if (name.matches("Mantle\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_MANTLE_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Mantle.pal, Mantle.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Mantle*.epf
            for (File mantleFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("mantle\\d+\\.dat");
                }
            })) {
                new DatFileHandler(mantleFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractMobFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("monster.dna")) {
                    return true;
                } else if (name.equals("monster.pal")) {
                    return true;
                } else if (name.matches("mon\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_MOB_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // monster.pal, monster.dna
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "mon.dat");
            charDat.exportFiles(dataDirectory);

            // mon*.epf
            for (File monFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("mon\\d+\\.dat");
                }
            })) {
                new DatFileHandler(monFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractSpearFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Spear.dsc")) {
                    return true;
                } else if (name.equals("Spear.pal")) {
                    return true;
                } else if (name.matches("Spear\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_SPEAR_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Spear.pal, Spear.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Spear*.epf
            for (File spearFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("spear\\d+\\.dat");
                }
            })) {
                new DatFileHandler(spearFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractShieldFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Shield.dsc")) {
                    return true;
                } else if (name.equals("Shield.pal")) {
                    return true;
                } else if (name.matches("Shield\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_SHIELD_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Shield.pal, Shield.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Shield*.epf
            for (File shieldFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("shield\\d+\\.dat");
                }
            })) {
                new DatFileHandler(shieldFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractShoesFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Shoes.dsc")) {
                    return true;
                } else if (name.equals("Shoes.pal")) {
                    return true;
                } else if (name.matches("Shoes\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_SHOES_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Shoes.pal, Shoes.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Shoes*.epf
            for (File shoesFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("shoes\\d+\\.dat");
                }
            })) {
                new DatFileHandler(shoesFile).exportFiles(dataDirectory);
            }
        }
    }

    public static void extractSwordFilesIfMissing(String dataDirectory, String nexusTKDataDirectory) {
        File dataDirectoryFile = new File(dataDirectory);
        if (!dataDirectoryFile.exists() || dataDirectoryFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                boolean matches = false;
                if (name.equals("Sword.dsc")) {
                    return true;
                } else if (name.equals("Sword.pal")) {
                    return true;
                } else if (name.matches("Sword\\d+\\.epf")) {
                    return true;
                } else {
                    return false;
                }
            }
        }).length < Resources.REQUIRED_SWORD_FILES) {
            // Create Directory if it doesn't exist
            if (!dataDirectoryFile.exists()) {
                dataDirectoryFile.mkdirs();
            }

            // Sword.pal, Sword.dsc
            DatFileHandler charDat = new DatFileHandler(nexusTKDataDirectory + File.separator + "char.dat");
            charDat.exportFiles(dataDirectory);

            // Sword*.epf
            for (File swordFile : new File(nexusTKDataDirectory).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.matches("sword\\d+\\.dat");
                }
            })) {
                new DatFileHandler(swordFile).exportFiles(dataDirectory);
            }
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
            FileUtils.extractMapFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
            SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(new File(Resources.DATA_DIRECTORY, "SObj.tbl"));

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
                                    writer.append("  <object id=\"" + objCount++ + "\" x=\"" + ((j + 1) * 48) + "\" y=\"" +  (i * 48) + "\" width=\"2\" height=\"48\"/>\n");
                                    break;
                                case 0xF:
                                    writer.append("  <object id=\""  + objCount++ +"\" x=\"" + (j * 48) + "\" y=\"" + (i * 48) + "\" width=\"48\" height=\"48\"/>\n");
                                    break;
                            }
                        }
                    }

                    // Non-Passable Tiles
                    if (cmp.mapTiles.get(tileCount).getPassableTile() == 1) {
                        writer.append("  <object id=\""  + objCount++ +"\" x=\"" + (j * 48) + "\" y=\"" + (i * 48) + "\" width=\"48\" height=\"48\"/>\n");
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

            FileUtils.extractMapFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
            SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(new File(Resources.DATA_DIRECTORY, "SObj.tbl"));

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<tileset version=\"1.2\" tiledversion=\"1.2.4\" name=\"SObjects\" tilewidth=\"48\" tileheight=\"576\" tilecount=\"" + sObjTblFileHandler.objectCount + "\" columns=\"5\">\n");
            writer.append(" <grid orientation=\"orthogonal\" width=\"1\" height=\"1\"/>\n");

            for (int i = 0 ; i < sObjTblFileHandler.objectCount; i++) {
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
                            writer.append("   <object id=\"1\" x=\"48\" y=\"" +  (48 * (height - 1)) + "\" height=\"48\"/>\n");
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

            FileUtils.extractMapFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
            SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(new File(Resources.DATA_DIRECTORY, "SObj.tbl"));

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<tileset version=\"1.2\" tiledversion=\"1.2.4\" name=\"SObjects\" tilewidth=\"48\" tileheight=\"576\" tilecount=\"" + sObjTblFileHandler.objectCount + "\" columns=\"5\">\n");
            writer.append(" <grid orientation=\"orthogonal\" width=\"1\" height=\"1\"/>\n");

            for (int i = 0 ; i < sObjTblFileHandler.objectCount; i++) {
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

            TblFileHandler tblFileHandler = new TblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl"));

            writer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            writer.append("<tileset version=\"1.2\" tiledversion=\"1.2.4\" name=\"Tiles\" tilewidth=\"48\" tileheight=\"48\" tilecount=\"" + tblFileHandler.tileCount + "\" columns=\"5\">\n");
            writer.append(" <grid orientation=\"orthogonal\" width=\"1\" height=\"1\"/>\n");

            for (int i = 0 ; i < tblFileHandler.tileCount; i++) {
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
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "tile.pal")), new TblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl")));

        for (int i = 0 ; i < tileRenderer.getCount(); i++) {
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
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "tile.pal")), new TblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl")));
        SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(new File(Resources.DATA_DIRECTORY, "SObj.tbl"));
        SObjRenderer sObjRenderer = new SObjRenderer(new TileRenderer(FileUtils.createEpfsFromFiles(FileUtils.getTileCEpfs(Resources.DATA_DIRECTORY)), new PalFileHandler(new File(Resources.DATA_DIRECTORY, "TileC.pal")), new TblFileHandler(new File(Resources.DATA_DIRECTORY, "TILEC.TBL"))), sObjTblFileHandler);

        for (int i = 0 ; i < sObjTblFileHandler.objectCount; i++) {
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
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "tile.pal")), new TblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl")));
        // Static Object Renderer (for C (Static Object -- SObj) Tiles)
        SObjTblFileHandler sObjTblFileHandler = new SObjTblFileHandler(new File(Resources.DATA_DIRECTORY, "SObj.tbl"));
        SObjRenderer sObjRenderer = new SObjRenderer(new TileRenderer(FileUtils.createEpfsFromFiles(FileUtils.getTileCEpfs(Resources.DATA_DIRECTORY)), new PalFileHandler(new File(Resources.DATA_DIRECTORY, "TileC.pal")), new TblFileHandler(new File(Resources.DATA_DIRECTORY, "TILEC.TBL"))), sObjTblFileHandler);
        // Map Renderer from TileRenderer and SObjRenderer
        MapRenderer mapRenderer = new MapRenderer(tileRenderer, sObjRenderer);

        for (File mapFile : mapDirectory.toFile()
                .listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        boolean matches = false;
                        if (name.matches("[Tt][Kk]\\d+\\.[Cc][Mm][Pp]")) {
                            return true;
                        } else {
                            return false;
                        }
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
