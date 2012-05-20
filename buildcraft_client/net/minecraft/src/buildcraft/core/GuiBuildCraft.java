package net.minecraft.src.buildcraft.core;

import net.minecraft.src.Container;
import net.minecraft.src.GuiContainer;

public abstract class GuiBuildCraft extends GuiContainer {

	public GuiBuildCraft(Container par1Container) {
		super(par1Container);
	}

	protected int getCenteredOffset(String string) {
		return getCenteredOffset(string, xSize);
	}
	protected int getCenteredOffset(String string, int xWidth) {
		return (xWidth - fontRenderer.getStringWidth(string)) / 2;
	}
	

}
