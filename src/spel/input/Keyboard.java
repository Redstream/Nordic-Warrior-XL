package spel.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import spel.Game;

public class Keyboard implements KeyListener, MouseMotionListener {

    public final int JUMP_KEY = KeyEvent.VK_SPACE;
    public final int RIGHT_KEY = KeyEvent.VK_D;
    public final int RIGHT_KEY_2 = KeyEvent.VK_RIGHT;
    public final int LEFT_KEY = KeyEvent.VK_A;
    public final int LEFT_KEY_2 = KeyEvent.VK_LEFT;
    public final int ATTACK_KEY = KeyEvent.VK_X;
    public final int DOWN_KEY = KeyEvent.VK_S;
    public final int WALK_KEY = KeyEvent.VK_SHIFT;
    public final int RESTART_KEY = KeyEvent.VK_R;
    public final int ESC_KEY = KeyEvent.VK_ESCAPE;
    public final int PAUSE_KEY = KeyEvent.VK_ESCAPE;

    public int mouseX = 0 ,mouseY = 0;
	
	private boolean[] keys = new boolean[256 * 256];
    public boolean jump, left, right, walk, down, restart, attack, esc, paused = false;
	private boolean attackLock = false;
	private final Game game;

	public Keyboard(Game game) {
	    this.game = game;
	}

	public void update() {
		jump = keys[JUMP_KEY]; // || keys[KeyEvent.VK_W];
		right = keys[RIGHT_KEY] || keys[RIGHT_KEY_2]; //|| keys[KeyEvent.VK_D];
		left = keys[LEFT_KEY] || keys[LEFT_KEY_2]; //|| keys[KeyEvent.VK_A];
		down = keys[DOWN_KEY]; //|| keys[KeyEvent.VK_S];
		walk = keys[WALK_KEY];
		restart = keys[RESTART_KEY];
		
		if (esc) {
			Game.game.togglePause();
			esc = false;
		}
	}

	
	// Should move logic out of function
	@Override
	public void keyPressed(KeyEvent e) {
		keys[e.getKeyCode()] = true;

		if (e.getKeyCode() == ESC_KEY) {
			esc = true;
		} 
		else if(e.getKeyCode() == PAUSE_KEY){
			game.togglePause2();
		}
		else if(e.getKeyCode() >= 49 && e.getKeyCode() <= 49 + 2){
			Game.game.level.mobs.add(Game.game.level.getMob(e.getKeyCode()-49,(Game.game.level.player.getX()+100*Game.game.level.player.dir),(Game.game.level.player.getY()+100)));
		}else if(e.getKeyCode() == ATTACK_KEY && !attackLock){
			attack = true;
			attackLock = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keys[e.getKeyCode()] = false;
		if(e.getKeyCode() == ATTACK_KEY){
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
