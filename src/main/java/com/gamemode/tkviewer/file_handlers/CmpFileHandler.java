package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Tile;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class CmpFileHandler extends FileHandler {

    public int mapWidth;
    public int mapHeight;

    public List<Tile> mapTiles;

    public CmpFileHandler(String filepath) {
        this(new File(filepath));
    }

    public CmpFileHandler(File file) {
        super(file);

        // CMAP
        this.seek(4, true);

        long dimensions = this.readInt(true, true);
        this.mapWidth = (int) dimensions & 0x0000FFFF;
        this.mapHeight = (int) dimensions >> 0x10;

        ByteBuffer data = this.readCompressed(true);

        this.mapTiles = new ArrayList<Tile>();
        for (int i = 0; i < (data.capacity() / 6); i++) {
            int idx = (i * 6);
            int abTile = (data.getShort(idx) & 0xffff) - 1;
            int passableTile = data.getShort(idx + 2);
            int sObjTile = (data.getShort(idx + 4) & 0xffff) - 1;

            this.mapTiles.add(new Tile(abTile, passableTile, sObjTile));
        }

        this.close();
    }
}
