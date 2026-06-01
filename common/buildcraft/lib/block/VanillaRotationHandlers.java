/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib.block;

import net.minecraft.util.math.Direction;
import java.util.Map;

import buildcraft.lib.misc.collect.OrderedEnumMap;

// STUB(R.Chen): 1.12.2 block-specific rotation handlers (BlockAnvil, BlockButton, BlockLever, etc.) and
// Forge ObfuscationReflectionHelper / CustomRotationHelper removed. Only the static rotation-order maps
// are preserved; they are all that TileEngineBase_BC8 (and future BC block wrenching) requires.
// TODO(R.Chen): re-introduce per-block handlers once BlockState properties are ported to 1.20.1.
public class VanillaRotationHandlers {

    /** Player-friendly all-six-faces rotation order (used by engines). */
    public static final OrderedEnumMap<Direction> ROTATE_FACING;
    /** Horizontal-only rotation order (N→E→S→W). */
    public static final OrderedEnumMap<Direction> ROTATE_HORIZONTAL;
    /** Torch rotation order (excludes DOWN). */
    public static final OrderedEnumMap<Direction> ROTATE_TORCH;
    /** Hopper rotation order (excludes UP). */
    public static final OrderedEnumMap<Direction> ROTATE_HOPPER;

    static {
        Direction e = Direction.EAST, w = Direction.WEST;
        Direction u = Direction.UP,   d = Direction.DOWN;
        Direction n = Direction.NORTH, s = Direction.SOUTH;
        ROTATE_HORIZONTAL = new OrderedEnumMap<>(Direction.class, e, s, w, n);
        ROTATE_FACING     = new OrderedEnumMap<>(Direction.class, e, s, d, w, n, u);
        ROTATE_TORCH      = new OrderedEnumMap<>(Direction.class, e, s, w, n, u);
        ROTATE_HOPPER     = new OrderedEnumMap<>(Direction.class, e, s, w, n, d);
    }
}
