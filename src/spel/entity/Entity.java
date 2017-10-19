package spel.entity;

import java.awt.Rectangle;

import spel.entity.mob.Mob;
import spel.graphics.Screen;
import spel.graphics.Sprite;
import spel.level.Level;

public abstract class  Entity implements Cloneable{

	protected boolean removed = false;
	protected Sprite sprite;
	protected Level level;
	public double x = 0, y = 0;
	public int hitboxWidth = 0, hitboxHeight = 0;
	public boolean dead = false;

	public void attackThis(Mob attacker, int damage, int knockback, int freezems) {}

	public abstract void update();

    public abstract void render(Screen screen);

    public void kill(){
        removed = true;
    }

	public void push(int units){
        this.x += units;
    }

	public static boolean intersect(Rectangle r1, Rectangle r2) {
		  return (((r1.x >= r2.x) && (r1.x < (r2.x + r2.getWidth()))) || ((r2.x >= r1.x) && (r2.x < (r1.x + r1.getWidth())))) &&
		  (((r1.y >= r2.y) && (r1.y < (r2.y + r2.getHeight()))) || ((r2.y >= r1.y) && (r2.y < (r1.y + r1.getHeight()))));
	}

	protected boolean collision(int x, int y) {
		if (level.getTile( x / Level.tileSize, level.height - y / Level.tileSize - 1).isSolid()) {
			return true;
		}
		return false;
	}

	public void remove() {
		removed = true;
	}

	public boolean isRemoved() {
		return removed;
	}

	public int getX() {
		return (int) x;
	}

	public int getY() {
		return (int) y;
	}

	public Entity clone() throws CloneNotSupportedException{
	    return (Entity) super.clone();
    }
}
