package buildcraft.lib.client.sprite;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class LibSprites {
    public static final SpriteHolder LOCK;
    public static final SpriteHolder WARNING_MINOR;
    public static final SpriteHolder WARNING_MAJOR;
    // Ownership sprites
    // TODO: Think about these sprites!
    public static final SpriteHolder LOADING;
    // Gui background related
    public static final SpriteHolder LEDGER_LEFT;
    public static final SpriteHolder LEDGER_RIGHT;
    public static final SpriteHolder HELP;
    public static final SpriteHolder HELP_SPLIT;

    static {
        LOCK = getHolder("icons/lock");
        WARNING_MINOR = getHolder("icons/warning_minor");
        WARNING_MAJOR = getHolder("icons/warning_major");
        LOADING = getHolder("icons/loading");
        LEDGER_LEFT = getHolder("icons/ledger_left");
        LEDGER_RIGHT = getHolder("icons/ledger_right");
        HELP = getHolder("icons/help");
        HELP_SPLIT = getHolder("icons/help_split");
    }

    public static void fmlPreInitClient() {
        // Nothing, just to call the static method
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftlib:" + suffix);
    }
}
