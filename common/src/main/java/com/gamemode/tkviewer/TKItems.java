package com.gamemode.tkviewer;

import com.gamemode.tkviewer.file_handlers.DatFileHandler;
import com.gamemode.tkviewer.file_handlers.EpfFileHandler;
import com.gamemode.tkviewer.file_handlers.PalFileHandler;
import com.gamemode.tkviewer.render.TileRenderer;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class TKItems {

    public static void main(String[] args) {
        File outputDirectory = new File("C:\\NTK_Items");

        if (!outputDirectory.exists()) {
            outputDirectory.mkdirs();
        }

        String ntkDataDirectory = Resources.getNtkDataDirectory();
        DatFileHandler charDat = new DatFileHandler(ntkDataDirectory + File.separator + "char.dat");
        DatFileHandler miscDat = new DatFileHandler(ntkDataDirectory + File.separator + "misc.dat");

        EpfFileHandler epf = new EpfFileHandler(miscDat.getFile("SYMBOLS.EPF"),"SYMBOLS.EPF");
        PalFileHandler pal = new PalFileHandler(charDat.getFile("ITEM.PAL"));

        TileRenderer tileRenderer = new TileRenderer(new ArrayList<EpfFileHandler>(Arrays.asList(epf)), pal, 0);

        for (int i = 0; i < epf.frameCount; i++) {
            FileUtils.writeBufferedImageToFile(tileRenderer.renderTile(i), Paths.get(outputDirectory.toString(), i + ".png").toString());
        }
    }
}
