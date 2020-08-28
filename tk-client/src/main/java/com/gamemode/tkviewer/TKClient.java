package com.gamemode.tkviewer;

import com.gamemode.tkviewer.client.GameResources;
import com.gamemode.tkviewer.client.Player;
import com.gamemode.tkviewer.client.PlayerLogic;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.TriggerActivatedListener;
import de.gurkenlabs.litiengine.entities.TriggerEvent;
import de.gurkenlabs.litiengine.entities.TriggerListener;
import de.gurkenlabs.litiengine.environment.EnvironmentEntityListener;
import de.gurkenlabs.litiengine.environment.EnvironmentListener;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.GroupLayer;
import de.gurkenlabs.litiengine.gui.screens.GameScreen;
import de.gurkenlabs.litiengine.resources.Resources;

public class TKClient {

    public static void main(String[] args) {
        Game.info().setName("Nexus");
        Game.info().setSubTitle("");
        Game.info().setVersion("1.0.0");

        Game.init(args);
        Game.graphics().setBaseRenderScale(1.00f);

        GameResources.init();
        PlayerLogic.init(5, 5);

        Game.screens().add(new GameScreen());

        IMap map = Resources.maps().get("maps/000002.tmx");
        Game.world().loadEnvironment(map);

        // Warp Test
        Trigger vale_valley = new Trigger(
                Trigger.TriggerActivation.COLLISION,
                "Vale Valley"
        );
        vale_valley.addActivatedListener(new TriggerActivatedListener() {
            @Override
            public void activated(TriggerEvent triggerEvent) {
                System.out.println("Hey");
            }
        });
        vale_valley.setLocation(30 * 48, 7 * 48);
        vale_valley.setCollision(true);
        vale_valley.canTrigger(Player.instance());
        Game.world().environment().add(vale_valley);

        // End Warp Test

        Game.start();
    }
}
