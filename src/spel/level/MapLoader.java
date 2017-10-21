package spel.level;

import spel.Settings;
import org.apache.commons.io.FileUtils;
import spel.Game;
import spel.Log;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;

public class MapLoader {

    public static File defaultFolder = new File(System.getProperty("user.home") + File.separator + Settings.NAME + File.separator + "maps");
    public static String[] defaultMaps = {"Demo-map", "Franzjump", "Franzmaze"};

    /**
     * Add all demo maps.
     * Used to make sure player has at least a few maps.
     */
    public static void addDemoMaps() {
        if (listMaps(defaultFolder).length == 0) {
            try {
                URL inputUrl;
                File dest;
                for (String map : defaultMaps) {
                    inputUrl = Game.class.getResource("/res/maps/demo/" + map + ".txt");
                    dest = new File(defaultFolder + File.separator + map + ".txt");
                    FileUtils.copyURLToFile(inputUrl, dest);
                    inputUrl = Game.class.getResource("/res/maps/demo/" + map + ".desc.txt");
                    if (inputUrl != null) {
                        dest = new File(defaultFolder + File.separator + map + " .desc.txt");
                        FileUtils.copyURLToFile(inputUrl, dest);
                    }
                }

            } catch (Exception e) {
                Log.msg(Log.SEVERE, e.toString());
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

    /**
     * List all saved Maps
     * @return All saved maps
     */
    public static String[] listMaps() {
        return listMaps(defaultFolder);
    }
}
