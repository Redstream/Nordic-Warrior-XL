package spel;

public class Log {

    private final static int information = 0;


    public static void log(int type, String message){
        String typeText = "";
        switch(type){
            case 0:
                if(information > 0) return;
                typeText = "[INFORMATION] ";
                break;
            case 1:
                if(information > 1) return;
                typeText = "[WARNING] ";
                break;
            case 2:
                if(information > 2) return;
                typeText = "[SEVERE] ";
                break;
        }
        System.out.println(typeText + message);
    }
}
