package spel.entity.mob;

import spel.graphics.Animation;
import spel.graphics.SpriteSheet;
import spel.level.Level;

public class Boss extends Mob {
	public Boss(Level level,int x, int y){
		this.x = x;
		this.y = y;
		this.level = level;
		animation = new Animation(SpriteSheet.boss, 144,144,4,0);
		animation.start(100);
	}
	
	public void update(){
		super.update();
		yv = 0;
	}
}
