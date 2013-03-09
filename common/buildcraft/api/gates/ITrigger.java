package buildcraft.api.gates;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Icon;

public interface ITrigger {

	public abstract int getId();

	/**
	 * Return the texture file for this trigger icon
	 */
    @SideOnly(Side.CLIENT)
	public abstract Icon getTextureIcon();

	/**
	 * Return true if this trigger can accept parameters
	 */
	public abstract boolean hasParameter();

	/**
	 * Return the trigger description in the UI
	 */
	public abstract String getDescription();

	/**
	 * Return true if the tile given in parameter activates the trigger, given the parameters.
	 */
	public abstract boolean isTriggerActive(TileEntity tile, ITriggerParameter parameter);

	/**
	 * Create parameters for the trigger. As for now, there is only one kind of trigger parameter available so this subprogram is final.
	 */
	public abstract ITriggerParameter createParameter();

}
