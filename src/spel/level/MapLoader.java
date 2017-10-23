package spel.level;

import spel.Settings;
import spel.Game;
import spel.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;

public class MapLoader {

    public static File defaultFolder = new File(System.getProperty("user.home") + File.separator + Settings.NAME + File.separator + "maps");
    public static String fileExtension = ".map";
    public static String descExtension = "-description" + fileExtension;
    private static String[] defaultMaps = {"Demo", "Franzjump", "Franzmaze"};

    /**
     * Add all demo maps.
     * Used to make sure player has at least a few maps.
     */
    public static void addDemoMaps() {
        if (listMaps(defaultFolder).length == 0) {
            for (String map : defaultMaps) {
                // Map
                try (InputStream from = Game.class.getResourceAsStream("/res/maps/demo/" + map + fileExtension)) {
                    File to = new File(defaultFolder + File.separator + map + fileExtension);
                    if(from.available() > 0) {
                        Files.copy(from, to.toPath());
                    }
                } catch (NullPointerException | IOException e) {
                    Log.msg(Log.SEVERE, "Couldn't read map file: " + e.toString());
                }

                // Description
                try (InputStream from = Game.class.getResourceAsStream("/res/maps/demo/" + map + descExtension)) {
                    File to = new File(defaultFolder + File.separator + map + descExtension);
                    if(from.available() > 0) {
                        Files.copy(from, to.toPath());
                    }
                } catch (NullPointerException | IOException e) {
                    Log.msg(Log.INFORMATION, "Couldn't read map description file for " + map);
                }
            }
        }
    }

    /**
     * List all saved Maps
     * @param folder
     * @return All saved maps
     */
    public static String[] listMaps(final File folder) {
        ArrayList<String> maps = new ArrayList<String>();
        if (!folder.exists()) {
            folder.mkdirs();
            Log.msg(Log.INFORMATION, "New map folder created.");
        }
        for (File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                listMaps(fileEntry);
            } else {
                if(!fileEntry.getName().contains(MapLoader.descExtension)) {
                    maps.add(fileEntry.getName().split(MapLoader.fileExtension)[0]);
                }
            }
        }
        String[] mapsArray = new String[maps.size()];
        for (int i = 0; i < mapsArray.length; i++) {
            mapsArray[i] = maps.get(i);
        }
        return mapsArray;
    }

    /**
     * List all saved Maps
     * @return All saved maps
     */
    public static String[] listMaps() {
        return listMaps(defaultFolder);
    }
}
