package spel.level;

import java.awt.Rectangle;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import spel.Game;
import spel.entity.Entity;
import spel.entity.mob.Mob;
import spel.entity.mob.player.Player;




public class Level extends BasicLevel {
	
	public static File mapfolder = new File(System.getProperty("user.home") + File.separator + Game.NAME + File.separator + "maps");
	public static final int tileSize = 36;

	public long start = 0;
	public long time = 0;
	public int shake = 0;
	public Rectangle finnish = new Rectangle(-100,-100,72,72);
	public boolean won = false;
	private long finnishTime = 0;

	/**
	 * Load level from path
	 * @param path
	 */
	public Level(String path) {
		loadLevel(path);
		init();
	}

	/**
	 * Used by the Mapmaker
	 * @param width
	 * @param height
	 */
	public Level(int width, int height) {
		this.width = width;
		this.height = height;
		tiles = new int[width * height];
		init();
	}

	public void init() {
		super.init();
		start = System.currentTimeMillis();
	}

	public void update() {
        super.update();
		if(Entity.intersect(finnish, player.hitBox)) {
			won = true;
		}

		if(won) {
			if(finnishTime == 0){
				finnishTime = time + 3000;
			} else {
				if(finnishTime < time) {
					Game.game.togglePause();
				}
			}
		}
		time = System.currentTimeMillis()-start;
	}

	/**
	 * Todo: split & redo method
	 * @param path
	 */
	private void loadLevel(String path) {
		try {	
			BufferedReader br = new BufferedReader(new FileReader(mapfolder + File.separator + path));
			try {
				String s = br.readLine();
				width = Integer.parseInt(s.split(",")[0]);
				height = Integer.parseInt(s.split(",")[1]);

				tiles = new int[width * height];

				s = br.readLine();
				if (s.equalsIgnoreCase("")) {
					Game.information(2,"Map empty");
					br.close();
					return;
				}

				
				for (int i = 0; i < tiles.length; i++) {
					if (i == s.split(",").length) {
						Game.information(2,"file corrupt");
						break;
					}
					tiles[i] = Integer.parseInt(s.split(",")[i]);
				}
				
				try {
					s = br.readLine();
					String[] mobs = s.split(";");

					for(int i = 0; i < mobs.length;i++){
						if(mobs.length < 2) break;
						int x = Integer.parseInt(mobs[i].split(",")[1]);
						int y = Integer.parseInt(mobs[i].split(",")[2]);
						Mob mob = getMob(Integer.parseInt(mobs[i].split(",")[0]), x,y);
						this.mobs.add(mob.clone());
						originMobs.add(mob);
					}

				} catch(Exception e) {
					Game.information(1, "Error loading mobs" + e.toString());
				}

				try{
					s = br.readLine();
					if(Game.game != null){
						player = new Player(null,this,Integer.parseInt(s.split(",")[0]),Integer.parseInt(s.split(",")[1]));
					}else{
						player = new Player(Integer.parseInt(s.split(",")[0]),Integer.parseInt(s.split(",")[1]));
					}
				} catch(Exception e) {
					if(Game.game != null){
						player = new Player(Game.game.key,this,36,36);
					}else{
						player = new Player(36,36);
					}
					
					Game.information(1,"Error loading player spawn, spawning at 36,36. \n"+e.toString());
				}
				
				
				try{
					s = br.readLine();
					int rx,ry,rw,rh;
					rx = Integer.parseInt(s.split(",")[0]);
					ry = Integer.parseInt(s.split(",")[1]);
					rw = Integer.parseInt(s.split(",")[2]);
					rh = Integer.parseInt(s.split(",")[3]);
					finnish = new Rectangle(rx, ry, rw, rh);
				} catch(Exception e) {
					Game.information(2,"Error loading level finnish"+  e.toString());
				}
				
				br.close();
			} catch (Exception e) {
				Game.information(1,"Corrupted map file\t" + path +" "+  e.toString());
			}

		} catch (Exception e) {
			Game.information(2,"Map file not found.");
		}
	}

	public void setCheckPoint() {
		player.xOrigin = (int)player.x;
		player.yOrigin = (int)player.y;
	}

}
