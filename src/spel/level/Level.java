package spel.level;

import spel.Game;
import spel.Log;
import spel.entity.Entity;
import spel.entity.mob.Mob;
import spel.entity.mob.player.Player;

import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

public class Level extends BasicLevel {

    public static final int tileSize = 36;

    public long start = 0;
    public long time = 0;
    public int shake = 0;
    public Rectangle finnish = new Rectangle(-100, -100, 72, 72);
    public boolean won = false;
    private long finnishTime = 0;
    public int spawnX, spawnY;

    /**
     * Load level from path
     *
     * @param path
     */
    public Level(String path) {
        loadLevel(path);
        init();
    }

    /**
     * Used by the Mapmaker
     *
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
        if (Entity.intersect(finnish, player.hitBox)) {
            won = true;
        }

        if (won) {
            if (finnishTime == 0) {
                finnishTime = time + 3000;
            } else {
                if (finnishTime < time) {
                    Game.game.togglePause();
                }
            }
        }
        time = System.currentTimeMillis() - start;
    }

    /**
     * Todo: split & redo method
     *
     * @param path
     */
    private void loadLevel(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(MapLoader.defaultFolder + File.separator + path));
            try {
                String s = br.readLine();
                width = Integer.parseInt(s.split(",")[0]);
                height = Integer.parseInt(s.split(",")[1]);

                tiles = new int[width * height];

                s = br.readLine();
                if (s.equalsIgnoreCase("")) {
                    Log.msg(Log.SEVERE, "Map empty");
                    br.close();
                    return;
                }


                for (int i = 0; i < tiles.length; i++) {
                    if (i == s.split(",").length) {
                        Log.msg(Log.SEVERE, "file corrupt");
                        break;
                    }
                    tiles[i] = Integer.parseInt(s.split(",")[i]);
                }

                try {
                    s = br.readLine();
                    String[] mobs = s.split(";");

                    for (int i = 0; i < mobs.length; i++) {
                        if (mobs.length < 2) break;
                        int x = Integer.parseInt(mobs[i].split(",")[1]);
                        int y = Integer.parseInt(mobs[i].split(",")[2]);
                        Mob mob = getMob(Integer.parseInt(mobs[i].split(",")[0]), x, y);
                        this.mobs.add(mob.clone());
                        originMobs.add(mob);
                    }

                } catch (Exception e) {
                    Log.msg(Log.WARNING, "Error loading mobs" + e.toString());
                }

                try {
                    s = br.readLine();
                    if (Game.game != null) {
                        player = new Player(null, this, Integer.parseInt(s.split(",")[0]), Integer.parseInt(s.split(",")[1]));
                    } else {
                        player = new Player(Integer.parseInt(s.split(",")[0]), Integer.parseInt(s.split(",")[1]));
                    }
                } catch (Exception e) {
                    if (Game.game != null) {
                        player = new Player(Game.game.key, this, 36, 36);
                    } else {
                        player = new Player(36, 36);
                    }

                    Log.msg(Log.INFORMATION, "Error loading player spawn, spawning at 36,36. \n" + e.toString());
                }

                // Set player spawn
                spawnX = player.xOrigin;
                spawnY = player.yOrigin;

                try {
                    s = br.readLine();
                    int rx, ry, rw, rh;
                    rx = Integer.parseInt(s.split(",")[0]);
                    ry = Integer.parseInt(s.split(",")[1]);
                    rw = Integer.parseInt(s.split(",")[2]);
                    rh = Integer.parseInt(s.split(",")[3]);
                    finnish = new Rectangle(rx, ry, rw, rh);
                } catch (Exception e) {
                    Log.msg(Log.SEVERE, "Error loading level finnish" + e.toString());
                }

                br.close();
            } catch (Exception e) {
                Log.msg(Log.WARNING, "Corrupted map file\t" + path + " " + e.toString());
            }

        } catch (Exception e) {
            Log.msg(Log.SEVERE,  "Map file not found.");
        }
    }

    public void setCheckPoint() {
        player.xOrigin = (int) player.x;
        player.yOrigin = (int) player.y;
    }

}
