package buildcraft.api.gates;

import net.minecraft.util.Icon;
import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IAction {

	int getId();

    @SideOnly(Side.CLIENT)
	Icon getTexture();
    
    @SideOnly(Side.CLIENT)
    IIconProvider getIconProvider();
    
	boolean hasParameter();

	String getDescription();

}
