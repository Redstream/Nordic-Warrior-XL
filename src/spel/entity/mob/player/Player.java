package spel.entity.mob.player;

import spel.Game;
import spel.entity.mob.Mob;
import spel.entity.projectile.AxeProjectile;
import spel.entity.projectile.Projectile;
import spel.graphics.Animation;
import spel.graphics.Screen;
import spel.graphics.Sprite;
import spel.graphics.SpriteSheet;
import spel.input.Keyboard;
import spel.level.Level;

import java.awt.*;

public class Player extends Mob {

    public Keyboard key;
    private Animation animations[] = new Animation[4];
    private long axeTimer;
    public Rectangle hitBox;
    public int xOrigin, yOrigin;
    private Point  spawn;

    public Player(int x, int y) {
        spawn = new Point(x,y);
        this.x = xOrigin = x;
        this.y = yOrigin = y;
        init();
    }

    public Player(Keyboard key, Level level, int x, int y) {
        this.level = level;
        this.key = key;
        spawn = new Point(x,y);
        this.x = xOrigin = x;
        this.y = yOrigin = y;
        init();
    }

    /**
     * Create animations, set health.
     */
    public void init() {
        animations[1] = new Animation(SpriteSheet.player, 36, 72, 4, 0); //Move
        animations[0] = new Animation(SpriteSheet.player, 36, 72, 5, 12); //Stance
        animations[2] = new Animation(SpriteSheet.player, 108, 72, 2, 8); //AxeThrow
        animations[3] = new Animation(SpriteSheet.player, 36, 72, 8, 14); //Death
        animations[3].locked = true;
        animations[3].requireFinnish = true;
        animations[2].locked = true;
        animations[2].requireFinnish = true;
        animations[0].start(150);
        animation = animations[0];
        health = 30;
        setHitbox(30, 65);
        hitBox = new Rectangle((int) x, (int) y, hitboxWidth, hitboxHeight);
    }

    private void throwAxe() {
        long cooldown = 600;
        long now = level.time;
        if (now > axeTimer) {
            Projectile p = new AxeProjectile((int) x, (int) y, dir, level);
            projectiles.add(p);
            axeTimer = now + cooldown;
        }
    }

    public void render(Screen screen) {
        // Used to be able to have the camera follow the character after this height have been reached.
        double startFollow = Game.HEIGHT * 3 / 5.0;
        if(y > startFollow) {
            int diff = (int) (startFollow - y);
            screen.offset.setLocation(0, -Game.HEIGHT/2);
            screen.renderSprite(getSprite(), Game.WIDTH / 2 + dir * -1 * (getSprite().getWidth() / 2 - 18), Game.HEIGHT / 2 - (int)startFollow - 70, dir);
            screen.offset.setLocation((int) x - Game.WIDTH / 2, diff);
        } else {
            screen.offset.setLocation(0, -Game.HEIGHT/2 );
            screen.renderSprite(getSprite(), Game.WIDTH / 2 + dir * -1 * (getSprite().getWidth() / 2 - 18), Game.HEIGHT / 2 - (int)y - 70, dir);
            screen.offset.setLocation((int) x - Game.WIDTH / 2, 0);
        }


        for (int i = 0; i < projectiles.size(); i++) {
            projectiles.get(i).render(screen);
        }
    }

    public void attackThis(Mob attacker, int damage, int knockback, int freezems) {
        if (graceTime < level.time) {
            super.attackThis(attacker, damage, knockback, freezems);
            graceTime = level.time + 2000;
        }
    }

    public Sprite getSprite() {
        Sprite sprite = animation.getSprite();
        sprite.red = isProtected() ? 70 : 0;
        return sprite;
    }

    public void update() {
        super.update();
        if (dead) {
            if (!animation.locked) {
                level.resetLevel();
            }
            move(0, yv);
            return;
        }

        if (key == null) return;

        if (key.restart) {
            xOrigin = level.spawnX;
            yOrigin = level.spawnY;
            this.level.resetLevel();
            return;
        }

        hitBox.setLocation((int) x, (int) y);

        if (key.left || key.right) {
            dir = key.left ? -1 : 1;
            xv = 4 * dir;
            moving = true;
            setAnimation(animations[1], 150);
        } else {
            xv = 0.0; //Deceleration? ,(Math.abs(xv) < 0.5 ? 0.0 : xv * 0.9);
            setAnimation(animations[0], 150);
        }
        if (key.jump && (onGround || godmode)) {
            yv = 8;
            onGround = false;
        } else if (key.down && godmode) {
            yv = -8;
        } else if (godmode) {
            yv = 0;
        }

        if (key.attack && axeTimer < level.time) {
            key.attack = false;
            throwAxe();
            setAnimation(animations[2], 200);
        }
    }

    public void kill() {
        if (godmode || dead) return;
        dead = true;
        animation = animations[3];
        animation.start(150);
    }
}
