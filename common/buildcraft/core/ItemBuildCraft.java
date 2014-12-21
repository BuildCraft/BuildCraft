/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemBuildCraft extends Item {

	private String iconName;
	private boolean passSneakClick = false;

	public ItemBuildCraft() {
		this(CreativeTabBuildCraft.ITEMS);
	}

	public ItemBuildCraft(CreativeTabBuildCraft creativeTab) {
		super();

		setCreativeTab(creativeTab.get());
	}

	@Override
	public Item setUnlocalizedName(String par1Str) {
		iconName = par1Str;
		return super.setUnlocalizedName(par1Str);
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		this.itemIcon = par1IconRegister.registerIcon("buildcraft:" + iconName);
	}*/

	public Item setPassSneakClick(boolean passClick) {
		this.passSneakClick = passClick;
		return this;
	}

	public String getModelName(int metadata) {
		return iconName;
	}

	public String getModelSuffix(int metadata) {
		return "";
	}

	@Override
	public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
		return passSneakClick;
	}
}
