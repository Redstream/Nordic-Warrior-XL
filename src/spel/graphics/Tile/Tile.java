package spel.graphics.Tile;

import spel.entity.mob.Mob;
import spel.entity.mob.player.Player;
import spel.graphics.Screen;
import spel.graphics.Sprite;
import spel.graphics.SpriteSheet;

public class Tile {

    public static Tile voidTile = new Tile(new Sprite(36, 36, 0x6b76b7), false, false);
    public static Tile blue = new Tile(new Sprite(36, 36, 0x0000ff), false, false);
    public static Tile red = new Tile(new Sprite(36, 36, 0xff0000), false, false);
    public static Tile green = new Tile(new Sprite(36, 36, 0x00ff00), false, false);

    public static Tile dirt = new Tile(new Sprite(SpriteSheet.sheet36, 1, 0, 36, 36));
    public static Tile grass2 = new Tile(new Sprite(SpriteSheet.sheet36, 0, 0, 36, 36), false, false);
    public static Tile grass3 = new Tile(new Sprite(SpriteSheet.sheet36, 2, 0, 36, 36), false, false) {
    };
    public static Tile grass = new Tile(new Sprite(SpriteSheet.sheet36, 3, 0, 36, 36));
    public static Tile stone1 = new Tile(new Sprite(SpriteSheet.sheet36, 1, 3, 36, 36)) {
        public void onCollision(Mob m, int x, int y) {
            if (m instanceof Player) {
                //m.yv = 10;
            }

        }
    };

    public static Tile stone2 = new Tile(new Sprite(SpriteSheet.sheet36, 1, 4, 36, 36));
    public static Tile tree = new Tile(new Sprite(SpriteSheet.sheet36, 4, 0, 180, 252), false, false);
    public static Tile face1 = new Tile(new Sprite(SpriteSheet.sheet36, 2, 1, 72, 72), false, false);
    public static Tile ground1 = new Tile(new Sprite(SpriteSheet.sheet36, 1, 0, 36, 36));
    public static Tile face2 = new Tile(new Sprite(SpriteSheet.sheet36, 1, 5, 108, 72), false, false);
    public static Tile budda = new Tile(new Sprite(SpriteSheet.sheet36, 0, 8, 72, 108), false, false);
    public static Tile dead1 = new Tile(new Sprite(SpriteSheet.sheet36, 0, 1, 72, 72), false, false);
    public static Tile dead2 = new Tile(new Sprite(SpriteSheet.sheet36, 13, 0, 144, 144), false, false);
    public static Tile gate = new Tile(new Sprite(SpriteSheet.sheet36, 9, 0, 144, 144), false, false);
    public static Tile edgeH = new Tile(new Sprite(SpriteSheet.sheet36, 2, 7, 36, 36));
    public static Tile edgeV = new Tile(new Sprite(SpriteSheet.sheet36, 3, 8, 36, 36));
    public static Tile eGrassH = new Tile(new Sprite(SpriteSheet.sheet36, 2, 8, 36, 36), false, false);
    public static Tile eGrassV = new Tile(new Sprite(SpriteSheet.sheet36, 5, 7, 36, 36), false, false);
    public static Tile eGroundH = new Tile(new Sprite(SpriteSheet.sheet36, 3, 7, 36, 36));
    public static Tile eGroundV = new Tile(new Sprite(SpriteSheet.sheet36, 4, 7, 36, 36));
    //public static Tile Groundskeleton1 = new Tile(new Sprite(SpriteSheet.sheet36, 4, 7, 36, 36));
    //public static Tile Groundskeleton2 = new Tile(new Sprite(SpriteSheet.sheet36, 4, 7, 36, 36));

    protected int width, height;
    protected boolean solid;
    protected boolean breakable;
    protected Sprite sprite;

    public Tile() {
    }

    public Tile(Sprite sprite) {
        initSprite(sprite);
        solid = true;
        breakable = false;
    }

    public Tile(Sprite sprite, boolean solid, boolean breakable) {
        initSprite(sprite);
    }

    private void initSprite(Sprite sprite) {
        this.sprite = sprite;
        width = sprite.getWidth();
        height = sprite.getHeight();
    }

    public void render(int x, int y, Screen screen) {
        screen.renderSprite(getSprite(), x, y);
    }


    public void onCollision(Mob mob, int x, int y) {

    }

    public boolean isSolid() {
        return solid;
    }

    public boolean isBreakable() {
        return breakable;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    public Sprite getSprite() {
        return sprite;
    }
}
