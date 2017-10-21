package spel;

import spel.graphics.Screen;
import spel.input.Keyboard;
import spel.level.Level;
import spel.level.MapLoader;
import spel.menu.Menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.text.DecimalFormat;
import java.util.Random;

public class Game extends Canvas {

    public static Game game;
    private Dimension screenSize = new Dimension((int) (Settings.WIDTH * Settings.scale), (int) (Settings.HEIGHT * Settings.scale));

    private BufferedImage image;
    private int[] pixels;

    private int frames;
    private int updates;
    private boolean running = false;
    private boolean paused = true;

    private JFrame frame;
    private Screen screen;
    public Keyboard key;
    public Level level;
    private CardLayout cl;
    private Menu menu;

    /**
     * Construct
     */
    public Game() {
        setSize();
        init();
    }

    /**
     * Find screen and set size
     */
    private void setSize() {
        screen = new Screen(Settings.WIDTH, Settings.HEIGHT);
        frame = new JFrame("Loading");

        frame.addComponentListener(new ComponentListener() {
            @Override
            public void componentShown(ComponentEvent e) {
            }

            @Override
            public void componentResized(ComponentEvent e) {
                screenSize.height = frame.getHeight();
                screenSize.width = frame.getWidth();
            }

            @Override
            public void componentMoved(ComponentEvent e) {
            }

            @Override
            public void componentHidden(ComponentEvent e) {
            }
        });

        // image �r bilden som man �ndrar och printar ut hela tiden.
        // pixels �r direktkopplade till images pixlar. �ndrar du v�rdet p�
        // n�got i pixels �ndras det ocks� i image.
        // Formatet �r hexadeximal f�rgkodning(0-255) Ex: 0xffffff, 0x11aa22,
        image = new BufferedImage(Settings.WIDTH, Settings.HEIGHT, BufferedImage.TYPE_INT_RGB);
        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        if (Settings.FULLSCREEN) {
            screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.getContentPane().setSize(screenSize.width, screenSize.height);
            frame.setPreferredSize(new Dimension(screenSize.width, screenSize.height));
            menu = new Menu(screenSize.width, screenSize.height);
            frame.setUndecorated(true);
        } else {
            frame.getContentPane().setSize(screenSize);
            frame.setPreferredSize(screenSize);
            menu = new Menu(screenSize.width, screenSize.height);
            frame.setUndecorated(false);
        }
    }

    /**
     * Init frame
     */
    private void init() {
        key = new Keyboard(this);
        cl = new CardLayout();
        frame.setLayout(cl);

        // frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setTitle(Settings.NAME);
        addKeyListener(key);
        addMouseMotionListener(key);
        frame.add(menu, "1");
        frame.add(this, "2");
        cl.show(frame.getContentPane(), "1");
        frame.pack();
        frame.setVisible(true);
        setFocusable(true);
        requestFocus();
        frame.requestFocus();
        frame.setLocationRelativeTo(null);
    }

    /**
     * Set the game level
     *
     * @param path to level
     */
    public void setLevel(String path) {
        level = new Level(path);
        level.player.key = key;
    }

    /**
     * Updated game physics, keyboards etc..
     */
    public synchronized void update() {
        key.update();
        if (!paused)
            level.update();
    }

    private DecimalFormat df = new DecimalFormat("#.###");

    /**
     * Graphics
     */
    private synchronized void render() {
        BufferStrategy bs = getBufferStrategy();
        if (bs == null) {
            createBufferStrategy(3);
            return;
        }

        if (!paused) {
            level.render(screen);
            System.arraycopy(screen.pixels, 0, pixels, 0, pixels.length);
        }

        // Print the image
        Graphics g = bs.getDrawGraphics();

        // Earthshake effect
        if (level.shake > 0) {
            Random r = new Random();
            g.drawImage(image, r.nextInt(level.shake) - level.shake / 2, r.nextInt(level.shake) - level.shake / 2
                    , getWidth() + r.nextInt(level.shake) - level.shake / 2, getHeight() + r.nextInt(level.shake) - level.shake / 2, null);
        } else {
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }

        if (level.won) {
            g.setFont(new Font("Verdana", Font.PLAIN, 20));
            g.drawString("Level completed!", getWidth() / 2, getHeight() / 2);
        }

        // Visar stats.
        if (Settings.stats > 0) {
            g.setFont(new Font("Dialog", Font.PLAIN, 12));
            g.setColor(Color.WHITE);
            g.drawString("FPS " + frames + ", " + "UPS " + updates, 10, 15);
            if (Settings.stats > 1) {
                g.drawString("X " + level.player.getX() + " Y " + level.player.getY(), 10, 35);
                g.drawString("Xv " + df.format(level.player.xv) + " Yv " + df.format(level.player.yv), 10, 55);
                if (Settings.stats > 2) {
                    g.drawString("onGround: " + level.player.onGround, 10, 75);
                    g.drawString("moving: " + level.player.moving, 10, 95);
                }
            }
        }

        g.dispose();
        bs.show();
    }


    /**
     * Game-loop
     */
    private void run() {
        double ns = 1000000000.0 / 60.0;
        long lastTime = System.nanoTime();
        long second = System.nanoTime();
        long now;
        double delta = 0;
        int frames = 0;
        int updates = 0;
        boolean updated = false;
        boolean framelock = true;

        while (running) {
            // Reduce the cpu usage. Can we do this differently?
            try {
                Thread.sleep(2);
            } catch (Exception e) {
            }

            now = System.nanoTime();
            delta += ((now - lastTime) / ns) * (level != null ? level.speed : 1);
            lastTime = now;

            // Update game
            while (delta >= 1) {
                delta--;
                update();
                updates++;
                updated = true;
            }

            // Render Game
            if (!paused && (updated || !framelock)) {
                render();
                frames++;
                updated = false;
            }

            // Update the fps and ups stats once very second
            if (now - second >= 1000000000) {
                second = now;
                this.frames = frames;
                this.updates = updates;
                frames = 0;
                updates = 0;
            }
        }
    }

    /**
     * Start game if not running
     */
    public void start() {
        if (running) return;
        running = true;
        requestFocus();
        run();
    }

    public void togglePause() {
        if (paused) {
            cl.show(frame.getContentPane(), "2");
            requestFocus();
        } else {
            cl.show(frame.getContentPane(), "1");
        }
        paused = !paused;
        frame.pack();
    }

    public void togglePause2() {
        paused = !paused;
    }

    /**
     * @param args ignored
     */
    public static void main(String[] args) {
        //System.setProperty("sun.java2d.opengl","True");
        MapLoader.addDemoMaps();
        game = new Game();
        game.start();
    }
}
