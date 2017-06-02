package spel.menu;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class Menu extends JPanel{
	private static final long serialVersionUID = 1L;
			

	public int width, height;
	
	public StartMenu startMenu;
	public LevelSelect lvlselect;
	
	private CardLayout cl = new CardLayout();
	
	public Menu(int width, int height){
		this.width = width;
		this.height = height;
		setLayout(cl);
		startMenu = new StartMenu(width,height,this);
		lvlselect = new LevelSelect(this);
		
		add(startMenu, "1");
		add(lvlselect, "2");			
	}
	
	public void changeCard(String index){
		lvlselect.updateList();
		cl.show(this, index);
	}
	
	public static void paintBg(Graphics g, JPanel j){
		g.setColor(Color.black);
		
		BufferedImage img;
		try{
			img = ImageIO.read(new File("src/res/graphics/nordic_warriorXL.jpg"));
			g.drawImage(img, 0, 0, j.getWidth(),j.getHeight(),null);
		}catch(Exception e){
			//Game.information(2,e.toString());
		}
		
		
	}

}
