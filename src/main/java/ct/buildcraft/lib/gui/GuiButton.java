package ct.buildcraft.lib.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.GuiComponent;
import net.minecraft.resources.ResourceLocation;

public class GuiButton extends GuiComponent{
	
	private int posX;
	private int posY;
	private int state;
	private int states_num;
	private boolean shouldRightReduce;
	private boolean active = true;
	private ResourceLocation deafultBg;
	private ResourceLocation holdingBg;

	public GuiButton(int x, int y, boolean rightReduce, int s) {
		posX = x;
		posY = y;
		states_num = s-1;
		shouldRightReduce = rightReduce;
	}
	
	public void render(PoseStack pose, float partialTick, int mouseX, int mouseY) {
		
	}
	
	protected boolean isHolding(double x,double y) {
		return false;
	}
	
	public void onMouseClick(double x, double y, int type) {
		if(isHolding(x, type)) {
			if(type == 1&&shouldRightReduce) 
				state = state == 0 ? states_num : state-1;
			else {
				state++;
				(state)%=states_num;
			}
		}
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setActive(boolean p) {
		active = p;
	}
	
	public void setState(int i) {
		state = i;
	}
	
	public int getState() {
		return state;
	}
	
}
