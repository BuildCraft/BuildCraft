package buildcraft.core;

import buildcraft.core.utils.StringUtils;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockBuildCraft extends ItemBlock {

	public ItemBlockBuildCraft(Block b) {
		super(b);
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}

	@Override
	public String getItemStackDisplayName(ItemStack itemstack) {
		return StringUtils.localize(getUnlocalizedName(itemstack));
	}
}
