package buildcraft.builders;

import java.util.BitSet;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.api.blueprints.SchematicBlock;
import buildcraft.api.blueprints.SchematicFluid;
import buildcraft.builders.schematics.SchematicBlockCreative;
import buildcraft.builders.schematics.SchematicTileCreative;
import buildcraft.core.blueprints.SchematicRegistry;

public final class HeuristicBlockDetection {
	
	private HeuristicBlockDetection() {
		
	}
	
	private static BitSet craftableBlockList = new BitSet(65536);
	
	public static void start() {
		// Initialize craftableBlockList
		/* for (Object or : CraftingManager.getInstance().getRecipeList()) {
			if (or instanceof IRecipe) {
				IRecipe recipe = ((IRecipe) or);
				if (recipe.getRecipeOutput() != null && recipe.getRecipeOutput().getItem() != null &&
						recipe.getRecipeOutput().getItem() instanceof ItemBlock) {
					int pos = recipe.getRecipeOutput().getItemDamage() & 15;
					pos |= Block.getIdFromBlock(Block.getBlockFromItem(recipe.getRecipeOutput().getItem())) << 4;
					if (pos >= 0 && pos < 65536) {
						craftableBlockList.set(pos);
					}
				}
			}
		} */
		
		// Register fluids
		for (Fluid f : FluidRegistry.getRegisteredFluids().values()) {
			SchematicRegistry.INSTANCE.registerSchematicBlock(f.getBlock(), SchematicFluid.class, new FluidStack(f, 1000));
		}
		
		// Register blocks
		for (Object o : Block.blockRegistry.getKeys()) {
			Block block = (Block) Block.blockRegistry.getObject(o);
			if (block == null) {
				continue;
			}
			
			for (int meta = 0; meta < 16; meta++) {
				if (!SchematicRegistry.INSTANCE.isSupported(block, meta)) {
					if (block.hasTileEntity(meta)) {
						// All tiles are registered as creative only.
						// This is helpful for example for server admins.
						SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicTileCreative.class);
						continue;
					}
					
					boolean creativeOnly = false;
					
					try {
						if (creativeOnly) {
							SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicBlockCreative.class);
						} else {
							SchematicRegistry.INSTANCE.registerSchematicBlock(block, meta, SchematicBlock.class);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	private static boolean canCraft(Block block, int meta) {
		int pos = Block.getIdFromBlock(block) << 4 | meta;
		return craftableBlockList.get(pos);
	}
}
