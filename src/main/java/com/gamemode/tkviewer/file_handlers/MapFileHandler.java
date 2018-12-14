package com.gamemode.tkviewer.file_handlers;

import com.gamemode.tkviewer.resources.Tile;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class MapFileHandler extends FileHandler {

    public int mapWidth;
    public int mapHeight;

    public List<Tile> mapTiles;

    public MapFileHandler(String filepath) {
        this(new File(filepath));
    }

    public MapFileHandler(File file) {
        super(file);

        long dimensions = this.readInt(false, true);
        this.mapWidth = (int) dimensions >> 0x10;
        this.mapHeight = (int) dimensions & 0x0000FFFF;

        boolean includesPassableTiles = false;
        long dataLength = this.file.length() - this.filePosition;
        if ((this.mapWidth * this.mapHeight * 6) == dataLength) {
            includesPassableTiles = true;
        }

        this.mapTiles = new ArrayList<Tile>();
        for (int i = 0; i < (this.mapWidth * this.mapHeight); i++) {
            int abTile = this.readShort(false, true) - 1;

            int passableTile = -1;
            if (includesPassableTiles) {
                passableTile = this.readShort(false, true);
            }

            int sObjTile = this.readShort(false, true) - 1;

            this.mapTiles.add(new Tile(abTile, passableTile, sObjTile));
        }

        this.close();
    }
}
