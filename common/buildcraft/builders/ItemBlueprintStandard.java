/**
 * Copyright (c) SpaceToad, 2011-2012 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public class ItemBlueprintStandard extends ItemBlueprint {

	private Icon cleanBlueprint;
	private Icon usedBlueprint;

	public ItemBlueprintStandard(int i) {
		super(i);
	}

	@Override
	public Icon getIconFromDamage(int damage) {
		if (damage == 0)
			return cleanBlueprint;
		else
			return usedBlueprint;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister par1IconRegister) {
		cleanBlueprint = par1IconRegister.registerIcon("buildcraft:blueprint_clean");
		usedBlueprint = par1IconRegister.registerIcon("buildcraft:blueprint_used");
	}
}
