package com.gamemode.tkviewer.client;

import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.input.KeyboardEntityController;
import de.gurkenlabs.litiengine.physics.MovementController;
import de.gurkenlabs.litiengine.util.ListUtilities;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GridController<T extends IMobileEntity> extends MovementController<T> {
    private final Integer up;
    private final Integer down;
    private final Integer left;
    private final Integer right;
    private Integer currentKey;

    public GridController(T entity) {
        this(entity, KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
    }

    public GridController(T entity, int up, int down, int left, int right) {
        super(entity);
        this.up = up;
        this.down = down;
        this.left = left;
        this.right = right;
        this.currentKey = this.down;
        Input.keyboard().onKeyPressed(this::handlePressedKey);
        Input.keyboard().onKeyReleased(this::handleReleasedKey);
    }

    public boolean areOtherKeysPressed(int keyCode) {
        if (this.up == keyCode) {
            if (Input.keyboard().isPressed(this.right) || Input.keyboard().isPressed(this.left) || Input.keyboard().isPressed(this.down)) {
                return true;
            }
        } else if (this.right == keyCode) {
            if (Input.keyboard().isPressed(this.up) || Input.keyboard().isPressed(this.left) || Input.keyboard().isPressed(this.down)) {
                return true;
            }
        } else if (this.down == keyCode) {
            if (Input.keyboard().isPressed(this.right) || Input.keyboard().isPressed(this.left) || Input.keyboard().isPressed(this.up)) {
                return true;
            }
        } else if (this.left == keyCode) {
            if (Input.keyboard().isPressed(this.right) || Input.keyboard().isPressed(this.up) || Input.keyboard().isPressed(this.down)) {
                return true;
            }
        }

        return false;
    }

    public void setLocation(double x, double y) {
        Player.instance().setLocation(x * 48, y  * 48);
    }

    public void handlePressedKey(KeyEvent keyCode) {
        if (this.up == keyCode.getKeyCode()) {
            if (!this.up.equals(this.currentKey) && this.areOtherKeysPressed(this.up)) {
                return;
            }
            this.currentKey = this.up;
            this.setDy(this.getDy() - 1.0F);
        } else if (this.down == keyCode.getKeyCode()) {
            if (!this.down.equals(this.currentKey) && this.areOtherKeysPressed(this.down)) {
                return;
            }
            this.currentKey = this.down;
            this.setDy(this.getDy() + 1.0F);
        } else if (this.left == keyCode.getKeyCode()) {
            if (!this.left.equals(this.currentKey) && this.areOtherKeysPressed(this.left)) {
                return;
            }
            this.currentKey = this.left;
            this.setDx(this.getDx() - 1.0F);
        } else if (this.right == keyCode.getKeyCode()) {
            if (!this.right.equals(this.currentKey) && this.areOtherKeysPressed(this.right)) {
                return;
            }
            this.currentKey = this.right;
            this.setDx(this.getDx() + 1.0F);
        } else if (keyCode.getKeyCode() == KeyEvent.VK_NUMPAD4) {
            setLocation(6, 32);
        } else if (keyCode.getKeyCode() == KeyEvent.VK_NUMPAD6) {
            setLocation(53, 32);
        } else if (keyCode.getKeyCode() == KeyEvent.VK_NUMPAD8) {
           setLocation(30, 10);
        } else if (keyCode.getKeyCode() == KeyEvent.VK_NUMPAD2) {
            setLocation(30, 42);
        }
    }

    public void handleReleasedKey(KeyEvent keyCode) {
        if (keyCode.getKeyCode() == KeyEvent.VK_SPACE) {
            Float velocity = Player.instance().getVelocity().get();
            if (velocity == 250) {
                Player.instance().setVelocity(500);
            } else {
                Player.instance().setVelocity(250);
            }

        }
    }
}
