package spel.entity.mob;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import spel.Game;
import spel.entity.Entity;
import spel.entity.mob.player.Player;
import spel.entity.projectile.Projectile;
import spel.graphics.Animation;
import spel.graphics.Screen;
import spel.graphics.Sprite;
import spel.level.Level;

public abstract class Mob extends Entity {

	public double xv = 0, yv = 0;
	public int hitboxWidth, hitboxHeight;
	public Animation animation;
	protected Animation[] animations;
	public int dir = 1;
	protected int health;
	public boolean onGround = false;
	protected boolean godmode = false;
	public boolean moving = false;
	public long createTime= 0;
	protected long graceTime = 0;
	protected long freezetimer = 0;
	protected int damage = 0;

	protected List<Projectile> projectiles = new ArrayList<Projectile>();

	public Mob() {

	}

	public Mob(Level level) {
		this.level = level;
		init();
	}

	protected void init() {
		createTime = System.currentTimeMillis();
	}

	public void render(Screen screen) {
		screen.renderSprite(animation.getSprite(), (int) x - level.xOffset, Game.HEIGHT - (int) y + level.yOffset - 70, dir);
	}

	public void update() {
		for(Projectile p: projectiles){
			p.update();
			int xp = p.getX()+p.getSprite().width/2;
			int yp = p.getY()+32;
			for(Mob m: level.entitys){
				if(xp >= m.getX() && xp <= m.getX()+m.hitboxWidth){
					if(yp >= m.getY() && yp <= m.getY()+m.hitboxHeight){
						if(!m.dead){
							m.attackThis(this, p.damage, p.knockback, p.freezetime);
							p.remove();
						}
					}
				}
			}
		}
		projectileClear();
		
		if (health <= 0 || dead) {
			kill();
			return;
		}
		
		move(xv, yv);
		if (!onGround && !godmode) {
			yv -= level.gravity;
		}
		if (!(this instanceof Player)) {
			Rectangle r1 = new Rectangle((int) x, (int) y, hitboxWidth, hitboxHeight);
			Rectangle r2 = new Rectangle(level.player.getX(), level.player.getY(), level.player.hitboxWidth, level.player.hitboxHeight);

			if (intersect(r1, r2) && !level.player.dead) {
				level.player.attackThis(this, damage, 20, 100);
			}
		}
		
	}

	public boolean move(double xv, double yv) {
		boolean collision = false;
		if (xv > 0) {
			dir = 1;
		} else if (xv < 0) {
			dir = -1;
		}
		if (freezetimer <= System.currentTimeMillis()) {
			if (!collision((int) (x + xv), (int) y) && xv != 0) {
				x += xv;
				moving = true;
			} else {
				this.xv = 0;
				moving = false;
				collision = true;
			}
		}

		if (!collision((int) x, (int) (y + yv))) {
			y += yv;
		} else {
			if (yv < 0) {
				onGround = true;
				int yt = (int) y;
				while (!collision((int) x, yt - 1)) {
					yt--;
				}
				y = yt;
				collision = true;
			}
			this.yv = 0.0;
		}
		if (onGround && y > 0) {
			if (!collision((int) x, (int) y - 1)) {
				onGround = false;
			}else{
				level.getTile((int)(x) / Level.tileSize, level.height - (int)(y-1) / Level.tileSize - 1).onCollision(this, (int)(x)/Level.tileSize, (int)(y)/Level.tileSize);
				level.getTile((int)(x+hitboxWidth) / Level.tileSize, level.height - (int)(y-1) / Level.tileSize - 1).onCollision(this, (int)(x)/Level.tileSize, (int)(y)/Level.tileSize);
			}
			
		}
		return collision;
	}

	protected boolean collision(int x, int y) {
		if (y < -hitboxHeight) {
			kill();
		}
		if (x + hitboxWidth < 0) {
			return false;
		}
		if (x > level.width * Level.tileSize) {
			return false;
		}
		if (godmode) return false;

		return tileCollision(x, y);
	}

	protected boolean tileCollision(int x, int y) {
		for (int yc = 0; yc < hitboxHeight; yc += 10) {
			for (int xc = 0; xc < hitboxWidth; xc += 10) {
				if (yc > hitboxHeight) yc = hitboxHeight;
				if (xc > hitboxWidth) xc = hitboxWidth;
				if (level.getTile((xc + x) / Level.tileSize, level.height - (y + yc) / Level.tileSize - 1).isSolid()) {
					return true;
				}
			}
		}
		return false;
	}

	public void kill() {
	}

	public void push(int units) {
		if (!collision((int) (x + units), (int) y)) {
			x += units;
		} else {
			if (units < -1) {
				push(units + 1);
			} else if (units > 1) {
				push(units - 1);
			} else {
				return;
			}
		}
	}

	public void freeze(int ms) {
		if (level.time + ms > freezetimer) {
			freezetimer = level.time + ms;
		}
	}

	public void attackThis(Mob attacker, int damage, int knockback, int freezems) {
		if (godmode) return;
		freezetimer = level.time + freezems;
		push(knockback * attacker.dir);
		health -= damage;
		graceTime = level.time + 2000;
	}

	public int getX() {
		return (int) x;
	}

	public int getY() {
		return (int) y;
	}

	public void setX(int x) {
		if (x < 0) {
			this.x = 0;
		} else if (x >= level.width * Level.tileSize) {
			this.x = (level.width - 1) * Level.tileSize;
		} else {
			this.x = x;
		}
	}

	public void setY(int y) {
		this.y = y;
	}

	public boolean isProtected() {
		return graceTime > level.time;
	}

	protected void setHitbox(int width, int height) {
		this.hitboxWidth = width;
		this.hitboxHeight = height;
	}

	public boolean setAnimation(Animation anim, int time) {
		if (!animation.locked && animation != anim) {
			animation = anim;
			animation.start(time);
			return true;
		}
		return false;
	}

	public Sprite getSprite() {
		if (animation == null) return Sprite.err;
		return animation.getSprite();
	}
	
	public void projectileClear() {
		for (int i = 0; i < projectiles.size(); i++) {
			if (projectiles.get(i).isRemoved()) {
				projectiles.remove(i);
			}
		}
	}

}
