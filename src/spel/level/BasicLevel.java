package spel.level;

import java.util.ArrayList;
import java.util.List;

import spel.Game;
import spel.entity.Entity;
import spel.entity.mob.Boss;
import spel.entity.mob.Goblin;
import spel.entity.mob.Mob;
import spel.entity.mob.Skeleton;
import spel.entity.mob.player.Player;
import spel.graphics.Screen;
import spel.graphics.Tile.Tile;

public abstract class  BasicLevel {
	
	public static final int tileSize = 36;
	public  int xOffset, yOffset;
	
	public double gravity = 0.4;
	public double speed = 1.0;
	
	public Player player;
	public int width, height;
	public int[] tiles;
	public List<Mob> entitys = new ArrayList<Mob>();
	

	public BasicLevel() {
		
	}
	
	public void update(){
		try{
			player.update();
		}catch(Exception e){
			Game.information(2, "Error updating player");
		}
		
		try{
			for (Entity e : entitys) {
				e.update();
			}
			for(Entity e: entitys){
				if(e.isRemoved()){
					entitys.remove(e);
				}
			}
		}catch (java.util.ConcurrentModificationException e){
			Game.information(0,"ConcurrentModificationException in update");
		}
		
	}

	public void renderAll(Screen screen){
		screen.clear(0x6b76b7);
		render(screen);
		try{
			for(Entity e: entitys){
				e.render(screen);
			}	
		}catch (java.util.ConcurrentModificationException e){
			Game.information(0,"ConcurrentModificationException in render");
		}
		try{
			player.render(screen);
		}catch(Exception e){
			Game.information(2, "Error rendering player");
		}
		
	}
	
	public void init() {
		offset = (int)(-36*height + 393);
	}
	
	int offset;
	public void render(Screen screen) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (getTile(x, y) == Tile.voidTile) continue;
				getTile(x, y).render(x * tileSize - xOffset, y * tileSize - yOffset + offset, screen);
			}
		}
	}

	// anv�nds f�r att rita ut bilden(v�rlden) i mapmakern
	public void renderTile(Screen screen) {
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if(getTileByID(tiles[y * width + x]) == Tile.voidTile) continue;
				screen.renderTile(getTileByID(tiles[y * width + x]), x * 36, y * 36);
			}
		}
	}

	// anv�nds n�r spelet k�r
	public Tile getTile(int x, int y) {
		if ((x + y * width >= tiles.length) || y < 0 || x < 0 || x > width || y > height) return Tile.voidTile;
		return getTileByID(tiles[x + y * width]);
	}
	
	// anv�nds till Mapmakern
	public Tile getTileByID(int id) {
		if (id == 1) return Tile.grass;
		if (id == 2) return Tile.grass2;
		if (id == 3) return Tile.grass3;
		if (id == 4) return Tile.stone1;
		if (id == 5) return Tile.stone2;
		if (id == 6) return Tile.ground1;
		
		if (id == 7) return Tile.tree;
		//if (id == 6) return Tile.face1;
		if (id == 8) return Tile.face2;
		if (id == 9) return Tile.budda;
		if (id == 10) return Tile.dead1;
		if (id == 11) return Tile.dead2;
		if (id == 12) return Tile.gate;
		if (id == 13) return Tile.edgeH;
		if (id == 14) return Tile.edgeV;
		if (id == 15) return Tile.eGrassH;
		if (id == 16) return Tile.eGrassV;
		if (id == 17) return Tile.eGroundH;
		if (id == 18) return Tile.eGroundV;
		
		return Tile.voidTile;
	}
	/* Old
	  	if (id == 1) return Tile.grass;
		if (id == 2) return Tile.grass2;
		if (id == 3) return Tile.grass3;
		if (id == 4) return Tile.stone1;
		if (id == 5) return Tile.stone2;
		if (id == 6) return Tile.tree;
		//if (id == 6) return Tile.face1;
		if (id == 7) return Tile.ground1;
		if (id == 8) return Tile.face2;
		if (id == 9) return Tile.budda;
		if (id == 10) return Tile.dead1;
		if (id == 11) return Tile.dead2;
		if (id == 12) return Tile.gate;
		if (id == 13) return Tile.edgeH;
		if (id == 14) return Tile.edgeV;
		if (id == 15) return Tile.eGrassH;
		if (id == 16) return Tile.eGrassV;
		if (id == 17) return Tile.eGroundH;
		if (id == 18) return Tile.eGroundV;
	 */
	
	public Mob getMob(int id){
		return getMob(id,0,0);
	}
	
	public Mob getMob(int id, int x, int y){
		if(id == 0) return new Skeleton((Level)this,x,y);
		if(id == 1) return new Goblin((Level)this,x,y);
		if(id == 2) return new Boss((Level)this,x,y);
		return null;
	}
	
	public int getMobID(Mob m){
		if(m instanceof Skeleton) return 0;
		if(m instanceof Goblin) return 1;
		if(m instanceof Boss) return 2;
		return -1;
	}

}
