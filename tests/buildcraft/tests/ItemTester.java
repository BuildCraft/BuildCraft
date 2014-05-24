/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.tests;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

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
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:test");
	}

	@Override
	public boolean onItemUse(ItemStack par1ItemStack, EntityPlayer entityplayer, World world, int x,
			int y, int z, int i, float par8, float par9, float par10) {

		if (!world.isRemote) {
			entityplayer.openGui(BuildCraftTests.instance, GuiTestIds.TESTER_ID, world, x, y, z);
		}

		return true;
	}

}
