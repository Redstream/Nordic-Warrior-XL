package game.graphics;

import game.Game;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class SpriteSheet {

    public static String graphicsPath = "res/graphics/";

    public static SpriteSheet background = new SpriteSheet(graphicsPath + "splash.png");

    public static SpriteSheet sheet32 = new SpriteSheet(graphicsPath + "spritesheet32.png");
    public static SpriteSheet sheet36 = new SpriteSheet(graphicsPath + "spritesheet36backup.png");
    public static SpriteSheet player = new SpriteSheet(graphicsPath + "characters/player/playersheet.png");
    public static SpriteSheet skeleton = new SpriteSheet(graphicsPath + "characters/skeleton/skeletonsheet.png");
    public static SpriteSheet goblin = new SpriteSheet(graphicsPath + "characters/goblin/goblinsheet.png");
    public static SpriteSheet boss = new SpriteSheet(graphicsPath + "characters/Boss/boss_move.png");


    public int[] pixels;
    private int width, height;
    private int size;
    public BufferedImage img;

    public SpriteSheet(String path) {
        init(path);
        size = 36;
    }

    public SpriteSheet(String path, int size) {
        init(path);
        this.size = size;
    }

    public void init(String path) {
        try {
            img = ImageIO.read(getClass().getClassLoader().getResource(path));
        } catch (Exception e) {
            Game.information(2, "Error loading spritesheet " + path);
            return;
        }
        height = img.getHeight();
        width = img.getWidth();
        pixels = img.getRGB(0, 0, width, height, pixels, 0, width);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getSize() {
        return size;
    }

}
