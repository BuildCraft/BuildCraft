/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.gui.buttons;

import buildcraft.core.gui.tooltips.ToolTip;

public interface IMultiButtonState {

	public String getLabel();

	public String name();

	public IButtonTextureSet getTextureSet();

	public ToolTip getToolTip();
}
