/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import buildcraft.api.core.IIconProvider;

public class WireIconProvider implements IIconProvider {

	public static final int Texture_Red_Dark = 0;
	public static final int Texture_Red_Lit = 1;
	public static final int Texture_Blue_Dark = 2;
	public static final int Texture_Blue_Lit = 3;
	public static final int Texture_Green_Dark = 4;
	public static final int Texture_Green_Lit = 5;
	public static final int Texture_Yellow_Dark = 6;
	public static final int Texture_Yellow_Lit = 7;

	public static final int MAX = 8;

	@SideOnly(Side.CLIENT)
	private IIcon[] icons;

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int pipeIconIndex) {
		return icons[pipeIconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		icons = new IIcon[MAX];

		icons[WireIconProvider.Texture_Red_Dark] = iconRegister.registerIcon("buildcraftcore:misc/texture_red_dark");
		icons[WireIconProvider.Texture_Red_Lit] = iconRegister.registerIcon("buildcraftcore:misc/texture_red_lit");
		icons[WireIconProvider.Texture_Blue_Dark] = iconRegister.registerIcon("buildcraftcore:misc/texture_blue_dark");
		icons[WireIconProvider.Texture_Blue_Lit] = iconRegister.registerIcon("buildcraftcore:misc/texture_blue_lit");
		icons[WireIconProvider.Texture_Green_Dark] = iconRegister.registerIcon("buildcraftcore:misc/texture_green_dark");
		icons[WireIconProvider.Texture_Green_Lit] = iconRegister.registerIcon("buildcraftcore:misc/texture_green_lit");
		icons[WireIconProvider.Texture_Yellow_Dark] = iconRegister.registerIcon("buildcraftcore:misc/texture_yellow_dark");
		icons[WireIconProvider.Texture_Yellow_Lit] = iconRegister.registerIcon("buildcraftcore:misc/texture_yellow_lit");

	}

}
