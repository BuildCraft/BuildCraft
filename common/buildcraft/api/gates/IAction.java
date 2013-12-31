package buildcraft.api.gates;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.util.Icon;

public interface IAction {

	String getUniqueTag();

	@SideOnly(Side.CLIENT)
	Icon getIcon();

	@SideOnly(Side.CLIENT)
	void registerIcons(IconRegister iconRegister);

	boolean hasParameter();

	String getDescription();
}
