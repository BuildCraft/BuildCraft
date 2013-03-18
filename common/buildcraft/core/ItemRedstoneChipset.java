package buildcraft.core;

import java.util.List;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;

public class ItemRedstoneChipset extends ItemBuildCraft {

    @SideOnly(Side.CLIENT)
    private Icon[] icons;

	public ItemRedstoneChipset(int i) {
		super(i);

		setHasSubtypes(true);
		setMaxDamage(0);
	}

	@SuppressWarnings({ "all" })
	@Override
	public Icon getIconFromDamage(int i) {
	    return i < icons.length ? icons[i] : null;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getUnlocalizedName()).append(".").append(itemstack.getItemDamage()).toString();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List itemList) {
		for (int i = 0; i < 5; i++) {
			itemList.add(new ItemStack(this, 1, i));
		}
	}

	private static String[] chipsetNames = { "redstone_red", "redstone_iron", "redstone_gold", "redstone_diamond", "redstone_pulsating" };
	@Override
	@SideOnly(Side.CLIENT)
	public void func_94581_a(IconRegister par1IconRegister)
	{
	    icons = new Icon[chipsetNames.length];
	    int i = 0;
	    for (String csName : chipsetNames) {
	        icons[i++] = par1IconRegister.func_94245_a("buildcraft:"+csName+"_chipset");
	    }
	}
}
