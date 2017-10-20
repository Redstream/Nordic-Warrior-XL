package game.entity.projectile;

import game.Settings;
import game.entity.Entity;
import game.graphics.Screen;
import game.graphics.Sprite;

public class Projectile extends Entity {

    protected Sprite sprite;
    protected double xOrigin, yOrigin;
    protected double nx, ny = 0;
    protected double speed, range;
    public int damage, knockback, freezetime;
    protected int dir;

    public Projectile(int x, int y, int dir) {
        this.dir = dir;
        this.x = x;
        this.y = y;
        init();
    }

    public void init() {
        xOrigin = x;
        yOrigin = y;
    }

    public void update() {
        if (collision((int) getX() + 18, (int) getY() + 32)) {
            remove();
        }
        if (Math.abs(xOrigin - x) > range) {
            removed = true;
        } else {
            move();
        }

    }

    protected void move() {
        //ny -= level.gravity/10;
        nx = dir * speed;
        x += nx;
        y += ny;
    }

    public void render(Screen screen) {
        screen.renderSprite(getSprite(), (int) x , Settings.HEIGHT - (int) y - 75, dir);
    }

    public Sprite getSprite() {
        return sprite;
    }

}
