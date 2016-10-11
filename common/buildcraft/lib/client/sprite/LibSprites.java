package buildcraft.lib.client.sprite;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class LibSprites {
    public static final SpriteHolder LOCK;
    public static final SpriteHolder WARNING_MINOR;
    public static final SpriteHolder WARNING_MAJOR;
    // Ownership sprites
    // TODO: Think about these sprites!
    public static final SpriteHolder OWNER_UNKNOWN;
    // Gui background related
    public static final SpriteHolder LEDGER_LEFT;
    public static final SpriteHolder LEDGER_RIGHT;

    static {
        LOCK = getHolder("icons/lock");
        WARNING_MINOR = getHolder("icons/warning_minor");
        WARNING_MAJOR = getHolder("icons/warning_major");
        OWNER_UNKNOWN = getHolder("icons/owner_unknown");
        LEDGER_LEFT = getHolder("icons/ledger_left");
        LEDGER_RIGHT = getHolder("icons/ledger_right");
    }

    public static void fmlPreInitClient() {
        // Nothing, just to call the static method
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftlib:" + suffix);
    }
}
