package buildcraft.core;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.List;
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

	@SuppressWarnings({"all"})
	@Override
	public Icon getIconFromDamage(int i) {
		return i < icons.length ? icons[i] : null;
	}

	@Override
	public String getUnlocalizedName(ItemStack itemstack) {
		return (new StringBuilder()).append(super.getUnlocalizedName()).append(".").append(itemstack.getItemDamage()).toString();
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int par1, CreativeTabs par2CreativeTabs, List itemList) {
		for (int i = 0; i < 5; i++) {
			itemList.add(new ItemStack(this, 1, i));
		}
	}
	private static String[] chipsetNames = {"redstone_red", "redstone_iron", "redstone_gold", "redstone_diamond", "redstone_pulsating"};

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		icons = new Icon[chipsetNames.length];
		int i = 0;
		for (String csName : chipsetNames) {
			icons[i++] = par1IconRegister.registerIcon("buildcraft:" + csName + "_chipset");
		}
	}

	public void registerItemStacks() {
		for (int i = 0; i < 5; i++) {
			GameRegistry.registerCustomItemStack(chipsetNames[i] + "_chipset", new ItemStack(this, 1, i));
		}
	}
}
