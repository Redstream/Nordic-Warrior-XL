package game;

public class Log {

    public static final int INFORMATION = 0;
    public static final int WARNING = 1;
    public static final int SEVERE = 2;
    private static final int filter = 0;

    public static void msg(int type, String message) {
        String typeText = "";
        switch (type) {
            case INFORMATION:
                if (filter > 0) return;
                typeText = (char)27 + "[32m[INFORMATION] ";
                break;
            case WARNING:
                if (filter > 1) return;
                typeText = (char)27 + "[33m[WARNING] ";
                break;
            case SEVERE:
                if (filter > 2) return;
                typeText = (char)27 + "[31m[SEVERE] ";
                break;
        }
        System.out.println(typeText + message);
    }

    public static void debug(String s) {
        System.out.println("[DEBUG] " + s);
    }
}
