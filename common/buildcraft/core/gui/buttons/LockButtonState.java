package buildcraft.core.gui.buttons;

import buildcraft.core.gui.tooltips.ToolTip;

/**
 * 
 * @author CovertJaguar <http://www.railcraft.info/>
 */
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
