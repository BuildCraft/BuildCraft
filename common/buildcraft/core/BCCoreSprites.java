/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team This Source Code Form is subject to the terms of the Mozilla
 * Public License, v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at
 * https://mozilla.org/MPL/2.0/
 */

package buildcraft.core;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.util.EnumFacing;

import buildcraft.api.tiles.IControllable;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.core.builders.patterns.PatternParameterCenter;
import buildcraft.core.statements.TriggerFluidContainer;
import buildcraft.core.statements.TriggerFluidContainerLevel;
import buildcraft.core.statements.TriggerInventory;
import buildcraft.core.statements.TriggerInventoryLevel;

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

    public static final SpriteHolder FILLER_NONE;
    public static final SpriteHolder FILLER_CLEAR;
    public static final SpriteHolder FILLER_FILL;
    public static final SpriteHolder FILLER_BOX;
    public static final SpriteHolder FILLER_FRAME;
    public static final SpriteHolder FILLER_FLATTEN;
    public static final SpriteHolder FILLER_HORIZON;
    public static final SpriteHolder FILLER_CYLINDER;
    public static final SpriteHolder FILLER_PYRAMID;
    public static final SpriteHolder FILLER_STAIRS;

    public static final SpriteHolder PARAM_HOLLOW;
    public static final SpriteHolder PARAM_FILLED;

    public static final SpriteHolder PARAM_STAIRS_DOWN;
    public static final SpriteHolder PARAM_STAIRS_UP;

    public static final SpriteHolder[] PARAM_REDSTONE_LEVEL;

    public static final Map<IControllable.Mode, SpriteHolder> ACTION_MACHINE_CONTROL;
    public static final Map<TriggerInventory.State, SpriteHolder> TRIGGER_INVENTORY;
    public static final Map<TriggerInventoryLevel.TriggerType, SpriteHolder> TRIGGER_INVENTORY_LEVEL;
    public static final Map<TriggerFluidContainer.State, SpriteHolder> TRIGGER_FLUID;
    public static final Map<TriggerFluidContainerLevel.TriggerType, SpriteHolder> TRIGGER_FLUID_LEVEL;
    public static final Map<EnumFacing, SpriteHolder> PARAM_XZ_DIR;
    public static final Map<PatternParameterCenter, SpriteHolder> PARAM_CENTER;

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

        FILLER_NONE = getHolder("filler/patterns/none");
        FILLER_CLEAR = getHolder("filler/patterns/clear");
        FILLER_FILL = getHolder("filler/patterns/fill");
        FILLER_BOX = getHolder("filler/patterns/box");
        FILLER_FRAME = getHolder("filler/patterns/frame");
        FILLER_FLATTEN = getHolder("filler/patterns/flatten");
        FILLER_HORIZON = getHolder("filler/patterns/horizon");
        FILLER_CYLINDER = getHolder("filler/patterns/cylinder");
        FILLER_PYRAMID = getHolder("filler/patterns/pyramid");
        FILLER_STAIRS = getHolder("filler/patterns/stairs");

        PARAM_HOLLOW = getHolder("filler/parameters/hollow");
        PARAM_FILLED = getHolder("filler/parameters/filled");

        PARAM_STAIRS_UP = getHolder("filler/parameters/stairs_ascend");
        PARAM_STAIRS_DOWN = getHolder("filler/parameters/stairs_descend");

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

        PARAM_XZ_DIR = new EnumMap<>(EnumFacing.class);
        PARAM_XZ_DIR.put(EnumFacing.WEST, getHolder("filler/parameters/arrow_left"));
        PARAM_XZ_DIR.put(EnumFacing.EAST, getHolder("filler/parameters/arrow_right"));
        PARAM_XZ_DIR.put(EnumFacing.NORTH, getHolder("filler/parameters/arrow_up"));
        PARAM_XZ_DIR.put(EnumFacing.SOUTH, getHolder("filler/parameters/arrow_down"));

        PARAM_CENTER = new EnumMap<>(PatternParameterCenter.class);
        for (PatternParameterCenter param : PatternParameterCenter.values()) {
            PARAM_CENTER.put(param, getHolder("filler/parameters/center_" + param.ordinal()));
        }
    }

    private static SpriteHolder getHolder(String suffix) {
        return SpriteHolderRegistry.getHolder("buildcraftcore:" + suffix);
    }

    public static void fmlPreInit() {
        // Nothing, just to register the sprites
    }
}
