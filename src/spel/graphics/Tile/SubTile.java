package spel.graphics.Tile;

import spel.graphics.Sprite;

public class SubTile extends Tile {
	int tileSize = 36;

	public SubTile(Tile tile, int xa, int ya, int width, int height) {
		solid = tile.solid;
		breakable = tile.breakable; 
		this.width = width;
		this.height = height;
		int[] pixels = new int[width * height];
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				pixels[y*width + x] = tile.getSprite().pixels[ya * tile.getSprite().getWidth() + xa];
				xa++;
			}
			ya++;
		}
		sprite = new Sprite(pixels, width, height);
	}
}
