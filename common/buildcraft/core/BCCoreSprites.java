package buildcraft.core;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import buildcraft.api.tiles.IControllable;

import buildcraft.core.statements.TriggerFluidContainer;
import buildcraft.core.statements.TriggerFluidContainerLevel;
import buildcraft.core.statements.TriggerInventory;
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

    public static final SpriteHolder TRIGGER_POWER_HIGH;
    public static final SpriteHolder TRIGGER_POWER_LOW;

    public static final SpriteHolder[] PARAM_REDSTONE_LEVEL;

    public static final Map<IControllable.Mode, SpriteHolder> ACTION_MACHINE_CONTROL;
    public static final Map<TriggerInventory.State, SpriteHolder> TRIGGER_INVENTORY;
    public static final Map<TriggerFluidContainer.State, SpriteHolder> TRIGGER_FLUID;
    public static final Map<TriggerFluidContainerLevel.TriggerType, SpriteHolder> TRIGGER_FLUID_LEVEL;

    static {
        TRIGGER_TRUE = getHolder("triggers/trigger_true");
        PARAM_GATE_SIDE_ONLY = getHolder("triggers/redstone_gate_side_only");

        TRIGGER_MACHINE_ACTIVE = getHolder("triggers/trigger_machine_active");
        TRIGGER_MACHINE_INACTIVE = getHolder("triggers/trigger_machine_inactive");

        TRIGGER_REDSTONE_ACTIVE = getHolder("triggers/trigger_redstoneinput_active");
        TRIGGER_REDSTONE_INACTIVE = getHolder("triggers/trigger_redstoneinput_inactive");
        ACTION_REDSTONE = getHolder("triggers/action_redstoneoutput");

        TRIGGER_POWER_HIGH = getHolder("triggers/trigger_energy_storage_high");
        TRIGGER_POWER_LOW = getHolder("triggers/trigger_energy_storage_low");

        PARAM_REDSTONE_LEVEL = new SpriteHolder[16];
        for (int i = 0; i < PARAM_REDSTONE_LEVEL.length; i++) {
            PARAM_REDSTONE_LEVEL[i] = getHolder("buildcraftcore:triggers/parameter_redstone_" + i);
        }

        ACTION_MACHINE_CONTROL = new EnumMap<>(IControllable.Mode.class);
        for (IControllable.Mode mode : IControllable.Mode.VALID_VALUES) {
            String tex = "triggers/action_machinecontrol_" + mode.name().toLowerCase(Locale.ROOT);
            ACTION_MACHINE_CONTROL.put(mode, getHolder(tex));
        }

        TRIGGER_INVENTORY = new EnumMap<>(TriggerInventory.State.class);
        for (TriggerInventory.State state : TriggerInventory.State.VALUES) {
            String tex = "triggers/trigger_inventory_" + state.name().toLowerCase(Locale.ROOT);
            TRIGGER_INVENTORY.put(state, getHolder(tex));
        }

        TRIGGER_FLUID = new EnumMap<>(TriggerFluidContainer.State.class);
        for (TriggerFluidContainer.State state : TriggerFluidContainer.State.VALUES) {
            String tex = "triggers/trigger_liquidcontainer_" + state.name().toLowerCase(Locale.ROOT);
            TRIGGER_FLUID.put(state, getHolder(tex));
        }

        TRIGGER_FLUID_LEVEL = new EnumMap<>(TriggerFluidContainerLevel.TriggerType.class);
        for (TriggerFluidContainerLevel.TriggerType type : TriggerFluidContainerLevel.TriggerType.VALUES) {
            String tex = "triggers/trigger_liquidcontainer_" + type.name().toLowerCase(Locale.ROOT);
            TRIGGER_FLUID_LEVEL.put(type, getHolder(tex));
        }
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftcore:" + suffix);
    }

    public static void fmlPreInit() {
        // Nothing, just to register the sprites
    }
}
