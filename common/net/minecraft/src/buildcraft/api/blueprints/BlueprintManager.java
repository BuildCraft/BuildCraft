package net.minecraft.src.buildcraft.api.blueprints;

import net.minecraft.src.Block;
import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;

public class BlueprintManager {

	public static BptBlock[] blockBptProps = new BptBlock[Block.blocksList.length];

	public static ItemSignature getItemSignature(Item item) {
		ItemSignature sig = new ItemSignature();
	
		if (item.shiftedIndex >= Block.blocksList.length + BuildCraftAPI.LAST_ORIGINAL_ITEM) {
			sig.itemClassName = item.getClass().getSimpleName();
		}
	
		sig.itemName = item.getItemNameIS(new ItemStack(item));
	
		return sig;
	}

	public static BlockSignature getBlockSignature(Block block) {
		return BlueprintManager.blockBptProps[0].getSignature(block);
	}

}
