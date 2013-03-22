/**
 * Copyright (c) SpaceToad, 2011
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.energy;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import buildcraft.BuildCraftEnergy;
import buildcraft.core.CreativeTabBuildCraft;
import buildcraft.core.DefaultProps;
import buildcraft.core.utils.StringUtil;

public class ItemBucketOil extends ItemBucket {

	public ItemBucketOil(int i) {
		super(i, BuildCraftEnergy.oilMoving.blockID);
		setCreativeTab(CreativeTabBuildCraft.tabBuildCraft);
	}

	@Override
	public String getItemDisplayName(ItemStack itemstack) {
		return StringUtil.localize(getUnlocalizedName(itemstack));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void updateIcons(IconRegister par1IconRegister)
	{
		this.iconIndex = par1IconRegister.registerIcon("buildcraft:oil_bucket");
	}
}
