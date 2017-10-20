package game.entity.projectile;

import game.graphics.Animation;
import game.graphics.Sprite;
import game.graphics.SpriteSheet;
import game.level.Level;

public class AxeProjectile extends Projectile {

    private Animation anim;

    public AxeProjectile(int x, int y, int dir, Level level) {
        super(x, y, dir);
        this.level = level;
        ny = level.player.yv;
        init();
    }

    public void init() {
        super.init();
        anim = new Animation(SpriteSheet.player, 36, 72, 8, 10);
        anim.setTime(30);
        anim.start();
        speed = 10;
        range = 700;
        damage = 50;
        knockback = 20;
        freezetime = 100;
    }

    public Sprite getSprite() {
        if (anim != null) {
            return anim.getSprite();
        } else {
            return sprite;
        }
    }

    public void update() {
        super.update();
    }
}
