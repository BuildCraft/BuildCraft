/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport;

import java.util.EnumMap;

import net.minecraft.util.DyeColor;
import net.minecraft.util.math.Direction;

import buildcraft.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

import buildcraft.transport.pipe.behaviour.PipeBehaviourEmzuli.SlotIndex;

// STUB(R.Chen): sprites registered in Phase 5.
// All fields are null until the Fabric sprite-atlas pipeline is wired.
public class BCTransportSprites {

    public static final SpriteHolder EMPTY_FILTERED_BUFFER_SLOT = null;
    public static final SpriteHolder NOTHING_FILTERED_BUFFER_SLOT = null;
    public static final SpriteHolder PIPE_COLOUR = null;
    public static final SpriteHolder COLOUR_ITEM_BOX = null;
    public static final SpriteHolder PIPE_COLOUR_BORDER_OUTER = null;
    public static final SpriteHolder PIPE_COLOUR_BORDER_INNER = null;

    public static final SpriteHolder TRIGGER_POWER_REQUESTED = null;
    public static final SpriteHolder TRIGGER_ITEMS_TRAVERSING = null;
    public static final SpriteHolder TRIGGER_FLUIDS_TRAVERSING = null;

    // STUB(R.Chen): array/map fields null — Phase 5 will populate these from the atlas.
    public static final SpriteHolder[] ACTION_PIPE_COLOUR = null;
    public static final EnumMap<SlotIndex, SpriteHolder> ACTION_EXTRACTION_PRESET = null;

    public static final SpriteHolder POWER_FLOW = null;
    public static final SpriteHolder POWER_FLOW_OVERLOAD = null;
    public static final SpriteHolder POWER_FLOW_RF = null;

    public static final SpriteHolder[] POWER_LIMIT = null;
    public static final SpriteHolder[] POWER_LIMIT_RF = null;

    // STUB(R.Chen): pipe-signal and direction maps null until Phase 5.
    public static SpriteHolder getPipeSignal(boolean active, DyeColor colour) {
        return null;
    }

    public static SpriteHolder getPipeDirection(Direction face) {
        return null;
    }
}
