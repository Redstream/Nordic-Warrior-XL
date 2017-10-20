package spel.graphics;

import spel.graphics.Tile.Tile;

import java.awt.*;

public class Screen {

    private static final int TRANSPARENT_COLOR = 0xffff00ff;
    public int pixels[];
    private int height, width;
    public Point offset = new Point(0,0);

    public Screen(int width, int height) {
        pixels = new int[width * height];
        this.height = height;
        this.width = width;
    }

    public void clear(int color) {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = color;
        }
    }

    public void renderSprite(Sprite sprite, int xp, int yp) {
        renderSprite(sprite, xp, yp, 1);
    }

    public void renderSprite(Sprite sprite, int xp, int yp, int dir) {
        xp -= (int)offset.getX();
        yp -= (int)offset.getY();

        for (int y = 0; y < sprite.getHeight(); y++) {
            int ya = yp + y;
            if (ya < 0 || ya >= height || y < 0 || y > sprite.getHeight()) continue;
            for (int x = 0; x < sprite.getWidth(); x++) {
                int xa = xp + x;
                if (dir == -1) {
                    xa = xp + 35 - x;
                }
                if (xa < 0 || xa >= width || x < 0 || x > sprite.getWidth()) continue;
                if (sprite.pixels[x + y * sprite.getWidth()] != 0xffff00ff) {
                    int color = sprite.pixels[x + y * sprite.getWidth()];

                    if (sprite.red != 0) {
                        int red = (color >>> 16) & 0xFF;
                        int green = (color >>> 8) & 0xFF;
                        int blue = (color) & 0xFF;
                        red += sprite.red;
                        if (red > 255) red = 255;
                        color = ((red << 16) | (green << 8) | blue);
                        pixels[xa + ya * width] = color;
                    } else {
                        if (color != 0xff00ff) pixels[xa + ya * width] = color;
                    }

                }
            }
        }
    }

    public void renderTile(Tile tile, int xa, int ya) {
        xa -= (int)offset.getX();
        ya -= (int)offset.getY();

        int color;
        for (int y = 0; y < tile.getSprite().height; y++) {
            for (int x = 0; x < tile.getSprite().width; x++) {
                color = tile.getSprite().pixels[x + y * tile.getSprite().width];
                if ((xa + x) + (ya + y) * width > pixels.length) continue;
                if (x + y * tile.getSprite().width > tile.getSprite().pixels.length) continue;
                if (color != TRANSPARENT_COLOR) {
                    pixels[(xa + x) + (ya + y) * width] = tile.getSprite().pixels[x + y * tile.getSprite().width];
                }
            }
        }
    }
}
