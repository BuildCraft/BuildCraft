package buildcraft.builders;

import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBasePressurePlate;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockDynamicLiquid;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;

import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicFluid;
import buildcraft.builders.schematics.SchematicStairs;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.schematics.SchematicBlockFloored;
import buildcraft.core.builders.schematics.SchematicIgnore;
import buildcraft.core.builders.schematics.SchematicTileCreative;
import buildcraft.core.builders.schematics.SchematicWallSide;
import buildcraft.core.lib.utils.FluidUtils;

public final class HeuristicBlockDetection {
	private HeuristicBlockDetection() {

	}

	public static void start() {
		Iterator i = Block.blockRegistry.iterator();
		while (i.hasNext()) {
			Block block = (Block) i.next();
			if (block == null || block == Blocks.air) {
				continue;
			}

			for (int meta = 0; meta < 16; meta++) {
				if (!SchematicRegistry.INSTANCE.isSupported(block, meta)) {
					try {
						if (block.hasTileEntity(meta)) {
							// All tiles not otherwise supported are registered
							// as creative only to prevent exploitation.
							SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicTileCreative.class);
							continue;
						}

						try {
							if (block instanceof BlockDynamicLiquid) {
								// Fixes #3341 - not recording flowing water
								SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicIgnore.class);
							} else {
								if (block instanceof BlockBush || block instanceof IPlantable || block instanceof IGrowable || block instanceof BlockBasePressurePlate) {
									SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicBlockFloored.class);
								} else if (block instanceof BlockLever || block instanceof BlockTorch || block instanceof BlockButton) {
									SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicWallSide.class);
								} else if (block instanceof BlockStairs) {
									SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicStairs.class);
								} else {
									FluidStack fstack = FluidUtils.getFluidStackFromBlock(block);
									if (fstack != null) {
										SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicFluid.class, fstack);
									} else {
										SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicBlock.class);
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					} catch (Exception e) {

					}
				}
			}
		}
	}
}
