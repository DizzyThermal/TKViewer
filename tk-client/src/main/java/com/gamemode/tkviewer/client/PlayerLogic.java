package com.gamemode.tkviewer.client;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.graphics.Camera;
import de.gurkenlabs.litiengine.graphics.PositionLockCamera;

public class PlayerLogic {

    private PlayerLogic() {
    }

    public static void init() {
        PlayerLogic.init(2, 2);
    }

    public static void init(int spawnX, int spawnY) {
        // we'll use a camera in our game that is locked to the location of the player
        Camera camera = new PositionLockCamera(Player.instance());
        camera.setClampToMap(true);
        Game.world().setCamera(camera);

        // set a basic gravity for all levels.
        Game.world().setGravity(0);

        // add default game logic for when a level was loaded
        Game.world().onLoaded(e -> {
            // spawn the player instance
            new Spawnpoint(spawnX * 48, (spawnY - 1) * 48).spawn(Player.instance());
        });
    }
}
