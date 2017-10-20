package mapeditor;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import game.Game;
import game.Log;
import game.entity.mob.Mob;
import game.entity.mob.player.Player;
import game.graphics.Screen;
import game.input.Keyboard;
import game.level.Level;
import game.level.MapLoader;

public class Mapmaker extends JFrame implements MouseMotionListener, MouseListener {

	private static final long serialVersionUID = 1L;
	public static int tileSize = 36;
	public int width = 200, height = 16;

	public Dimension dim = new Dimension(width * tileSize, height * tileSize);
	public String name;
	public int activeTile = 0;

	public BufferedImage img;
	private int[] pixels;
	private List<Mob> mobs = new ArrayList<>();
	private Level level;
	private Screen screen;
	private JList<String> tileList;
	private JList<String> mobList;
	private Keyboard key;

	public Mapmaker() {
		initComponents();
		level = new Level(width, height);
		level.player = new Player(36,36);
		init();
		addKeyListener(key);
		setFocusable(true);
		requestFocus();
		tileList.setSelectedIndex(0);
	}

	public void init() {
		dim = new Dimension(width * tileSize, height * tileSize);
		img = new BufferedImage(width * tileSize, height * tileSize, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
		screen = new Screen(width * tileSize, height * tileSize);

		drawPanel.setPreferredSize(dim);
		drawPanel.setMinimumSize(dim);
		drawPanel.setMaximumSize(dim);

		scrollPane.addMouseMotionListener(this);
		scrollPane.addMouseListener(this);
		setLocationRelativeTo(null);

		redraw();
	}

	private void initComponents() {
		jMenuItem1 = new javax.swing.JMenuItem();
		jLabel1 = new javax.swing.JLabel();
		nameTextfield = new javax.swing.JTextField();

		jTabbedPane1 = new javax.swing.JTabbedPane();
		scrollPane = new JScrollPane(drawPanel);
		drawPanel = new JPanel() {
			private static final long serialVersionUID = 1L;

			public void paintComponent(Graphics g) {
				g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
				try {
					for (Mob m : mobs) {
						g.drawString(m.getClass().getSimpleName(), m.getX(), (m.getY() - img.getHeight()) * -1);
					}
					g.drawString("Player", level.player.getX(), (level.player.getY() - img.getHeight()) * -1);
					g.drawRect(level.finnish.x, (level.finnish.y - img.getHeight()) *-1- level.finnish.height, level.finnish.width, level.finnish.height);
				} catch (Exception e) {
				}
			}
		};
		jLabel2 = new javax.swing.JLabel();
		jLabel3 = new javax.swing.JLabel();
		jMenuBar1 = new javax.swing.JMenuBar();
		jMenu1 = new javax.swing.JMenu();
		newMap = new javax.swing.JMenuItem();
		saveMap = new javax.swing.JMenuItem();
		loadMap = new javax.swing.JMenuItem();
		jMenu2 = new javax.swing.JMenu();

		scrollPane.setBackground(new java.awt.Color(0, 0, 0));
		scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

		jMenuItem1.setText("jMenuItem1");

		setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
		setBackground(new java.awt.Color(153, 153, 153));
		setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
		setName("Map editor\n");
		setResizable(false);

		jLabel1.setText("Name:");

		String[] tileData = new String[] {"Grass", "Grass 2", "Grass 3", "Stone 1", "Stone 2", "Dirt", "Tree", "Face2", "Budda", "Dead1", "Dead2", "Gate", "edgeH", "edgeV", "eGrassH", "eGrassV", "eGroundH", "eGroundV" };
		tileList = new JList<String>(tileData);
		tileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tileList.setLayoutOrientation(JList.VERTICAL);
		tileList.setVisibleRowCount(25);

		String[] mobData = new String[] { "Player Spawn ","Level Finnish",  "Skeleton", "Goblin" };
		mobList = new JList<String>(mobData);
		mobList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mobList.setLayoutOrientation(JList.VERTICAL);
		mobList.setVisibleRowCount(25);

		jTabbedPane1.addTab("Tiles", new JScrollPane(tileList));
		jTabbedPane1.addTab("Other", new JScrollPane(mobList));
		tileList.setSelectedIndex(3);

		javax.swing.GroupLayout drawPanelLayout = new javax.swing.GroupLayout(drawPanel);
		drawPanel.setLayout(drawPanelLayout);
		drawPanelLayout.setHorizontalGroup(drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 795, Short.MAX_VALUE));
		drawPanelLayout.setVerticalGroup(drawPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGap(0, 598, Short.MAX_VALUE));

		scrollPane.setViewportView(drawPanel);

		jLabel2.setText("0");
		jLabel3.setText("0");
		
		jMenu1.setText("File");

		newMap.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
		newMap.setText("New Map");
		newMap.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				newMapActionPerformed(evt);
			}
		});
		jMenu1.add(newMap);
		newMap.getAccessibleContext().setAccessibleDescription("");

		saveMap.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
		saveMap.setText("Save Map");
		saveMap.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				saveMapActionPerformed(evt);
			}
		});
		jMenu1.add(saveMap);

		loadMap.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
		loadMap.setText("Load Map");
		loadMap.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				loadMapActionPerformed(evt);
			}
		});
		jMenu1.add(loadMap);

		jMenuBar1.add(jMenu1);

		jMenu2.setText("Edit");
		jMenuBar1.add(jMenu2);

		setJMenuBar(jMenuBar1);

		javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
		getContentPane().setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(
												layout.createSequentialGroup().addComponent(jLabel1).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED).addComponent(nameTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, 117, javax.swing.GroupLayout.PREFERRED_SIZE)
														.addGap(160, 160, 160).addGap(362, 362, 362).addComponent(jLabel3, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
										.addGroup(
												layout.createSequentialGroup().addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 800, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
														.addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE))).addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
		layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addGroup(
				layout.createSequentialGroup()
						.addContainerGap()
						.addGroup(
								layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
										.addComponent(scrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 597, javax.swing.GroupLayout.PREFERRED_SIZE))
						.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
						.addGroup(
								layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(nameTextfield, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addComponent(jLabel1))
										.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE).addComponent(jLabel2).addComponent(jLabel3)))));

		jLabel2.getAccessibleContext().setAccessibleName("mouseX");
		jLabel3.getAccessibleContext().setAccessibleName("mouseY");

		pack();
	}

	public void redraw() {
		screen.clear(0x6b76b7);
		level.renderTile(screen);
		System.arraycopy(screen.pixels, 0, pixels, 0, pixels.length);
		drawPanel.repaint();
		scrollPane.repaint();
	}

	public void addTile(int x, int y) {
		if (x < level.width && y < level.height && x >= 0 && y >= 0) {
			int h = level.getTileByID(activeTile).getHeight()/tileSize;
			int w = level.getTileByID(activeTile).getWidth()/tileSize;
			if(h > 36 || w > 36){
			}
			level.tiles[x + y * level.width] = activeTile;
			redraw();
		}
	}

	public void addMob(int x, int y) {
		int id = mobList.getSelectedIndex();
		if (id == -1){
			return;
		}else if(id == 0){
			level.player.x = x;
			level.player.y = y;
		}else if(id == 1){
			level.finnish.setLocation(x, y);
		}else{
			mobs.add(level.getMob(id-2, x, y));
		}
		redraw();
	}

	public void clearMob(int xt, int yt) {
		try {
			for (Mob m : mobs) {
				for (int y = yt; y < yt + 36; y++) {
					for (int x = xt; x < xt + 36; x++) {
						if (m.getX() == x && m.getY() == y) {
							mobs.remove(m);
						}
					}
				}
			}
		} catch (Exception e) {
		}
		redraw();
	}

	private void newMapActionPerformed(java.awt.event.ActionEvent evt) {

		//nameTextfield.setText(JOptionPane.showInputDialog(this, "Map Name"));
		String w = JOptionPane.showInputDialog(this, "Map Width(Tiles)");
		String h = JOptionPane.showInputDialog(this, "Map Height(Tiles)");
		try {
			height = Integer.parseInt(h);
			width = Integer.parseInt(w);
		} catch (Exception e) {
			Log.msg(Log.SEVERE,"invalid width or height");
		}
		mobs.clear();
		level = new Level(width, height);
		init();
		drawPanel.setSize(width, height);
		scrollPane.getViewport().setSize(width, height);
		scrollPane.setSize(width, height);
		scrollPane.getHorizontalScrollBar().setValue(scrollPane.getHorizontalScrollBar().getMaximum());
		scrollPane.getVerticalScrollBar().setValue(0);
		redraw();
		validate();
		revalidate();
	}

	public void loadLevel(String mapname) {
		Log.msg(Log.INFORMATION, "Loading " + mapname + ".txt");
		level = new Level(mapname + ".txt");
		
		this.mobs = level.mobs;
		nameTextfield.setText(mapname);
		width = level.width;
		height = level.height;

		Log.debug("" + width);
		init();
		drawPanel.setSize(width, height);
		scrollPane.getViewport().setSize(width, height);
		scrollPane.setSize(width, height);
		scrollPane.getHorizontalScrollBar().setValue(scrollPane.getHorizontalScrollBar().getMaximum());
		scrollPane.getHorizontalScrollBar().setUnitIncrement(10);
		scrollPane.getVerticalScrollBar().setValue(0);
		redraw();
		validate();
		revalidate();
	}

	private void loadMapActionPerformed(java.awt.event.ActionEvent evt) {
		String s = JOptionPane.showInputDialog(this, "Map name?");
		loadLevel(s);
		
	}

	// sparar banan till fil
	private void saveMapActionPerformed(java.awt.event.ActionEvent evt) {
		String name = nameTextfield.getText();
		if (name.length() < 1) {
			JOptionPane.showMessageDialog(this, "Invalid map name", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		// kollar ifall filen finns is�fall fr�gas anv�ndaren om den vill
		// forts�tta
		File f = new File(MapLoader.defaultFolder + File.separator + name + ".txt");
		if (f.exists()) {
			int reply = JOptionPane.showConfirmDialog(this, "A file with that name already exists. \nDo you still want to continue?", "File already exists", JOptionPane.YES_OPTION);
			if (reply == 1) {
				return;
			}
		}

		// sparar mapen
		PrintWriter writer;
		try {
			writer = new PrintWriter(MapLoader.defaultFolder +  File.separator +  name + ".txt", "UTF-8");
			Game.information(0,"Saving to " + MapLoader.defaultFolder + File.separator +  name + ".txt");
			writer.println(width + "," + height);

			// Skriver ut tilesen i v�rlden till filen.
			writer.print("" + level.tiles[0]);
			for (int i = 1; i < level.tiles.length; i++) {
				writer.print("," + level.tiles[i]);
			}
			writer.println();
			for (Mob m : mobs) {
				writer.print(level.getMobID(m) + "," + m.getX() + "," + m.getY() + ";");
			}
			writer.println();
			writer.println((int)level.player.x +","+(int)level.player.y);
			writer.println((int)level.finnish.x+","+(int)level.finnish.y+","+level.finnish.width+","+level.finnish.height);

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException ex) {
			Game.information(2,"Couldnt create file or unsuported encoding");
		}

	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		int x, y;
		if (jTabbedPane1.getSelectedIndex() == 1) {
			x = (e.getX() + scrollPane.getHorizontalScrollBar().getValue());
			y = (img.getHeight() - e.getY() - scrollPane.getVerticalScrollBar().getValue());

			if (e.getButton() == MouseEvent.BUTTON3) {
				x = x - 18;
				y = y - 18;
				clearMob(x, y);
				return;
			}

			addMob(x, y);
		} else {
			x = (e.getX() + scrollPane.getHorizontalScrollBar().getValue()) / tileSize;
			y = (e.getY() + scrollPane.getVerticalScrollBar().getValue()) / tileSize;
			if (e.getButton() == MouseEvent.BUTTON3) {
				activeTile = 0;
			} else {
				activeTile = tileList.getSelectedIndex()+1;
			}

			addTile(x, y);
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		if (jTabbedPane1.getSelectedIndex() == 1) return;
		int x, y;
		x = (e.getX() + scrollPane.getHorizontalScrollBar().getValue()) / tileSize;
		y = (e.getY() + scrollPane.getVerticalScrollBar().getValue()) / tileSize;
		jLabel2.setText("" + (e.getX() + scrollPane.getHorizontalScrollBar().getValue()) / tileSize);
		jLabel3.setText("" + (e.getY() + scrollPane.getVerticalScrollBar().getValue()) / tileSize);
		addTile(x, y);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		jLabel2.setText("" + (e.getX() + scrollPane.getHorizontalScrollBar().getValue()) / tileSize);
		jLabel3.setText("" + (e.getY() + scrollPane.getVerticalScrollBar().getValue()) / tileSize);
		redraw();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
	}


	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	public static void main(String args[]) {
		try {
			for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					javax.swing.UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (Exception e) {
		}

		java.awt.EventQueue.invokeLater(() -> new Mapmaker().setVisible(true));
	}

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private JPanel drawPanel;
	private JLabel jLabel1;
	private JLabel jLabel2;
	private JLabel jLabel3;
	private JMenu jMenu1;
	private JMenu jMenu2;
	private JMenuBar jMenuBar1;
	private JMenuItem jMenuItem1;
	private JTabbedPane jTabbedPane1;
	private JMenuItem loadMap;
	private JTextField nameTextfield;
	private JMenuItem newMap;
	private JMenuItem saveMap;
	private JScrollPane scrollPane;

}
