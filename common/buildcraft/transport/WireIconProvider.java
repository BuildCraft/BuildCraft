/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;


public class WireIconProvider /*implements IIconProvider*/ {
	
	public static final int Texture_Red_Dark				=  0;
	public static final int Texture_Red_Lit					=  1;
	public static final int Texture_Blue_Dark				=  2;
	public static final int Texture_Blue_Lit				=  3;
	public static final int Texture_Green_Dark				=  4;
	public static final int Texture_Green_Lit				=  5;
	public static final int Texture_Yellow_Dark				=  6;
	public static final int Texture_Yellow_Lit				=  7;
	
	public static final int MAX								=  8;

	@SideOnly(Side.CLIENT)
	private TextureAtlasSprite[] icons;

	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getIcon(int pipeIconIndex) {
		return icons[pipeIconIndex];
	}

	@SideOnly(Side.CLIENT)
	public void registerIcons(TextureMap map) {
		icons = new TextureAtlasSprite[MAX];

		icons[WireIconProvider.Texture_Red_Dark] = map.registerSprite(new ResourceLocation("buildcraft", "texture_red_dark"));
		icons[WireIconProvider.Texture_Red_Lit] = map.registerSprite(new ResourceLocation("buildcraft", "texture_red_lit"));
		icons[WireIconProvider.Texture_Blue_Dark] = map.registerSprite(new ResourceLocation("buildcraft", "texture_blue_dark"));
		icons[WireIconProvider.Texture_Blue_Lit] = map.registerSprite(new ResourceLocation("buildcraft", "texture_blue_lit"));
		icons[WireIconProvider.Texture_Green_Dark] = map.registerSprite(new ResourceLocation("buildcraft", "texture_green_dark"));
		icons[WireIconProvider.Texture_Green_Lit] = map.registerSprite(new ResourceLocation("buildcraft", "texture_green_lit"));
		icons[WireIconProvider.Texture_Yellow_Dark] = map.registerSprite(new ResourceLocation("buildcraft", "texture_yellow_dark"));
		icons[WireIconProvider.Texture_Yellow_Lit] = map.registerSprite(new ResourceLocation("buildcraft", "texture_yellow_lit"));
	}
}
