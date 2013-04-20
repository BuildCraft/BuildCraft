package buildcraft.core;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import buildcraft.core.utils.StringUtil;

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
		return StringUtil.localize(getUnlocalizedName(itemstack));
	}
}
