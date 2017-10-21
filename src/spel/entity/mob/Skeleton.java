package spel.entity.mob;

import spel.graphics.Animation;
import spel.graphics.SpriteSheet;
import spel.level.Level;

public class Skeleton extends Mob {
    public Skeleton(Level level) {
        x = 0;
        y = 0;
        this.level = level;
        init();
    }

    public Skeleton(Level level, int x, int y) {
        this.x = x;
        this.y = y;
        this.level = level;
        init();
    }

    public void init() {
        animation = new Animation(SpriteSheet.skeleton, 36, 72, 4, 0);
        animation.start(200);
        health = 100;
        setHitbox(35, 70);
        xv = 2;
        damage = 10;
    }

    public void update() {
        super.update();
        if (collision((int) x + (int) xv, (int) y)) {
            xv *= -1;
        }
    }

    public void kill() {
        removed = true;
    }
}
