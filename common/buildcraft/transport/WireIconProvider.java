package buildcraft.transport;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import buildcraft.core.IIconProvider;

public class WireIconProvider implements IIconProvider {
	
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
	private Icon[] icons;
	
	private boolean registered = false;
	
	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int pipeIconIndex) {
		return icons[pipeIconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void RegisterIcons(IconRegister iconRegister) {
		if (registered) return;
		icons = new Icon[MAX];

		icons[WireIconProvider.Texture_Red_Dark] = iconRegister.func_94245_a("buildcraft:texture_red_dark");
		icons[WireIconProvider.Texture_Red_Lit] = iconRegister.func_94245_a("buildcraft:texture_red_lit");
		icons[WireIconProvider.Texture_Blue_Dark] = iconRegister.func_94245_a("buildcraft:texture_blue_dark");
		icons[WireIconProvider.Texture_Blue_Lit] = iconRegister.func_94245_a("buildcraft:texture_blue_lit");
		icons[WireIconProvider.Texture_Green_Dark] = iconRegister.func_94245_a("buildcraft:texture_green_dark");
		icons[WireIconProvider.Texture_Green_Lit] = iconRegister.func_94245_a("buildcraft:texture_green_lit");
		icons[WireIconProvider.Texture_Yellow_Dark] = iconRegister.func_94245_a("buildcraft:texture_yellow_dark");
		icons[WireIconProvider.Texture_Yellow_Lit] = iconRegister.func_94245_a("buildcraft:texture_yellow_lit");		

	}

}
