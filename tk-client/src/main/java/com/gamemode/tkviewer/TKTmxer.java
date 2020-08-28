package com.gamemode.tkviewer;

import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;
import com.gamemode.tkviewer.utilities.Utils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TKTmxer {

    private static String outputDirectory = "C:\\Users\\Stephen\\IdeaProjects\\TKViewer\\maps";

    private static String tileDirectory = Paths.get(outputDirectory, "tiles").toString();
    private static String sObjDirectory = Paths.get(outputDirectory, "objects").toString();

    private static List<Integer> mapsToConvert = new ArrayList<Integer>(
            Arrays.asList(
                    2,
                    41,
                    18000
            )
    );

    public static void main(String[] args) {
        generateResources();
        convertMaps(Paths.get(outputDirectory));
    }

    private static void generateResources() {
        FileUtils.generateGroundTileImages(Paths.get(tileDirectory));
        FileUtils.generateStaticObjectTileImages(Paths.get(sObjDirectory));
        FileUtils.generateGroundTileSets(Paths.get(outputDirectory));
        FileUtils.generateStaticObjectTileSets(Paths.get(outputDirectory));
    }

    private static void convertMaps(Path outputDirectory) {
        if (!outputDirectory.toFile().exists()) {
            outputDirectory.toFile().mkdirs();
        }

        for (Integer mapId : mapsToConvert) {
            if (!Paths.get(outputDirectory.toString(), Utils.pad(mapId, 6) + ".tmx").toFile().exists()) {
                FileUtils.cmpFileToTmxFile(
                        Paths.get(Resources.NTK_MAP_DIRECTORY, "TK" + Utils.pad(mapId, 6) + ".cmp").toFile(),
                        Paths.get(outputDirectory.toString(), Utils.pad(mapId, 6) + ".tmx").toFile()
                );
            }
        }
    }
}
