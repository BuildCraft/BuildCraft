package buildcraft.builders;

import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicFluid;
import buildcraft.api.blueprints.SchematicRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public final class HeuristicBlockDetection {
	private HeuristicBlockDetection() {
		
	}
	
	public static void start() {
		// Register fluids
		for (Fluid f : FluidRegistry.getRegisteredFluids().values()) {
			SchematicRegistry.registerSchematicBlock(f.getBlock(), SchematicFluid.class, new FluidStack(f, 1000));
		}
		
		// Register blocks
		for (Object o : Block.blockRegistry.getKeys()) {
			Block block = (Block) Block.blockRegistry.getObject(o);
			if (block == null) {
				continue;
			}
			
			for (int meta = 0; meta < 16; meta++) {
				if (!SchematicRegistry.isSupported(block, meta)) {
					// Stops dupes with (for instance) ore blocks
					try {
						if (block.getItemDropped(meta, null, 0) != Item.getItemFromBlock(block)) {
							continue;
						}
					} catch(NullPointerException e) {
						// The "null" for Random in getItemDropped stops blocks
						// depending on an RNG for deciding the dropped item
						// from being autodetected.
					}
					if (block.hasTileEntity(meta)) {
						continue;
					}
				
					SchematicRegistry.registerSchematicBlock(block, meta, SchematicBlock.class);
				}
			}
		}
	}
}
