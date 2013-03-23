package buildcraft.api.gates;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;
import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.ForgeDirection;

public interface ITrigger {

	public abstract int getId();

	/**
	 * Return the texture file for this trigger icon
	 */
    @SideOnly(Side.CLIENT)
	public Icon getTextureIcon();
    
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider();

	/**
	 * Return true if this trigger can accept parameters
	 */
	public boolean hasParameter();

	/**
	 * Return the trigger description in the UI
	 */
	public String getDescription();

	/**
	 * Return true if the tile given in parameter activates the trigger, given the parameters.
	 */
	public abstract boolean isTriggerActive(ForgeDirection side, TileEntity tile, ITriggerParameter parameter);

	/**
	 * Create parameters for the trigger. As for now, there is only one kind of trigger parameter available so this subprogram is final.
	 */
	public ITriggerParameter createParameter();

}
