package game.graphics;

import game.Game;
import game.Log;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class Sprite {

    public int width, height;
    public int pixels[];
    public int red = 0;
    public BufferedImage img;

    public static Sprite player = new Sprite(SpriteSheet.player, 0, 0, 36, 72);
    public static Sprite err = new Sprite(36, 36, 0xffffff);

    // Skapar en sprite från en bild
    public Sprite(String path) {
        try {
            img = ImageIO.read(new File(path));
            height = img.getHeight();
            width = img.getWidth();
            pixels = img.getRGB(0, 0, width, height, pixels, 0, width);
        } catch (Exception e) {
            Log.msg(Log.WARNING, "Error loading sprite @" + path);
        }
    }

    // Skapar en sprite från en existerande bild-array;
    public Sprite(int[] pixels, int width, int height) {
        this.pixels = pixels;
        this.width = width;
        this.height = height;
    }

    // Skapar en sprite från ett spritesheet.

    public Sprite(SpriteSheet sheet, int xa, int ya, int width, int height) {
        this.width = width;
        this.height = height;

        pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[x + y * width] = sheet.pixels[xa * sheet.getSize() + x + (ya * sheet.getSize()) * sheet.getWidth() + y * sheet.getWidth()];
            }
        }

    }

    // Skapar en helfärgad sprite
    public Sprite(int width, int height, int color) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = color;
        }
    }

    // Ritar ut spriten
    public void render(Screen screen, int x, int y) {
        screen.renderSprite(this, x, Game.game.getHeight() - y - height);
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public BufferedImage getImage() {
        return img;
    }

}
