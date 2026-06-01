/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.state.property.Property;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.DyeColor;
import net.minecraft.util.ActionResult;

import buildcraft.api.blocks.CustomPaintHelper;
import buildcraft.api.blocks.ICustomPaintHandler;

public class VanillaPaintHandlers {

    public static void fmlInit() {
        registerDoubleTypedHandler(Blocks.GLASS, Blocks.STAINED_GLASS, BlockStainedGlass.COLOR);
        registerDoubleTypedHandler(Blocks.GLASS_PANE, Blocks.STAINED_GLASS_PANE, BlockStainedGlassPane.COLOR);
        registerDoubleTypedHandler(Blocks.HARDENED_CLAY, Blocks.STAINED_HARDENED_CLAY, BlockColored.COLOR);
    }

    private static void registerDoubleTypedHandler(Block clear, Block dyed, Property<DyeColor> colourProp) {
        ICustomPaintHandler handler = createDoubleTypedPainter(clear, dyed, colourProp);
        CustomPaintHelper.INSTANCE.registerHandler(clear, handler);
        CustomPaintHelper.INSTANCE.registerHandler(dyed, handler);
    }

    public static ICustomPaintHandler createDoubleTypedPainter(Block clear, Block dyed, Property<DyeColor> colourProp) {
        return (world, pos, state, hitPos, hitSide, to) -> {
            if (state.getBlock() == clear) {
                // We are currently clear
                if (to == null) {
                    return ActionResult.FAIL;
                }
                BlockState painted = dyed.getDefaultState().with(colourProp, to);
                world.setBlockState(pos, painted);
                return ActionResult.SUCCESS;
            } else if (state.getBlock() == dyed) {
                if (to == state.get(colourProp)) {
                    return ActionResult.FAIL;
                }
                if (to == null) {
                    state = clear.getDefaultState();
                } else {
                    state = state.with(colourProp, to);
                }
                world.setBlockState(pos, state);
                return ActionResult.SUCCESS;
            }
            return ActionResult.PASS;
        };
    }
}
