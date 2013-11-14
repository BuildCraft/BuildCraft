package buildcraft.core.gui.buttons;

import buildcraft.core.gui.tooltips.ToolTip;

/**
 * 
 * @author CovertJaguar <railcraft.wikispaces.com>
 */
public interface IMultiButtonState {

	public String getLabel();

	public String name();

	public IButtonTextureSet getTextureSet();

	public ToolTip getToolTip();
}
