package spel.input;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import spel.Game;

public class Keyboard implements KeyListener, MouseMotionListener {

	public int mouseX = 0 ,mouseY = 0;
	
	private boolean[] keys = new boolean[256 * 256];
	public boolean jump, left, right, walk, down, restart, attack, paused = false;
	boolean attackLock = false;
	public boolean esc;
	long lastTime = 0;

	public Keyboard() {
	}

	public void update() {
		jump = keys[KeyEvent.VK_UP]; // || keys[KeyEvent.VK_W];
		right = keys[KeyEvent.VK_RIGHT]; //|| keys[KeyEvent.VK_D];
		left = keys[KeyEvent.VK_LEFT]; //|| keys[KeyEvent.VK_A];
		down = keys[KeyEvent.VK_DOWN]; //|| keys[KeyEvent.VK_S];
		walk = keys[KeyEvent.VK_SHIFT];
		restart = keys[KeyEvent.VK_R];
		
		if (esc) {
			Game.game.togglePause();
			esc = false;
		}
	}

	
	
	@Override
	public void keyPressed(KeyEvent e) {
		keys[e.getKeyCode()] = true;

		if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
			esc = true;
		} 
		else if(e.getKeyCode() == KeyEvent.VK_P){
			Game.game.togglePause2();
		}
		else if(e.getKeyCode() >= 49 && e.getKeyCode() <= 49 + 2){
			Game.game.level.add(Game.game.level.getMob(e.getKeyCode()-49,(int)(Game.game.level.player.getX()+100*Game.game.level.player.dir), (int)(Game.game.level.player.getY()+100)));
		}else if(e.getKeyCode() == KeyEvent.VK_SPACE && !attackLock){
			attack = true;
			attackLock = true;
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		keys[e.getKeyCode()] = false;
		if(e.getKeyCode() == KeyEvent.VK_SPACE){
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
