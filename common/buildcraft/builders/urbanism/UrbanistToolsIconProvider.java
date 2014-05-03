/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.urbanism;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.core.IIconProvider;

public final class UrbanistToolsIconProvider implements IIconProvider {

	public static UrbanistToolsIconProvider INSTANCE = new UrbanistToolsIconProvider();
	public static final int Tool_Block_Place = 0;
	public static final int Tool_Block_Erase = 1;
	public static final int Tool_Area = 2;
	public static final int Tool_Path = 3;
	public static final int Tool_Filler = 4;
	public static final int Tool_Blueprint = 5;

	public static final int MAX = 6;
	@SideOnly(Side.CLIENT)
	private final IIcon[] icons = new IIcon[MAX];

	private UrbanistToolsIconProvider() {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int iconIndex) {
		return icons[iconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icons[UrbanistToolsIconProvider.Tool_Block_Place] = iconRegister.registerIcon("buildcraft:icons/urbanist_block");
		icons[UrbanistToolsIconProvider.Tool_Block_Erase] = iconRegister.registerIcon("buildcraft:icons/urbanist_erase");
		icons[UrbanistToolsIconProvider.Tool_Area] = iconRegister.registerIcon("buildcraft:icons/urbanist_area");
		icons[UrbanistToolsIconProvider.Tool_Path] = iconRegister.registerIcon("buildcraft:icons/urbanist_path");
		icons[UrbanistToolsIconProvider.Tool_Filler] = iconRegister.registerIcon("buildcraft:icons/urbanist_filler");
		icons[UrbanistToolsIconProvider.Tool_Blueprint] = iconRegister.registerIcon("buildcraft:icons/urbanist_blueprint");
	}
}
