/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBucket;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.utils.IModelRegister;
import buildcraft.core.utils.ModelHelper;

public class ItemBucketBuildcraft extends ItemBucket implements IModelRegister {

	private String iconName;

	public ItemBucketBuildcraft(Block block) {
		this(block, CreativeTabBuildCraft.ITEMS);
	}

	public ItemBucketBuildcraft(Block block, CreativeTabBuildCraft creativeTab) {
		super(block);
		setContainerItem(Items.bucket);
		setCreativeTab(creativeTab.get());
	}

	@Override
	public Item setUnlocalizedName(String par1Str) {
		iconName = par1Str;
		return super.setUnlocalizedName(par1Str);
	}

	@Override
	public void registerModels() {
		ModelHelper.registerItemModel(this, 0, "");
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:" + iconName);
	}*/
}
