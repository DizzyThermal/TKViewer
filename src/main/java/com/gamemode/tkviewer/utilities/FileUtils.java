package com.gamemode.tkviewer.utilities;

import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.file_handlers.EpfFileHandler;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.Resources;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

// Static Utility Class
public class FileUtils {
    public FileUtils() {}

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
                    int s = name.indexOf(prefix.substring(prefix.length() - 1))+1;
                    int e = name.lastIndexOf('.');
                    String number = name.substring(s, e);
                    i = Integer.parseInt(number);
                } catch(Exception e) {
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
        int epfIndex = 0;

        int frameCount = 0;
        for (int i = 0; i < epfFiles.size(); i++) {
            if (index < (frameCount + epfFiles.get(i).frameCount)) {
                epfIndex = i;
                break;
            }

            frameCount += epfFiles.get(i).frameCount;
        }

        return epfFiles.get(epfIndex).getFrame(index - frameCount);
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_BODY_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_BOW_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_FACE_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_FAN_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_HAIR_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_HELMET_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
        } ).length < Resources.REQUIRED_MAP_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_MANTLE_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_SPEAR_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_SHIELD_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_SHOES_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
                }else {
                    return false;
                }
            }
        } ).length < Resources.REQUIRED_SWORD_FILES) {
            // Create Directory if it doesn't exist
            if(!dataDirectoryFile.exists()) {
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
}
