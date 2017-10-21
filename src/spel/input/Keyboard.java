package spel.input;

import spel.Game;
import spel.Settings;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

public class Keyboard implements KeyListener, MouseMotionListener {


    public int mouseX = 0, mouseY = 0;

    private boolean[] keys = new boolean[256 * 256];
    public boolean jump, left, right, walk, down, restart, attack, esc, paused = false;
    private boolean attackLock = false;
    private final Game game;

    public Keyboard(Game game) {
        this.game = game;
    }

    public void update() {
        jump = keys[Settings.JUMP_KEY];
        right = keys[Settings.RIGHT_KEY] || keys[Settings.RIGHT_KEY_2];
        left = keys[Settings.LEFT_KEY] || keys[Settings.LEFT_KEY_2];
        down = keys[Settings.DOWN_KEY];
        walk = keys[Settings.WALK_KEY];
        //esc = keys[ESC_KEY];
        restart = keys[Settings.RESTART_KEY];


        if (esc) {
            Game.game.togglePause();
            esc = false;
        }
        if (keys[Settings.CHECKPOINT_KEY]) {
            game.level.setCheckPoint();
        }
    }


    // Should move logic out of function ?
    @Override
    public void keyPressed(KeyEvent e) {
        keys[e.getKeyCode()] = true;

        if (e.getKeyCode() == Settings.ESC_KEY) {
            esc = true;
        } else if (e.getKeyCode() == Settings.PAUSE_KEY) {
            game.togglePause2();
        } else if (e.getKeyCode() >= 49 && e.getKeyCode() <= 49 + 2) {
            Game.game.level.mobs.add(Game.game.level.getMob(e.getKeyCode() - 49, (Game.game.level.player.getX() + 100 * Game.game.level.player.dir), (Game.game.level.player.getY() + 100)));
        } else if (e.getKeyCode() == Settings.ATTACK_KEY && !attackLock) {
            attack = true;
            attackLock = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keys[e.getKeyCode()] = false;
        if (e.getKeyCode() == Settings.ATTACK_KEY) {
            attackLock = false;
        }

    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }


}
