package buildcraft.core;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;

public class ItemRedstoneChipset extends ItemBuildCraft {

	public ItemRedstoneChipset(int i) {
		super(i);

		setHasSubtypes(true);
		setMaxDamage(0);
		setCreativeTab(CreativeTabs.tabRedstone);
	}

	@SuppressWarnings({ "all" })
	@Override
	public int getIconFromDamage(int i) {
		switch (i) {
		case 0:
			return 6 * 16 + 0;
		case 1:
			return 6 * 16 + 1;
		case 2:
			return 6 * 16 + 2;
		case 3:
			return 6 * 16 + 3;
		default:
			return 6 * 16 + 4;
		}
	}

	@Override
	public String getItemNameIS(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getItemName()).append(".").append(itemstack.getItemDamage()).toString();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List itemList) {
		for (int i = 0; i < 5; i++) {
			itemList.add(new ItemStack(this, 1, i));
		}
	}
}
