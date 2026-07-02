package ct.buildcraft.lib.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerData;

public interface ContainerComponent{
	
	public static final byte R_TO_L = 1;
	public static final byte L_TO_R = 2;
	public static final byte U_TO_D = 4;
	public static final byte D_TO_U = 8;
	
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick, AbstractContainerScreen<?> screen);
	
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTick, AbstractContainerScreen<?> screen);
	
	public default boolean onClick(double x, double y, int mouse) {return false;}
	
	public default boolean mouseRelease(double x, double y, int mouse) {return false;};
	
	public default boolean isHovering(int x, int y) {return false;}
	
	public default void renderTooltip(PoseStack pose, int x, int y) {}
	
	
	
	public default int getX() {return 0;}
		
	public default int getY() {return 0;}
	
	public default int getXsize() {return 0;}
	
	public default int getYsize() {return 0;}
	
	
	
	public default void setup(AbstractContainerScreen<?> screen, ContainerData data) {}
	
	public default void setDataoffset(int offset) {}
	
	public int getNeedDataSize();
	
	public default void onClose() {};
}
