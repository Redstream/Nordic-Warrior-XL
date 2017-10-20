package spel.entity;

import spel.Log;
import spel.entity.mob.Mob;
import spel.graphics.Screen;
import spel.graphics.Sprite;
import spel.level.Level;

import java.awt.*;

public abstract class Entity implements Cloneable {

    protected Sprite sprite;
    protected Level level;
    public double x = 0, y = 0;
    public double vx = 0, vy = 0;
    public int width = 0, height = 0;
    protected boolean removed = false;
    protected boolean affectedByGravity = true;
    protected boolean canCollide = true;

    public abstract void update();

    public abstract void render(Screen screen);

    // Change to use this
    protected void move() {
        x += vx;
        y += vy;
    }

    public void push(int units) {
        this.x += units;
    }

    protected boolean collision(int x, int y) {
        if (level.getTile(x / Level.tileSize, level.height - y / Level.tileSize - 1).isSolid()) {
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

    public void setX(int x) {
        this.x = Math.max(0, Math.min(x, (level.width - 1) * Level.tileSize));
    }

    public void setY(int y) {
        this.y = Math.max(0, Math.min(y, (level.height - 1) * Level.tileSize));
    }

    public boolean isAffectedByGravity() {
        return affectedByGravity;
    }

    public boolean canCollide() {
        return canCollide;
    }

    public Entity clone() {
        try {
            return (Mob) super.clone();
        } catch (CloneNotSupportedException e) {
            Log.log(Log.WARNING, "Could not clone Mob");
            return null;
        }
    }

    // Why use this?
    public static boolean intersect(Rectangle r1, Rectangle r2) {
        return (((r1.x >= r2.x) && (r1.x < (r2.x + r2.getWidth()))) || ((r2.x >= r1.x) && (r2.x < (r1.x + r1.getWidth())))) &&
                (((r1.y >= r2.y) && (r1.y < (r2.y + r2.getHeight()))) || ((r2.y >= r1.y) && (r2.y < (r1.y + r1.getHeight()))));
    }
}
