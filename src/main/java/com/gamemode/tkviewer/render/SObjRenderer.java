package com.gamemode.tkviewer.render;

import com.gamemode.tkviewer.file_handlers.SObjTblFileHandler;
import com.gamemode.tkviewer.resources.Frame;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.resources.SObject;
import com.gamemode.tkviewer.utilities.FileUtils;

import java.awt.*;
import java.awt.image.BufferedImage;

public class SObjRenderer {

    SObjTblFileHandler tileSobjTbl;
    TileRenderer tileRenderer;

    public SObjRenderer(TileRenderer tileRenderer, SObjTblFileHandler tileSobjTbl) {
        this.tileRenderer = tileRenderer;
        this.tileSobjTbl = tileSobjTbl;
    }

    public BufferedImage renderSObject(int sObjIndex) {
        SObject sObj = this.tileSobjTbl.objects.get(sObjIndex);
        int sObjHeight = sObj.getHeight();

        BufferedImage image = new BufferedImage(Resources.TILE_DIM, sObjHeight * Resources.TILE_DIM, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphicsObject = image.createGraphics();
        for (int i = 0; i < sObjHeight; i++) {
            int tileIndex = sObj.getTileIndices().get(i);

            BufferedImage tile = this.tileRenderer.renderTile(tileIndex);
            Frame frame = FileUtils.getFrameFromEpfs(tileIndex, this.tileRenderer.tileEpfs);

            if (tileIndex > -1) {
                int b = (sObjHeight - i) * Resources.TILE_DIM;
                graphicsObject.drawImage(
                        tile,
                        null,
                        frame.getLeft(),
                        (sObjHeight - i - 1) * Resources.TILE_DIM + frame.getTop());
            }
        }

        return image;
    }
}
