package com.gamemode.tkviewer.render;

import com.gamemode.tkviewer.file_handlers.CmpFileHandler;
import com.gamemode.tkviewer.file_handlers.MapFileHandler;
import com.gamemode.tkviewer.resources.Resources;
import com.gamemode.tkviewer.utilities.Utils;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class MapRenderer {

    public TileRenderer tileRenderer;
    public SObjRenderer sObjRenderer;

    public MapRenderer() {
        this.tileRenderer = new TileRenderer("tile");
        this.sObjRenderer = new SObjRenderer();
    }

    public MapRenderer(TileRenderer tileRenderer, SObjRenderer sObjRenderer) {
        this.tileRenderer = tileRenderer;
        this.sObjRenderer = sObjRenderer;
    }

    public BufferedImage renderCropped(int mapId, int x, int y, int width, int height) {
        CmpFileHandler cmp = new CmpFileHandler(Resources.NTK_MAP_DIRECTORY + File.separator + "TK" + Utils.pad(mapId, 6) + ".cmp");

        return renderCropped(cmp, x, y, width, height);
    }

    // Overrenders downwards to get all static objects
    public BufferedImage renderCropped(CmpFileHandler cmpFileHandler, int x, int y, int width, int height) {
        BufferedImage image = new BufferedImage((width * Resources.TILE_DIM), (height * Resources.TILE_DIM), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphicsObject = image.createGraphics();
        graphicsObject.setColor(Color.BLACK);

        int originalHeight = height;
        height += 10;
        if (y + height > cmpFileHandler.mapHeight) {
            height = cmpFileHandler.mapHeight - y;
        }
        for (int i = x; i < (x + width); i++) {
            for (int j = y; j < (y + height); j++) {
                int tileIndex = cmpFileHandler.mapTiles.get(cmpFileHandler.getIndex(i, j)).getAbTile();
                if (tileIndex >= 0) {
                    graphicsObject.drawImage(
                            this.tileRenderer.renderTile(tileIndex + 1),
                            null,
                            (i - x) * Resources.TILE_DIM,
                            (j - y) * Resources.TILE_DIM);
                } else {
                    BufferedImage transparent = new BufferedImage((width * Resources.TILE_DIM),
                            (height * Resources.TILE_DIM), BufferedImage.TYPE_INT_ARGB);
                    graphicsObject.drawImage(
                            transparent,
                            null,
                            (i - x) * Resources.TILE_DIM,
                            (j - y) * Resources.TILE_DIM);
                }

                // Render Static Object (C Tile)
                int sObjIndex = cmpFileHandler.mapTiles.get(cmpFileHandler.getIndex(i, j)).getSObjTile();
                if (sObjIndex > 0) {
                    int sObjHeight = this.sObjRenderer.tileSObjTbl.objects.get(sObjIndex).getHeight();
                    if (sObjHeight > 0) {
                        graphicsObject.drawImage(
                                this.sObjRenderer.renderSObject(sObjIndex),
                                null,
                                (i - x) * Resources.TILE_DIM,
                                (j - y - sObjHeight + 1) * Resources.TILE_DIM);
                    }
                }
            }
        }

        return image.getSubimage(0, 0, width * Resources.TILE_DIM, originalHeight * Resources.TILE_DIM);
    }

    public BufferedImage renderMap(int mapId) {
        CmpFileHandler cmp = new CmpFileHandler(Resources.NTK_MAP_DIRECTORY + File.separator + "TK" + Utils.pad(mapId, 6) + ".cmp");

        return renderMap(cmp);
    }

    public BufferedImage renderMap(CmpFileHandler cmpFileHandler) {
        int width = cmpFileHandler.mapWidth;
        int height = cmpFileHandler.mapHeight;

        BufferedImage image = new BufferedImage((width * Resources.TILE_DIM), (height * Resources.TILE_DIM), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphicsObject = image.createGraphics();
        graphicsObject.setColor(Color.BLACK);
        int depth = 0;
        int length = 0;
        for (int i = 0; i < (width * height); i++) {
            // Render Ground Tile (AB Tile)
            int tileIndex = cmpFileHandler.mapTiles.get(i).getAbTile();
            if (tileIndex >= 0) {
                graphicsObject.drawImage(
                        this.tileRenderer.renderTile(tileIndex + 1),
                        null,
                        (length * Resources.TILE_DIM),
                        (depth * Resources.TILE_DIM));
            } else {
                graphicsObject.fillRect(
                        (length * Resources.TILE_DIM),
                        (depth * Resources.TILE_DIM),
                        Resources.TILE_DIM, Resources.TILE_DIM);
            }

            // Render Static Object (C Tile)
            int sObjIndex = cmpFileHandler.mapTiles.get(i).getSObjTile();
            if (sObjIndex > 0) {
                int sObjHeight = this.sObjRenderer.tileSObjTbl.objects.get(sObjIndex).getHeight();
                if (sObjHeight > 0) {
                    graphicsObject.drawImage(
                            this.sObjRenderer.renderSObject(sObjIndex),
                            null,
                            (length * Resources.TILE_DIM),
                            (depth - sObjHeight + 1) * Resources.TILE_DIM);
                }
            }

            if ((((i + 1) % width) == 0) && (i != 0)) {
                depth += 1;
                length = 0;
            } else {
                length += 1;
            }
        }

        return image;
    }

    public BufferedImage renderMap(MapFileHandler mapFileHandler) {
        int width = mapFileHandler.mapWidth;
        int height = mapFileHandler.mapHeight;

        BufferedImage image = new BufferedImage((width * Resources.TILE_DIM), (height * Resources.TILE_DIM), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphicsObject = image.createGraphics();
        graphicsObject.setColor(Color.BLACK);
        int depth = 0;
        int length = 0;
        for (int i = 0; i < (width * height); i++) {
            // Render Ground Tile (AB Tile)
            int tileIndex = mapFileHandler.mapTiles.get(i).getAbTile();
            if (tileIndex > 0) {
                graphicsObject.drawImage(
                        this.tileRenderer.renderTile(tileIndex + 1),
                        null,
                        (length * Resources.TILE_DIM),
                        (depth * Resources.TILE_DIM));
            } else {
                graphicsObject.fillRect(
                        (length * Resources.TILE_DIM),
                        (depth * Resources.TILE_DIM),
                        Resources.TILE_DIM, Resources.TILE_DIM);
            }

            // Render Static Object (C Tile)
            int sObjIndex = mapFileHandler.mapTiles.get(i).getSObjTile();
            if (sObjIndex > 0) {
                int sObjHeight = this.sObjRenderer.tileSObjTbl.objects.get(sObjIndex).getHeight();
                if (sObjHeight > 0) {
                    graphicsObject.drawImage(
                            this.sObjRenderer.renderSObject(sObjIndex),
                            null,
                            (length * Resources.TILE_DIM),
                            (depth - sObjHeight + 1) * Resources.TILE_DIM);
                }
            }

            if ((((i + 1) % width) == 0) && (i != 0)) {
                depth += 1;
                length = 0;
            } else {
                length += 1;
            }
        }

        return image;
    }

    public Boolean[][] renderMapStencil(CmpFileHandler cmpFileHandler) {
        int width = cmpFileHandler.mapWidth * 2 + 1;
        int height = cmpFileHandler.mapHeight * 2 + 1;

        Boolean[][] gridNodes = new Boolean[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                gridNodes[i][j] = false;
            }
        }
        // Left and Right borders
        for (int i = 0; i < height; i++) {
            gridNodes[0][i] = true;
            gridNodes[width-1][i] = true;
        }
        // Top and Bottom borders
        for (int i = 0; i < width; i++) {
            gridNodes[i][0] = true;
            gridNodes[i][height-1] = true;
        }
        // Diagonal borders
        for(int i = 2; i < width-3; i+=2) {
            for (int j = 2; j < height-3; j+=2) {
                gridNodes[i][j] = true;
            }
        }

        int mapIndex = 0;

        for (int j = 1; j < height-1; j+=2) {
            for (int i = 1; i < width-1; i+=2) {
                // Render Static Object (C Tile)
                int sObjIndex = cmpFileHandler.mapTiles.get(mapIndex).getSObjTile();
                int passableTile = cmpFileHandler.mapTiles.get(mapIndex++).getPassableTile();
                if (passableTile == 1) {
                    // Block out adjacent nodes
                    // Left, Right, Top, Bottom
                    gridNodes[i - 1][j] = true;
                    gridNodes[i + 1][j] = true;
                    gridNodes[i][j - 1] = true;
                    gridNodes[i][j + 1] = true;
                    gridNodes[i][j] = true;
                }
                if (sObjIndex > 0) {
                    byte movementDirection = this.sObjRenderer.tileSObjTbl.objects.get(sObjIndex).getMovementDirection();
                    if (movementDirection == 0xF) {         // Full
                        // Block out adjacent nodes
                        // Left, Right, Top, Bottom
                        gridNodes[i - 1][j] = true;
                        gridNodes[i + 1][j] = true;
                        gridNodes[i][j - 1] = true;
                        gridNodes[i][j + 1] = true;
                        gridNodes[i][j] = true;
                    } else if (movementDirection == 0x8) {  // Right
                        gridNodes[i + 1][j] = true;
                    } else if (movementDirection == 0x4) {  // Left
                        gridNodes[i - 1][j] = true;
                    } else if (movementDirection == 0x2) {  // Top
                        gridNodes[i][j - 1] = true;
                    } else if (movementDirection == 0x1) {  // Bottom
                        gridNodes[i][j + 1] = true;
                    }
                }
            }
        }

        return gridNodes;
    }

    public void dispose() {
        tileRenderer.dispose();
        sObjRenderer.dispose();
    }
}
