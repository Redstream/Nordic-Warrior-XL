package spel.graphics.Tile;

import spel.graphics.Animation;
import spel.graphics.Sprite;

public class AnimatedTile extends Tile{
	private Animation animation;
	private int time = 150;
	
	public AnimatedTile(Animation animation){
		this.animation = animation;
		animation.start(time);
		width = animation.getSprite().getWidth();
		height = animation.getSprite().getHeight();
	}
	
	public Sprite getSprite(){
		return animation.getSprite();
	}
	
	public void setTime(int time){
		this.time = time; 
		animation.start(time);
	}
}
