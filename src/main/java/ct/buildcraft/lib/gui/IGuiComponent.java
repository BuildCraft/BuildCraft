package ct.buildcraft.lib.gui;

import com.mojang.blaze3d.vertex.PoseStack;

public abstract class IGuiComponent {
	public int posX = 0;
	public int posY = 0;
	public boolean active = true;
	public abstract void render(PoseStack pose, float partialTick, int mouseX, int mouseY);
	
	public abstract void onMouseClick(double x, double y, int type);
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean b) {
		active = b;
	}
}
