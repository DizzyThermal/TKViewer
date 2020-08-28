package com.gamemode.tkviewer.client;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.resources.Resources;

import java.nio.file.Paths;

public class GameResources {

    private GameResources() {
    }

    public static void init() {
        // Set Game Icon
        Game.window().setIcon(Resources.images().get("client_icon.png"));

        // Load Static Spritesheets
        Resources.spritesheets().load(Paths.get("sprites", "nexus-idle-up.png").toString(),38,86);
        Resources.spritesheets().load(Paths.get("sprites", "nexus-walk-up.png").toString(),38,86);
        Resources.spritesheets().load(Paths.get("sprites", "nexus-idle-right.png").toString(), 35, 83);
        Resources.spritesheets().load(Paths.get("sprites", "nexus-walk-right.png").toString(), 35, 83);
        Resources.spritesheets().load(Paths.get("sprites", "nexus-idle-down.png").toString(), 36, 86);
        Resources.spritesheets().load(Paths.get("sprites", "nexus-walk-down.png").toString(), 36, 86);
        Resources.spritesheets().load(Paths.get("sprites", "nexus-idle-left.png").toString(), 35, 83);
        Resources.spritesheets().load(Paths.get("sprites", "nexus-walk-left.png").toString(), 35, 83);
    }
}
