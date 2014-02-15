/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class ItemBptTemplate extends ItemBptBase {
	private IIcon usedTemplate;
	public ItemBptTemplate() {
		super();
	}

	@Override
	public IIcon getIconFromDamage(int i) {
		if (i == 0)
			return itemIcon;
		else
			return usedTemplate;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister)
	{
		itemIcon = par1IconRegister.registerIcon("buildcraft:template_clean");
		usedTemplate = par1IconRegister.registerIcon("buildcraft:template_used");
	}
}
