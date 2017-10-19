package spel;

public class Log {

    public static final int INFORMATION = 0;
    public static final int WARNING = 1;
    public static final int SEVERE = 2;
    private static final int filter = 0;

    public static void log(int type, String message){
        String typeText = "";
        switch(type){
            case INFORMATION:
                if(filter > 0) return;
                typeText = "[INFORMATION] ";
                break;
            case WARNING:
                if(filter > 1) return;
                typeText = "[WARNING] ";
                break;
            case SEVERE:
                if(filter > 2) return;
                typeText = "[SEVERE] ";
                break;
        }
        System.out.println(typeText + message);
    }
}
