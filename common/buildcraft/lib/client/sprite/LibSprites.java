package buildcraft.lib.client.sprite;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class LibSprites {
    public static final SpriteHolder LOCK;
    public static final SpriteHolder WARNING_MINOR;
    public static final SpriteHolder WARNING_MAJOR;
    // Ownership sprites
    // TODO: Think about these sprites!
    public static final SpriteHolder USABLE_ANYONE;
    public static final SpriteHolder USABLE_OWNER_ONLY;
    public static final SpriteHolder USABLE_NO_AUTO;
    // Gui background related
    public static final SpriteHolder LEDGER_LEFT;
    public static final SpriteHolder LEDGER_RIGHT;

    static {
        LOCK = getHolder("icons/lock");
        WARNING_MINOR = getHolder("icons/warning_minor");
        WARNING_MAJOR = getHolder("icons/warning_major");
        USABLE_ANYONE = getHolder("icons/ownership_anyone");
        USABLE_OWNER_ONLY = getHolder("icons/ownership_owner_only");
        USABLE_NO_AUTO = getHolder("icons/ownership_no_auto");
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
