package buildcraft.builders;

import java.util.Iterator;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBush;
import net.minecraft.block.IGrowable;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;

import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicFluid;
import buildcraft.core.blueprints.SchematicRegistry;
import buildcraft.core.builders.schematics.SchematicBlockFloored;
import buildcraft.core.builders.schematics.SchematicTileCreative;

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
							// All tiles are registered as creative only.
							// This is helpful for example for server admins.
							SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicTileCreative.class);
							continue;
						}

						try {
							if (block instanceof IFluidBlock) {
								IFluidBlock fblock = (IFluidBlock) block;
								if (fblock.getFluid() != null) {
									SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicFluid.class, new FluidStack(fblock.getFluid(), 1000));
								}
							} else {
								if (block instanceof BlockBush || block instanceof IPlantable || block instanceof IGrowable) {
									SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicBlockFloored.class);
								} else {
									SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicBlock.class);
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
