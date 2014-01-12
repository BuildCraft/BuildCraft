package buildcraft.core;

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public class CoreIconProvider implements IIconProvider {

	public static int ENERGY 	= 0;
	
	public static int MAX 		= 1;

	private IIcon[] _icons;
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int iconIndex) {
		return _icons[iconIndex];
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		_icons = new IIcon[MAX];
		
		_icons[ENERGY] = iconRegister.registerIcon("buildcraft:icons/energy");
		
	}

}
