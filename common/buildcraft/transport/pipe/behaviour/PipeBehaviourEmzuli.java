/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.pipe.behaviour;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeBehaviour;

import buildcraft.lib.tile.item.ItemHandlerSimple;

// STUB(R.Chen): full PipeBehaviourEmzuli (extends PipeBehaviourWood) deferred — pipeline dep.
// Minimal fields + SlotIndex for container compile.
public class PipeBehaviourEmzuli extends PipeBehaviour {

    public enum SlotIndex {
        SQUARE(DyeColor.RED),
        CIRCLE(DyeColor.GREEN),
        TRIANGLE(DyeColor.BLUE),
        CROSS(DyeColor.YELLOW);

        public static final SlotIndex[] VALUES = values();

        public final DyeColor colour;

        SlotIndex(DyeColor colour) {
            this.colour = colour;
        }
    }

    public final ItemHandlerSimple invFilters = new ItemHandlerSimple(4, null);
    public final EnumMap<SlotIndex, DyeColor> slotColours = new EnumMap<>(SlotIndex.class);

    public PipeBehaviourEmzuli(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourEmzuli(IPipe pipe, NbtCompound nbt) {
        super(pipe, nbt);
    }
}
