/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;


import buildcraft.api.enums.EnumPowerStage;
import buildcraft.api.tiles.IControllable;
import buildcraft.core.statements.TriggerFluidContainer;
import buildcraft.core.statements.TriggerFluidContainerLevel;
import buildcraft.core.statements.TriggerInventory;
import buildcraft.core.statements.TriggerInventoryLevel;
import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class BCCoreSprites {
    public static final SpriteHolder MARKER_VOLUME_CONNECTED;
    public static final SpriteHolder MARKER_VOLUME_POSSIBLE;
    public static final SpriteHolder MARKER_VOLUME_SIGNAL;
    public static final SpriteHolder MARKER_PATH_CONNECTED;
    public static final SpriteHolder MARKER_PATH_POSSIBLE;
    public static final SpriteHolder MARKER_DEFAULT_POSSIBLE;

    public static final SpriteHolder STRIPES_READ;
    public static final SpriteHolder STRIPES_WRITE;
    public static final SpriteHolder STRIPES_WRITE_DIRECTION;

    public static final SpriteHolder LASER_POWER_LOW;
    public static final SpriteHolder LASER_POWER_MED;
    public static final SpriteHolder LASER_POWER_HIGH;
    public static final SpriteHolder LASER_POWER_FULL;

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
    public static final Map<TriggerInventoryLevel.TriggerType, SpriteHolder> TRIGGER_INVENTORY_LEVEL;
    public static final Map<TriggerFluidContainer.State, SpriteHolder> TRIGGER_FLUID;
    public static final Map<TriggerFluidContainerLevel.TriggerType, SpriteHolder> TRIGGER_FLUID_LEVEL;
    public static final Map<EnumPowerStage, SpriteHolder> TRIGGER_POWER_STAGE;

    static {
        MARKER_VOLUME_CONNECTED = getHolder("lasers/marker_volume_connected");
        MARKER_VOLUME_POSSIBLE = getHolder("lasers/marker_volume_possible");
        MARKER_VOLUME_SIGNAL = getHolder("lasers/marker_volume_signal");
        MARKER_PATH_CONNECTED = getHolder("lasers/marker_path_connected");
        MARKER_PATH_POSSIBLE = getHolder("lasers/marker_path_possible");
        MARKER_DEFAULT_POSSIBLE = getHolder("lasers/marker_default_possible");

        STRIPES_READ = getHolder("lasers/stripes_read");
        STRIPES_WRITE = getHolder("lasers/stripes_write");
        STRIPES_WRITE_DIRECTION = getHolder("lasers/stripes_write_direction");

        LASER_POWER_LOW = getHolder("lasers/power_low");
        LASER_POWER_MED = getHolder("lasers/power_med");
        LASER_POWER_HIGH = getHolder("lasers/power_high");
        LASER_POWER_FULL = getHolder("lasers/power_full");

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
            PARAM_REDSTONE_LEVEL[i] = getHolder("triggers/parameter_redstone_" + i);
        }

        ACTION_MACHINE_CONTROL = new EnumMap<>(IControllable.Mode.class);
        for (IControllable.Mode mode : IControllable.Mode.VALUES) {
            String tex = "triggers/action_machinecontrol_" + mode.name().toLowerCase(Locale.ROOT);
            ACTION_MACHINE_CONTROL.put(mode, getHolder(tex));
        }

        TRIGGER_INVENTORY = new EnumMap<>(TriggerInventory.State.class);
        for (TriggerInventory.State state : TriggerInventory.State.VALUES) {
            String tex = "triggers/trigger_inventory_" + state.name().toLowerCase(Locale.ROOT);
            TRIGGER_INVENTORY.put(state, getHolder(tex));
        }

        TRIGGER_INVENTORY_LEVEL = new EnumMap<>(TriggerInventoryLevel.TriggerType.class);
        for (TriggerInventoryLevel.TriggerType state : TriggerInventoryLevel.TriggerType.VALUES) {
            String tex = "triggers/trigger_inventory_" + state.name().toLowerCase(Locale.ROOT);
            TRIGGER_INVENTORY_LEVEL.put(state, getHolder(tex));
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

        TRIGGER_POWER_STAGE = new EnumMap<>(EnumPowerStage.class);
        for (EnumPowerStage stage : EnumPowerStage.values()) {
            if (stage == EnumPowerStage.BLACK) continue;
            String tex = "triggers/trigger_engineheat_" + stage.getSerializedName();
            TRIGGER_POWER_STAGE.put(stage, getHolder(tex));
        }
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftcore:" + suffix);
    }

    public static void fmlPreInit() {
        // Nothing, just to register the sprites
    }
}
