package spel;

import java.awt.Canvas;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.text.DecimalFormat;
import java.util.Random;

import javax.swing.JFrame;

import spel.graphics.Screen;
import spel.input.Keyboard;
import spel.level.Level;
import spel.menu.Menu;

public class Game extends Canvas {

	public static Game game;
	public final static  String NAME = "Nordic-Warrior-XL";
	private final static int stats = 2;
	public final static int WIDTH = 700;
	public final static int HEIGHT = WIDTH * 9 / 16;
	private final static float scale = 2f;
	private static Dimension screenSize = new Dimension((int)(WIDTH*scale),(int)(WIDTH*scale*9/16));
	private final static boolean FULLSCREEN = true;
	public static Random random = new Random();

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

	public Game() {
		setSize();
		init();
	}
	

	// hittar sk�rmens storlek och st�ller in rutans storlek d�refter.
	private void setSize() {
		screen = new Screen(WIDTH,HEIGHT);
		frame = new JFrame("Loading");
		
		frame.addComponentListener(new ComponentListener() {
			@Override
			public void componentShown(ComponentEvent e) {}
			
			@Override
			public void componentResized(ComponentEvent e) {
				screenSize.height = frame.getHeight();
				screenSize.width = frame.getWidth();
			}
			
			@Override
			public void componentMoved(ComponentEvent e) {}
			
			@Override
			public void componentHidden(ComponentEvent e) {}
		});

		// image �r bilden som man �ndrar och printar ut hela tiden.
		// pixels �r direktkopplade till images pixlar. �ndrar du v�rdet p�
		// n�got i pixels �ndras det ocks� i image.
		// Formatet �r hexadeximal f�rgkodning(0-255) Ex: 0xffffff, 0x11aa22,
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();
		
		if (FULLSCREEN) {
			screenSize = Toolkit.getDefaultToolkit().getScreenSize();
			frame.getContentPane().setSize(screenSize.width, screenSize.height);
			frame.setPreferredSize(new Dimension(screenSize.width, screenSize.height));
			menu = new Menu(screenSize.width, screenSize.height);
			frame.setUndecorated(true);
		} else {
			frame.getContentPane().setSize(screenSize);
			frame.setPreferredSize(screenSize);
			menu = new Menu(screenSize.width,screenSize.height);
			frame.setUndecorated(false);
		}
	}

	// Initierar variabler och st�ller in framen.
	private void init() {
		key = new Keyboard(this);
		cl = new CardLayout();
		frame.setLayout(cl);

		// frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle(NAME);
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

	public void setLevel(String path, boolean jar) {
		level = new Level(path);
		level.player.key = key;
	}


	// updaterar knapptryckningar, gubbar och all mekanik.
	public synchronized void update() {
		key.update();
		if (!paused)
			level.update();

	}

	private DecimalFormat df = new DecimalFormat("#.###");

	// grafik metod. Ritar i princip ut allt som man kan se p� sk�rmen.
	private synchronized void render() {
		// Skapar en bufferstrategy d�r man g�r redo 2 bilder innan man ritar
		// ut dom.
		BufferStrategy bs = getBufferStrategy();
		if (bs == null) {
			createBufferStrategy(3);
			return;
		}

		if (!paused) {
			level.renderAll(screen);
			System.arraycopy(screen.pixels, 0, pixels, 0, pixels.length);
		}

		// printar ut bilden som allt �r ritat p�.
		Graphics g = bs.getDrawGraphics();

		// jordb�vnings effekt

		if (level.shake > 0) {;
			Random r = new Random();
			//int rand = r.nextInt(level.shake) - level.shake / 2;
			g.drawImage(image,  r.nextInt(level.shake) - level.shake / 2, r.nextInt(level.shake) - level.shake / 2
					, getWidth() + r.nextInt(level.shake) - level.shake / 2, getHeight() + r.nextInt(level.shake) - level.shake / 2, null);
		} else {
			g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
		}
		
		if(level.won){
			g.setFont(new Font("Verdana", Font.PLAIN, 20));
			g.drawString("Level completed!", getWidth()/2, getHeight()/2);
		}

		// Visar stats.
		if (stats > 0) {
			g.setFont(new Font("Dialog", Font.PLAIN,12));
			g.setColor(Color.WHITE);
			g.drawString("FPS " + frames + ", " + "UPS " + updates, 10, 15);
			if (stats > 1) {
				g.drawString("X " + level.player.getX() + " Y " + level.player.getY(), 10, 35);
				g.drawString("Xv " + df.format(level.player.xv) + " Yv " + df.format(level.player.yv), 10, 55);
				if (stats > 2) {
					g.drawString("onGround: " + level.player.onGround, 10, 75);
					g.drawString("moving: " + level.player.moving, 10, 95);
				}
			}
		}
		g.dispose();
		bs.show();
	}

	double ns = 1000000000.0 / 60.0;

	// game-loopen
	public void run() {
		long lastTime = System.nanoTime();
		long second = System.nanoTime();
		long now = lastTime;
		double delta = 0;
		int frames = 0;
		int updates = 0;
		boolean updated = true;
		boolean framelock = false;

		while (running) {
			now = System.nanoTime();
			if (level != null) {
				delta += ((now - lastTime) / ns) * level.speed;
			} else {
				delta += (now - lastTime) / ns;
			}

			lastTime = now;

			// updaterar game mechanics. k�r loopen f�r varje 1/64.0 s som
			// den inte har uppdaterats.
			while (delta >= 1) {
				delta--;
				update();
				updates++;
				updated = true;
			}


			if (!paused && (updated || !framelock)) {
				render();
				frames++;
				updated = false;
			}
			
			// updaterar fps och ups statsen en g�ng per sekund.
			if (now - second >= 1000000000) {
				second = now;
				this.frames = frames;
				this.updates = updates;
				frames = 0;
				updates = 0;
			}
		}
	}

	// startar game-loopen
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
	
	public void togglePause2(){
		paused = !paused;
	}
	
	public static void information(int type, String message){
        Log.log(type, message);
	}

	public static void main(String[] args) {
	    //System.setProperty("sun.java2d.xrender","True");
        game = new Game();
		game.start();
	}

}
