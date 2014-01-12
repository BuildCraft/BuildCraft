package buildcraft.api.gates;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.util.IIcon;

public interface IAction {

	String getUniqueTag();

	@SideOnly(Side.CLIENT)
	IIcon getIcon();

	@SideOnly(Side.CLIENT)
	void registerIcons(IIconRegister iconRegister);

	boolean hasParameter();

	String getDescription();
}
