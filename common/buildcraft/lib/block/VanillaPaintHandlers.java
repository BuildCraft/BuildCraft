/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.block;

import buildcraft.api.blocks.CustomPaintHelper;
import buildcraft.api.blocks.ICustomPaintHandler;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VanillaPaintHandlers {

    public static void fmlInit() {
//        registerDoubleTypedHandler(Blocks.GLASS, Blocks.STAINED_GLASS, StainedGlassBlock.COLOR);
//        registerDoubleTypedHandler(Blocks.GLASS_PANE, Blocks.STAINED_GLASS_PANE, BlockStainedGlassPane.COLOR);
//        registerDoubleTypedHandler(Blocks.TERRACOTTA, Blocks.STAINED_HARDENED_CLAY, BlockColored.COLOR);
        registerDoubleTypedHandler(Blocks.GLASS, createColourBlockMap("minecraft", "_terracotta"));
        registerDoubleTypedHandler(Blocks.GLASS_PANE, createColourBlockMap("minecraft", "_stained_glass"));
        registerDoubleTypedHandler(Blocks.TERRACOTTA, createColourBlockMap("minecraft", "_stained_glass_pane"));
        registerDoubleTypedHandler(Blocks.WHITE_WOOL, createColourBlockMap("minecraft", "_wool"));
        registerDoubleTypedHandler(Blocks.WHITE_CARPET, createColourBlockMap("minecraft", "_carpet"));
        registerDoubleTypedHandler(Blocks.WHITE_CONCRETE, createColourBlockMap("minecraft", "_concrete"));
        registerDoubleTypedHandler(Blocks.WHITE_CONCRETE_POWDER, createColourBlockMap("minecraft", "_concrete_powder"));
    }

    // private static void registerDoubleTypedHandler(Block clear, Block dyed, Property<DyeColor> colourProp)
    private static void registerDoubleTypedHandler(Block clear, Map<DyeColor, ? extends Block> dyed) {
//        ICustomPaintHandler handler = createDoubleTypedPainter(clear, dyed, colourProp);
        ICustomPaintHandler handler = createDoubleTypedPainter(clear, dyed);
        CustomPaintHelper.INSTANCE.registerHandler(clear, handler);
//        CustomPaintHelper.INSTANCE.registerHandler(dyed, handler);
        dyed.values().forEach(b -> CustomPaintHelper.INSTANCE.registerHandler(b, handler));
    }

    // public static ICustomPaintHandler createDoubleTypedPainter(Block clear, Block dyed, Property<DyeColor> colourProp)
    public static ICustomPaintHandler createDoubleTypedPainter(Block clear, Map<DyeColor, ? extends Block> dyed) {
        return (world, pos, state, hitPos, hitSide, to) ->
        {
            // clear -> ?
            if (state.getBlock() == clear) {
                // We are currently clear
                if (to == null) {
                    return InteractionResult.FAIL;
                }
//                BlockState painted = dyed.defaultBlockState().setValue(colourProp, to);
                Block painted = dyed.get(to);
//                world.setBlock(pos, painted, Block.UPDATE_ALL);
//                return InteractionResult.SUCCESS;
                if (painted != null) {
                    world.setBlock(pos, painted.defaultBlockState(), Block.UPDATE_ALL);
                    return InteractionResult.SUCCESS;
                } else {
                    return InteractionResult.FAIL;
                }
            }
            // dyed -> ?
//            else if (state.getBlock() == dyed)
            else if (dyed.containsValue(state.getBlock())) {
                // the same colour
//                if (to == state.getValue(colourProp))
                if (dyed.get(to) == state.getBlock()) {
                    return InteractionResult.FAIL;
                }
                if (to == null) {
                    // to colorless
                    state = clear.defaultBlockState();
                } else {
                    // to another colour
//                    state = state.setValue(colourProp, to);
                    Block b = dyed.get(to);
                    if (b != null) {
                        state = b.defaultBlockState();
                    } else {
                        return InteractionResult.FAIL;
                    }
                }
                world.setBlock(pos, state, Block.UPDATE_ALL);
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        };
    }

    // Calen
    public static Map<DyeColor, ? extends Block> createColourBlockMap(String namespace, String pathSuffix) {
        Map<DyeColor, Block> ret = new HashMap<>();
        Arrays.stream(DyeColor.values()).toList().forEach(
                c ->
                {
                    ResourceLocation blockName = new ResourceLocation(namespace, c.getSerializedName() + pathSuffix);
                    Block block = ForgeRegistries.BLOCKS.getValue(blockName);
                    if (block == null || block == Blocks.AIR)
                        throw new IllegalStateException("Unknown block: " + blockName.toString());
                    ret.put(c, block);
                }
        );
        return ret;
    }
}
