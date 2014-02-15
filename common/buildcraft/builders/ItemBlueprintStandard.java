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

public class ItemBlueprintStandard extends ItemBlueprint {

	private IIcon cleanBlueprint;
	private IIcon usedBlueprint;

	public ItemBlueprintStandard() {
		super();
	}

	@Override
	public IIcon getIconFromDamage(int damage) {
		if (damage == 0)
			return cleanBlueprint;
		else
			return usedBlueprint;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister) {
		cleanBlueprint = par1IconRegister.registerIcon("buildcraft:blueprint_clean");
		usedBlueprint = par1IconRegister.registerIcon("buildcraft:blueprint_used");
	}
}
