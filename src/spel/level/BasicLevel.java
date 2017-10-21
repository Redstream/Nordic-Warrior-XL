package spel.level;

import spel.Settings;
import spel.entity.mob.Boss;
import spel.entity.mob.Goblin;
import spel.entity.mob.Mob;
import spel.entity.mob.Skeleton;
import spel.entity.mob.player.Player;
import spel.graphics.Screen;
import spel.graphics.Tile.Tile;

import java.util.ArrayList;
import java.util.List;

public abstract class BasicLevel {

    private static final int tileSize = 36;

    public double gravity = 0.4;
    public double speed = 1.0;

    public Player player;
    public int width, height;
    public int[] tiles;
    public List<Mob> mobs = new ArrayList<>();
    List<Mob> originMobs = new ArrayList<>();

    protected long startTime;
    protected int tries = 0;

    public void init() {
        startTime = System.currentTimeMillis();
        offset = -Level.tileSize * height + Settings.HEIGHT;
    }

    public void update() {
        player.update();

        for (int i = 0; i < mobs.size(); i++) {
            mobs.get(i).update();
            if (mobs.get(i).isRemoved())
                mobs.remove(i);
        }
    }

    private int offset;

    public void render(Screen screen) {
        screen.clear(0x6b76b7);

        // Render the map tiles
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (getTile(x, y) == Tile.voidTile) continue;
                getTile(x, y).render(x * tileSize , y * tileSize  + offset, screen);
            }
        }

        // Render the mobs
        for (int i = 0; i < mobs.size(); i++) {
            mobs.get(i).render(screen);
        }

        // Render the player
        player.render(screen);

    }

    /**
     * Used by the mapmaker
     *
     * @param screen
     */
    public void renderTile(Screen screen) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (getTileByID(tiles[y * width + x]) == Tile.voidTile) continue;
                screen.renderTile(getTileByID(tiles[y * width + x]), x * 36, y * 36);
            }
        }
    }

    /**
     * Used when running the game
     *
     * @param x
     * @param y
     * @return Tile
     */
    public Tile getTile(int x, int y) {
        if ((x + y * width >= tiles.length) || y < 0 || x < 0 || x > width || y > height) return Tile.voidTile;
        return getTileByID(tiles[x + y * width]);
    }


    /**
     * Used by the mapmaker
     *
     * @param id
     * @return Tile
     */
    public Tile getTileByID(int id) {
        if (id == 1) return Tile.grass;
        if (id == 2) return Tile.grass2;
        if (id == 3) return Tile.grass3;
        if (id == 4) return Tile.stone1;
        if (id == 5) return Tile.stone2;
        if (id == 6) return Tile.ground1;

        if (id == 7) return Tile.tree;
        //if (id == 19) return Tile.face1;
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

    public Mob getMob(int id, int x, int y) {
        if (id == 0) return new Skeleton((Level) this, x, y);
        if (id == 1) return new Goblin((Level) this, x, y);
        if (id == 2) return new Boss((Level) this, x, y);
        return null;
    }

    public int getMobID(Mob m) {
        if (m instanceof Skeleton) return 0;
        if (m instanceof Goblin) return 1;
        if (m instanceof Boss) return 2;
        return -1;
    }

    public void resetLevel() {
        tries++;

        // Reset Mobs
        mobs.clear();
        originMobs.forEach((m) -> mobs.add(m.clone()));

        // Reset player
        player.setHp(30);
        player.dead = false;
        player.xv = 0;
        player.yv = 0;
        player.x = player.xOrigin;
        player.y = player.yOrigin;
    }


}
