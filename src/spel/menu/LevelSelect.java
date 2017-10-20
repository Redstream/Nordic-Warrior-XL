package spel.menu;

import spel.Game;
import spel.level.Level;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class LevelSelect extends JPanel {

    private static final long serialVersionUID = 1L;
    private final JList<String> list;

    private final JEditorPane textarea;
    private final Menu parent;

    LevelSelect(final Menu parent) {
        this.parent = parent;
        System.out.println(Level.mapfolder);
        String[] maps = listMaps(Level.mapfolder);
        list = new JList<>(maps);
        list.setSize(list.getWidth(), 100);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setLayoutOrientation(JList.HORIZONTAL_WRAP);
        list.setVisibleRowCount(30);

        JScrollPane jscrollpane = new JScrollPane(list);
        jscrollpane.setLocation(0, 0);
        add(jscrollpane);

        textarea = new JEditorPane("text/html", "");
        textarea.setEditable(false);
        add(textarea);
        setText("Select a map.");

        MouseListener mouseListener = new MouseAdapter() {
            String lastclicked = "";

            public void mouseClicked(MouseEvent e) {
                String level = list.getSelectedValue();

                if (!lastclicked.equalsIgnoreCase(list.getSelectedValue())) {
                    try {
                        BufferedReader in = new BufferedReader(new FileReader(Level.mapfolder + File.separator + level + ".desc.txt"));
                        String s = "";
                        String line;
                        while ((line = in.readLine()) != null) {
                            s += line + "<br>";
                        }
                        in.close();
                        setText("<b>Name:</b> " + level + "<br><b>Description:</b><br>" + s);
                    } catch (Exception ex) {
                        setText("<b>Name:</b><br> " + level + "<br><b>Description:</b><br>No map info");
                    }
                    lastclicked = list.getSelectedValue();
                } else {
                    setLevel(list.getSelectedValue() + ".txt");
                }
            }
        };
        list.addMouseListener(mouseListener);

    }

    public void setLevel(String level) {
        parent.changeCard("1");
        Game.game.setLevel(level);
        Game.game.togglePause();
    }

    public void setText(String s) {
        textarea.setText(s);
    }

    public void updateList() {
        list.setListData(listMaps(Level.mapfolder));
    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Menu.paintBg(g, this);
    }

    public static String[] listMaps(final File folder) {

        ArrayList<String> maps = new ArrayList<String>();
        if (!folder.exists()) {
            folder.mkdirs();
            Game.information(0, "New map folder created.");
        }
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listMaps(fileEntry);
            } else {
                if (fileEntry.getName().split(".desc.").length == 1) {
                    maps.add(fileEntry.getName().split(".txt")[0]);
                }
            }
        }
        String[] mapsArray = new String[maps.size()];
        for (int i = 0; i < mapsArray.length; i++) {
            mapsArray[i] = maps.get(i);
        }
        return mapsArray;
    }

}
