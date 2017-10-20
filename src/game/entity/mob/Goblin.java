package game.entity.mob;

import game.graphics.Animation;
import game.graphics.SpriteSheet;
import game.level.Level;

import java.awt.*;
import java.util.Random;

public class Goblin extends Mob {
    private int blastRadius = 100;
    private long destructTime = 2000;
    private long explodedTime = 0;
    private static int exploding = 0;
    private Random rand = new Random();

    public Goblin(Level level, int x, int y) {
        this.x = x;
        this.y = y;
        this.level = level;
        init();
    }

    public void init() {
        xv = 3;
        damage = 0;
        health = 1;
        setHitbox(72, 35);
        animations = new Animation[2];
        animations[0] = new Animation(SpriteSheet.goblin, 72, 72, 4, 0);
        animations[1] = new Animation(SpriteSheet.goblin, 72, 72, 1, 2);
        animations[1].locked = true;
        animations[1].requireFinnish = true;
        animation = animations[0];
        animation.start(200);
    }

    @Override
    public void update() {
        if (removed) return;
        if (dead) {
            if (level.time - explodedTime > destructTime) {
                exploding--;
                removed = true;
                level.shake = 0;
            } else {
                level.shake = (int) (10 / (((level.time - explodedTime) / 100) + 1) * (exploding + 1));
            }
            return;
        }
        if (collision((int) x + (int) xv, (int) y)) {
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

    @Override
    public void attackThis(Mob attacker, int damage, int knockback, int freezems) {
        if (dead) return;
        kill();

    }

    @Override
    public void kill() {
        if (dead) return;
        dead = true;
        selfDestruct();
        explodedTime = level.time;
    }

    private void selfDestruct() {
        exploding++;
        destructTime += 10 * rand.nextInt(50);
        setAnimation(animations[1], 400);
        int eX, eY;
        for (Mob e : level.mobs) {
            eX = e.getX() + hitboxWidth / 2;
            eY = e.getY() + hitboxHeight / 2;
            if (eX > x + hitboxWidth / 2 - blastRadius && eX < x + hitboxWidth / 2 + blastRadius) {
                if (eY > y + hitboxHeight / 2 - blastRadius && eY < y + hitboxHeight / 2 + blastRadius) {
                    if (e != this && !e.dead) {
                        e.kill();
                    }
                }
            }
        }
        eX = level.player.getX() + hitboxWidth / 2;
        eY = level.player.getY() + hitboxHeight / 2;
        if (level.player.dead) return;
        if (eX > x + hitboxWidth / 2 - blastRadius && eX < x + hitboxWidth / 2 + blastRadius) {
            if (eY > y + hitboxHeight / 2 - blastRadius && eY < y + hitboxHeight / 2 + blastRadius) {
                level.player.kill();
            }
        }
        xv = 0;
    }
}
