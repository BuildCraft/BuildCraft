package buildcraft.tests;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.CreativeTabBuildCraft;

public class ItemTester extends Item {

	public ItemTester() {
		setCreativeTab(CreativeTabBuildCraft.ITEMS.get());
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:tester");
	}

}
