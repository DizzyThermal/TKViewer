package com.gamemode.tkviewer.client;

import de.gurkenlabs.litiengine.entities.CollisionInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.EntityInfo;
import de.gurkenlabs.litiengine.entities.MovementInfo;
import de.gurkenlabs.litiengine.physics.IMovementController;


@EntityInfo(width = 35, height = 83)
@MovementInfo(velocity = 250)
@CollisionInfo(collisionBoxWidth = 24, collisionBoxHeight = 24, collision = true)
public class Player extends Creature {

    private static Player instance;

    private Player() {
        super("nexus");
    }

    public static Player instance() {
        if (instance == null) {
            instance = new Player();
        }

        return instance;
    }

    @Override
    protected IMovementController createMovementController() {
        // setup movement controller
        return new GridController<>(this);
    }
}
