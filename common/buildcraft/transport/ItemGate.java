package buildcraft.transport;

import java.util.List;

import cpw.mods.fml.common.Side;
import cpw.mods.fml.common.asm.SideOnly;

import buildcraft.core.ItemBuildCraft;

import net.minecraft.src.CreativeTabs;
import net.minecraft.src.ItemStack;

public class ItemGate extends ItemBuildCraft {

	private int series;

	public ItemGate(int i, int series) {
		super(i);

		this.series = series;

		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@SuppressWarnings({ "all" })
	// @Override (client only)
	public int getIconFromDamage(int i) {
		int n = 0;
		if (series > 0)
			n = 3;
		else
			n = 2;

		switch (i) {
		case 0:
			return n * 16 + 6;
		case 1:
			return n * 16 + 7;
		case 2:
			return n * 16 + 8;
		case 3:
			return n * 16 + 9;
		case 4:
			return n * 16 + 10;
		case 5:
			return n * 16 + 11;
		default:
			return n * 16 + 12;
		}
	}

	@Override
	public String getItemNameIS(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getItemName()).append(".").append(itemstack.getItemDamage()).toString();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public CreativeTabs getCreativeTab() {
		return CreativeTabs.tabMisc;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List itemList) {
		super.getSubItems(par1, par2CreativeTabs, itemList);
		itemList.add(new ItemStack(this, 1, 0));
		itemList.add(new ItemStack(this, 1, 1));
		itemList.add(new ItemStack(this, 1, 2));
		itemList.add(new ItemStack(this, 1, 3));
		itemList.add(new ItemStack(this, 1, 4));
		itemList.add(new ItemStack(this, 1, 5));
		itemList.add(new ItemStack(this, 1, 6));
	}
}
