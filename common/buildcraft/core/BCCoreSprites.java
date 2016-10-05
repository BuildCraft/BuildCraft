package buildcraft.core;

import buildcraft.core.statements.StatementParamGateSideOnly;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

public class BCCoreSprites {
    public static final SpriteHolder TRIGGER_TRUE;
    public static final SpriteHolder PARAM_GATE_SIDE_ONLY;

    public static final SpriteHolder TRIGGER_MACHINE_ACTIVE;
    public static final SpriteHolder TRIGGER_MACHINE_INACTIVE;

    public static final SpriteHolder TRIGGER_REDSTONE_ACTIVE;
    public static final SpriteHolder TRIGGER_REDSTONE_INACTIVE;
    public static final SpriteHolder ACTION_REDSTONE;

    static {
        TRIGGER_TRUE = getHolder("triggers/trigger_true");
        PARAM_GATE_SIDE_ONLY = getHolder("triggers/redstone_gate_side_only");

        TRIGGER_MACHINE_ACTIVE = getHolder("triggers/trigger_machine_active");
        TRIGGER_MACHINE_INACTIVE = getHolder("triggers/trigger_machine_inactive");

        TRIGGER_REDSTONE_ACTIVE = getHolder("triggers/trigger_redstoneinput_active");
        TRIGGER_REDSTONE_INACTIVE = getHolder("triggers/trigger_redstoneinput_inactive");
        ACTION_REDSTONE = getHolder("triggers/action_redstoneoutput");

        // statement sprite setting
        BCCoreStatements.TRIGGER_TRUE.setSpriteHolder(TRIGGER_TRUE);
        StatementParamGateSideOnly.sprite = PARAM_GATE_SIDE_ONLY;
        BCCoreStatements.TRIGGER_MACHINE_ACTIVE.setSpriteHolder(TRIGGER_MACHINE_ACTIVE);
        BCCoreStatements.TRIGGER_MACHINE_INACTIVE.setSpriteHolder(TRIGGER_MACHINE_INACTIVE);

        BCCoreStatements.TRIGGER_REDSTONE_ACTIVE.setSpriteHolder(TRIGGER_REDSTONE_ACTIVE);
        BCCoreStatements.TRIGGER_REDSTONE_INACTIVE.setSpriteHolder(TRIGGER_REDSTONE_INACTIVE);
        BCCoreStatements.ACTION_REDSTONE.setSpriteHolder(ACTION_REDSTONE);
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftcore:" + suffix);
    }

    public static void fmlPreInit() {
        // Nothing, just to register the sprites
    }
}
