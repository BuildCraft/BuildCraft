/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;

import buildcraft.lib.client.sprite.SpriteHolderRegistry;
import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

public class BCTransportSprites {

    private static final String NS = "buildcrafttransport:";

    public static final SpriteHolder EMPTY_FILTERED_BUFFER_SLOT =
        getHolder("gui/empty_filtered_buffer_slot");
    public static final SpriteHolder NOTHING_FILTERED_BUFFER_SLOT =
        getHolder("gui/nothing_filtered_buffer_slot");
    public static final SpriteHolder PIPE_COLOUR            = getHolder("pipes/overlay_stained");
    public static final SpriteHolder COLOUR_ITEM_BOX        = getHolder("pipes/colour_item_box");
    public static final SpriteHolder PIPE_COLOUR_BORDER_OUTER = getHolder("pipes/colour_border_outer");
    public static final SpriteHolder PIPE_COLOUR_BORDER_INNER = getHolder("pipes/colour_border_inner");

    public static final SpriteHolder TRIGGER_POWER_REQUESTED   = getHolder("triggers/trigger_power_requested");
    public static final SpriteHolder TRIGGER_ITEMS_TRAVERSING  = getHolder("triggers/trigger_items_traversing");
    public static final SpriteHolder TRIGGER_FLUIDS_TRAVERSING = getHolder("triggers/trigger_fluids_traversing");

    public static final SpriteHolder POWER_FLOW          = getHolder("pipes/power_flow");
    public static final SpriteHolder POWER_FLOW_OVERLOAD = getHolder("pipes/power_flow_overload");
    public static final SpriteHolder POWER_FLOW_RF       = getHolder("pipes/power_flow_rf");

    public static final SpriteHolder[] ACTION_PIPE_COLOUR;
    public static final SpriteHolder[] POWER_LIMIT;
    public static final SpriteHolder[] POWER_LIMIT_RF;
    public static final EnumMap<SlotIndex, SpriteHolder> ACTION_EXTRACTION_PRESET;

    private static final EnumMap<DyeColor, SpriteHolder> PIPE_SIGNAL_ON  = new EnumMap<>(DyeColor.class);
    private static final EnumMap<DyeColor, SpriteHolder> PIPE_SIGNAL_OFF = new EnumMap<>(DyeColor.class);
    private static final EnumMap<Direction, SpriteHolder> ACTION_PIPE_DIRECTION = new EnumMap<>(Direction.class);

    static {
        ACTION_PIPE_COLOUR = new SpriteHolder[DyeColor.values().length];
        POWER_LIMIT    = new SpriteHolder[8];
        POWER_LIMIT_RF = new SpriteHolder[8];
        for (DyeColor c : DyeColor.values()) {
            String name = c.getName();
            ACTION_PIPE_COLOUR[c.getId()] = getHolder("actions/action_pipe_colour_" + name);
            PIPE_SIGNAL_ON.put(c,  getHolder("triggers/pipe_signal_on_"  + name));
            PIPE_SIGNAL_OFF.put(c, getHolder("triggers/pipe_signal_off_" + name));
        }
        for (int i = 0; i < 8; i++) {
            POWER_LIMIT[i]    = getHolder("triggers/trigger_power_limit_"    + i);
            POWER_LIMIT_RF[i] = getHolder("triggers/trigger_power_limit_rf_" + i);
        }
        ACTION_EXTRACTION_PRESET = new EnumMap<>(SlotIndex.class);
        for (SlotIndex slot : SlotIndex.values()) {
            ACTION_EXTRACTION_PRESET.put(slot,
                getHolder("actions/action_extraction_preset_" + slot.name().toLowerCase()));
        }
        for (Direction face : Direction.values()) {
            ACTION_PIPE_DIRECTION.put(face,
                getHolder("actions/action_pipe_direction_" + face.getName()));
        }
    }

    private static SpriteHolder getHolder(String path) {
        return SpriteHolderRegistry.getHolder(NS + path);
    }

    public static SpriteHolder getPipeSignal(boolean active, DyeColor colour) {
        return (active ? PIPE_SIGNAL_ON : PIPE_SIGNAL_OFF).get(colour);
    }

    public static SpriteHolder getPipeDirection(Direction face) {
        return ACTION_PIPE_DIRECTION.get(face);
    }
}
