package spel.menu;

import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import mapeditor.Mapmaker;


public class StartMenu extends JPanel {

	private static final long serialVersionUID = 1L;

	public int width = 800,height =  600;
	public StartMenu(int width, int heigth, final Menu parent) {
		this.width = width;
		this.height = heigth;
		
		setLayout(null);
		
		JButton btnPlay = new JButton("Play");
		btnPlay.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				parent.changeCard("2");
			}
		});
		btnPlay.setBounds(width/2-50, height/4, 100, 25);
		add(btnPlay);
		
		JButton btnMapEditor = new JButton("Create");
		
		btnMapEditor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Mapmaker.main(new String[]{});
			}
		});
		btnMapEditor.setBounds(width/2-50, height/4+50,100, 25);
		add(btnMapEditor);
		btnMapEditor.setEnabled(true);
		
		JButton btnExit = new JButton("Exit");
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		btnExit.setBounds(width/2-50, height/4+100, 100, 25);
		add(btnExit);
		
		
	}
	
	public void paintComponent(Graphics g){
		super.paintComponents(g);
		Menu.paintBg(g,this);
	}
	

}
