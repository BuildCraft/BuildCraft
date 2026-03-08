/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.gate;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;

import java.util.Locale;

public enum EnumGateMaterial {
    CLAY_BRICK(Blocks.BRICKS, 1, false),
    IRON(Blocks.IRON_BLOCK, 2, true),
    NETHER_BRICK(Blocks.NETHER_BRICKS, 4, true),
    GOLD(Blocks.GOLD_BLOCK, 8, true);

    public static final EnumGateMaterial[] VALUES = values();

    public final Block block;
    public final int numSlots;
    public final boolean canBeModified;
    public final String tag = name().toLowerCase(Locale.ROOT);

    EnumGateMaterial(Block block, int numSlots, boolean canBeModified) {
        this.block = block;
        this.numSlots = numSlots;
        this.canBeModified = canBeModified;
    }

    public static EnumGateMaterial getByOrdinal(int ord) {
        if (ord < 0 || ord >= VALUES.length) {
            return EnumGateMaterial.CLAY_BRICK;
        }
        return VALUES[ord];
    }
}
