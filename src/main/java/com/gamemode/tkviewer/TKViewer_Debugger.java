package com.gamemode.tkviewer;

import com.gamemode.tkviewer.file_handlers.*;
import com.gamemode.tkviewer.render.*;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TKViewer_Debugger {
    // Count of required files:
    //   tile*.epf    | tile.pal  | tile.tbl
    //   tilec*.epf   | TileC.pal | TILEC.tbl | SObj.tbl
    private static final int REQUIRED_FILES = 56;

    private static final String PROGRAM_FILES = "C:\\Program Files";
    private static final String PROGRAM_FILES_X86 = "C:\\Program Files (x86)";
    private static final String PATH_PREFIX = (System.getProperty("os.arch").contains("64"))?PROGRAM_FILES_X86:PROGRAM_FILES;

    private static final String NEXUSTK_DATA_DIRECTORY = PATH_PREFIX + File.separator + "KRU\\NexusTK\\Data";

    private static final String DATA_DIRECTORY = System.getProperty("java.io.tmpdir") + File.separator + "TKViewer\\NexusTK-Data";

    private static final String TMP_DATA = "C:\\Users\\Stephen\\Git\\TKViewer\\Data";

    public static void main(String[] args) {
        // Extract Required Map Files
        FileUtils.extractMapFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        // Tile Renderer (for AB (Ground) Tiles)
        TileRenderer tileRenderer =
                new TileRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getTileEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "tile.pal")), new TblFileHandler(new File(Resources.DATA_DIRECTORY, "tile.tbl")));
        // Static Object Renderer (for C (Static Object -- SObj) Tiles)
        SObjRenderer sObjRenderer = new SObjRenderer(new TileRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getTileCEpfs(Resources.DATA_DIRECTORY)), new PalFileHandler(new File(Resources.DATA_DIRECTORY, "TileC.pal")), new TblFileHandler(new File(Resources.DATA_DIRECTORY, "TILEC.TBL"))), new SObjTblFileHandler(new File(Resources.DATA_DIRECTORY, "SObj.tbl")));
        // Map Renderer from TileRenderer and SObjRenderer
        MapRenderer mapRenderer = new MapRenderer(tileRenderer, sObjRenderer);

        for (File mapFile : new File("C:\\Users\\Stephen\\Documents\\NexusTK\\Maps")
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
            File mapStencil = new File("C:\\Users\\Stephen\\Desktop\\stencils\\TK" + mapId + ".gridstencil");
            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(mapStencil));
                writer.write(file.toString());

                writer.close();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }

        System.exit(0);


        FileUtils.extractFaceFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        PartRenderer faceRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getFaceEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Face.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Face.dsc")));

        FileUtils.extractHairFilesIfMissing(Resources.DATA_DIRECTORY, Resources.NEXUSTK_DATA_DIRECTORY);
        PartRenderer hairRenderer =
                new PartRenderer(EpfFileHandler.createEpfsFromFiles(FileUtils.getHairEpfs(Resources.DATA_DIRECTORY)),
                        new PalFileHandler(new File(Resources.DATA_DIRECTORY, "Hair.pal")),
                        new DscFileHandler(new File(Resources.DATA_DIRECTORY, "Hair.dsc")));

        BufferedImage head = new BufferedImage(50, 50, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gObject = head.createGraphics();
        BufferedImage face = faceRenderer.renderPart(0,6,0);
        BufferedImage hair = hairRenderer.renderPart(0,6,0);
        int faceX = head.getWidth() / 2 - face.getWidth() / 2;
        int faceY = head.getHeight() / 2 - face.getHeight() / 2;
        int hairX = head.getWidth() / 2 - hair.getWidth() / 2;
        int hairY = head.getHeight() / 2 - hair.getHeight() / 2;
        gObject.drawImage(face, faceX, faceY, null);
        gObject.drawImage(hair, hairX, hairY - 3, null);

        File f = new File("C:\\Users\\Stephen\\Desktop\\test.png");
        try {
            ImageIO.write(head, "png", f);
        } catch (IOException ioe) {
            System.out.println("Error writing");
        }
    }
}