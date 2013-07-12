package buildcraft.core;

import buildcraft.core.utils.StringUtils;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class ItemBlockBuildCraft extends ItemBlock {

	public ItemBlockBuildCraft(int id) {
		super(id);
	}

	@Override
	public int getMetadata(int i) {
		return i;
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtils.localize(getUnlocalizedName(itemstack));
	}
}
