package buildcraft.lib;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class BCLibSprites {
    public static final SpriteHolder LOCK;
    public static final SpriteHolder WARNING_MINOR;
    public static final SpriteHolder WARNING_MAJOR;
    public static final SpriteHolder LOADING;

    public static final SpriteHolder LEDGER_LEFT;
    public static final SpriteHolder LEDGER_RIGHT;
    public static final SpriteHolder HELP;
    public static final SpriteHolder HELP_SPLIT;
    public static final SpriteHolder DEBUG;

    public static final SpriteHolder ENGINE_INACTIVE;
    public static final SpriteHolder ENGINE_ACTIVE;
    public static final SpriteHolder ENGINE_WARM;
    public static final SpriteHolder ENGINE_OVERHEAT;

    static {
        LOCK = getHolder("icons/lock");
        WARNING_MINOR = getHolder("icons/warning_minor");
        WARNING_MAJOR = getHolder("icons/warning_major");
        LOADING = getHolder("icons/loading");
        LEDGER_LEFT = getHolder("icons/ledger_left");
        LEDGER_RIGHT = getHolder("icons/ledger_right");
        HELP = getHolder("icons/help");
        HELP_SPLIT = getHolder("icons/help_split");
        DEBUG = getHolder("items/debugger");
        ENGINE_INACTIVE = getHolder("icons/engine_inactive");
        ENGINE_ACTIVE = getHolder("icons/engine_active");
        ENGINE_WARM = getHolder("icons/engine_warm");
        ENGINE_OVERHEAT = getHolder("icons/engine_overheat");
    }

    public static void fmlPreInitClient() {
        // Nothing, just to call the static method
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftlib:" + suffix);
    }
}
