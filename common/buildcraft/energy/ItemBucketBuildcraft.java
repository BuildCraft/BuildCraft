/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.StringUtils;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;

public class ItemBucketBuildcraft extends ItemBucket {

	private String iconName;

	public ItemBucketBuildcraft(Block block) {
		super(block);
		setCreativeTab(CreativeTabBuildCraft.MACHINES.get());
		setContainerItem(Items.bucket);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtils.localize(getUnlocalizedName(itemstack));
	}

	@Override
	public Item setBlockName(String par1Str) {
		iconName = par1Str;
		return super.setBlockName(par1Str);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:" + iconName);
	}
}
