package spel.entity.mob;

import java.awt.Rectangle;

import spel.Game;
import spel.entity.Entity;
import spel.graphics.Animation;
import spel.graphics.SpriteSheet;
import spel.level.Level;

public class Goblin extends Mob{
	public int blastRadius = 100;
	public long destructTime = 2000;
	public long explodedTime = 0;
	public static int exploding = 0;
	
	
	public Goblin(Level level){
		x = 100;
		y = 100;
		this.level = level;
		init();
	}
	
	public Goblin(Level level, int x, int y){
		this.x = x;
		this.y = y;
		this.level = level;
		
		init();
	}
	

	public void init(){
		xv = 3;
		damage = 0;
		health = 1;
		setHitbox(72, 35);
		animations = new Animation [2];
		animations[0] = new Animation(SpriteSheet.goblin,72,72,4,0);
		animations[1] = new Animation(SpriteSheet.goblin,72,72,1,2);
		animations[1].locked = true;
		animations[1].requireFinnish = true;
		animation = animations[0];
		animation.start(200);
	}
	
	public void update() {
		if(removed) return;
		if(dead){
			if(level.time - explodedTime > destructTime){
				exploding--;
				removed = true;
				level.shake = 0;
			}else{
				level.shake = (int)(10/(((level.time - explodedTime)/100)+1)*(exploding+1));
			}
			return;
		}
		if(collision((int)x+(int)xv, (int)y) ){
			xv *= -1;
		}
		move(xv, yv);
		if (!onGround && !godmode) {
			yv -= level.gravity;
		}
		
		Rectangle r1 = new Rectangle((int) x, (int) y, hitboxWidth, hitboxHeight);
		Rectangle r2 = new Rectangle(level.player.getX(), level.player.getY(), level.player.hitboxWidth, level.player.hitboxHeight);
		if (intersect(r1, r2) && !level.player.dead) {
			kill();
		}
		

	}
	
	public void attackThis(Mob attacker, int damage, int knockback, int freezems){
		if(dead) return;
		kill();
		
	}

	public void kill(){
		if(dead) return;
		dead = true;
		selfDestruct();
		explodedTime = level.time;
	}
	
	
	
	public void selfDestruct(){
		exploding ++;
		destructTime += 10 * Game.random.nextInt(50);
		setAnimation(animations[1],400);
		int eX,eY;
		for (Entity e : level.entitys) {		
			eX = e.getX()+hitboxWidth/2;
			eY = e.getY()+hitboxHeight/2;
			if(eX > x+hitboxWidth/2-blastRadius && eX < x+hitboxWidth/2+blastRadius){
				if(eY > y+hitboxHeight/2-blastRadius && eY < y+hitboxHeight/2+blastRadius){
					if(e != this && !e.dead){
						e.kill();
					}
				}
			}
		}
		eX = level.player.getX()+hitboxWidth/2;
		eY = level.player.getY()+hitboxHeight/2;
		if(level.player.dead) return;
		if(eX > x+hitboxWidth/2-blastRadius && eX < x+hitboxWidth/2+blastRadius){
			if(eY > y+hitboxHeight/2-blastRadius && eY < y+hitboxHeight/2+blastRadius){
					level.player.kill();
			}
		}
		xv = 0;
	}
}
