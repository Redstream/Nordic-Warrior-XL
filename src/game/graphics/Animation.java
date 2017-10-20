package game.graphics;

public class Animation {

    public Sprite[] sprites;
    private boolean running = false;
    private int index = 0;
    private long startTime;
    private long changeTime;
    private int time = 100;
    private boolean repetetive = true;
    public boolean requireFinnish = false;
    public boolean locked = false;

    public Animation(int time, Sprite[] sprites) {
        this.time = time;
        startTime = System.currentTimeMillis();
        changeTime = startTime;
        addSprites(sprites);
    }

    public Animation(SpriteSheet sheet, int width, int height, int counts, int row) {
        startTime = System.currentTimeMillis();
        changeTime = startTime;
        loadSprites(sheet, width, height, counts, row);
    }

    public void loadSprites(SpriteSheet sheet, int width, int height, int counts, int row) {
        sprites = new Sprite[counts];
        for (int i = 0; i < counts; i++) {
            sprites[i] = new Sprite(sheet, i * width / 36, row, width, height);
        }
    }

    public void addSprites(Sprite[] sprites) {
        this.sprites = sprites;
    }


    private void nextSprite() {
        if (index < sprites.length - 1) {
            index++;
        } else {
            locked = false;
            if (repetetive) {
                index = 0;
            } else {
                stop();
            }
        }
    }

    public Sprite getSprite() {
        if (running) {
            if (System.currentTimeMillis() - changeTime >= time) {
                nextSprite();
                changeTime = System.currentTimeMillis();
            }
        }
        return sprites[index];
    }

    public void start() {
        running = true;
        index = 0;
        changeTime = System.currentTimeMillis();
        if (requireFinnish) locked = true;
    }

    public void start(int time) {
        start();
        this.time = time;
    }

    public void stop() {
        running = false;
    }

    public void setRepetetive(boolean bool) {
        repetetive = bool;
    }

    public void setTime(int time) {
        this.time = time;
    }

}
