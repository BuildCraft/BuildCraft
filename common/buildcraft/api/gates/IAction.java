package buildcraft.api.gates;

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Icon;

public interface IAction {

	int getId();

    @SideOnly(Side.CLIENT)
	Icon getTexture();
    
    @SideOnly(Side.CLIENT)
    IIconProvider getIconProvider();
    
	boolean hasParameter();

	String getDescription();

}
