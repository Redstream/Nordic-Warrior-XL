package spel;

import java.awt.event.KeyEvent;

public class Settings {

    public final static String NAME = "Nordic-Warrior-XL";
    public final static int stats = 2;

    // Window settings
    public final static int WIDTH = 900;
    public final static int HEIGHT = WIDTH * 9 / 16;
    final static float scale = 2f;
    final static boolean FULLSCREEN = false;

    // Key Bindings
    public static final int JUMP_KEY = KeyEvent.VK_SPACE;
    public static final int RIGHT_KEY = KeyEvent.VK_D;
    public static final int RIGHT_KEY_2 = KeyEvent.VK_RIGHT;
    public static final int LEFT_KEY = KeyEvent.VK_A;
    public static final int LEFT_KEY_2 = KeyEvent.VK_LEFT;
    public static final int ATTACK_KEY = KeyEvent.VK_X;
    public static final int DOWN_KEY = KeyEvent.VK_S;
    public static final int WALK_KEY = KeyEvent.VK_SHIFT;
    public static final int RESTART_KEY = KeyEvent.VK_R;
    public static final int ESC_KEY = KeyEvent.VK_ESCAPE;
    public static final int PAUSE_KEY = KeyEvent.VK_ESCAPE;
    public static final int CHECKPOINT_KEY = KeyEvent.VK_C;
    private static final int FPSLOCK_KEY = KeyEvent.VK_F;
}
