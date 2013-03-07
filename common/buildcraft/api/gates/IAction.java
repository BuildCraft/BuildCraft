package buildcraft.api.gates;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.util.Icon;

public interface IAction {

	int getId();

    @SideOnly(Side.CLIENT)
	Icon getTexture();

	boolean hasParameter();

	String getDescription();

}
