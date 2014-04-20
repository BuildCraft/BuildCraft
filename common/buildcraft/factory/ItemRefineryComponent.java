package buildcraft.factory;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemRefineryComponent extends ItemBlock {

	public ItemRefineryComponent(Block block) {
		super(block);
	}

	@Override
	public int getMetadata(int meta) {
		return meta;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return super.getUnlocalizedName(stack) + "." + BlockRefineryComponent.NAMES[stack.getItemDamage()];
	}
}
