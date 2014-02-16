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

public enum LockButtonState implements IMultiButtonState {

	UNLOCKED(new ButtonTextureSet(224, 0, 16, 16)), LOCKED(new ButtonTextureSet(240, 0, 16, 16));
	public static final LockButtonState[] VALUES = values();
	private final IButtonTextureSet texture;

	private LockButtonState(IButtonTextureSet texture) {
		this.texture = texture;
	}

	@Override
	public String getLabel() {
		return "";
	}

	@Override
	public IButtonTextureSet getTextureSet() {
		return texture;
	}

	@Override
	public ToolTip getToolTip() {
		return null;
	}
}
