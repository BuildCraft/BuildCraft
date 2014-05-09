/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.core.utils.NBTUtils;

public class ItemBlueprintTemplate extends ItemBlueprint {
	private IIcon cleanTemplate;
	private IIcon usedTemplate;

	public ItemBlueprintTemplate() {
		super();
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		if (!NBTUtils.getItemData(stack).hasKey("name")) {
			itemIcon = cleanTemplate;
		} else {
			itemIcon = usedTemplate;
		}
		
		return itemIcon;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		cleanTemplate = par1IconRegister.registerIcon("buildcraft:template_clean");
		usedTemplate = par1IconRegister.registerIcon("buildcraft:template_used");
	}
}
