package ct.buildcraft.lib.gui.component;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;

public class ProgressComponent extends AbstractComponent{
	
	
	protected final int u0;
	protected final int v0;
	
	protected final int type;
	protected final ResourceLocation bg;
	
	public ProgressComponent(int x, int y, int xs, int ys, int u0, int v0, byte type, ResourceLocation bg) {
		super(x, y, xs, ys);
		this.u0 = u0;
		this.v0 = v0;
		this.type = type;
		this.bg = bg;
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick, AbstractContainerScreen<?> screen) {
		double pro =  (getProgress(partialTick));
		RenderSystem.setShaderTexture(0, bg);
//		screen.blit(pose, screen.getGuiLeft()+x, screen.getGuiTop()+y, u0, v0, (xs*pro), ys);//L_to_r
//		screen.blit(pose, (int)(screen.getGuiLeft()+x+xs*(1-pro)), screen.getGuiTop()+y, (int)(u0+xs*(1-pro)), v0, (int) (xs*pro), ys);//r2l
//		screen.blit(pose, screen.getGuiLeft()+x, screen.getGuiTop()+y, u0, v0, (xs), (int) (ys*pro));//d2u
//		screen.blit(pose, screen.getGuiLeft()+x, (int) (screen.getGuiTop()+y+ys*(1-pro)), u0, (int)(v0+ys*(1-pro)), (xs), (int) (ys*pro));//u2d
		screen.blit(pose,
				(int)(screen.getGuiLeft()+x+xs*(1-pro)*((type&R_TO_L)>>1)),
				(int) (screen.getGuiTop()+y+ys*(1-pro)*((type&U_TO_D)>>2)),
				(int)(u0+xs*(1-pro)*((type&R_TO_L)>>1)),
				(int)(v0+ys*(1-pro)*((type&U_TO_D)>>2)),
				(int) (xs*(pro+(1-pro)*(((3-type)&0x80)>>7))), 
				(int) (ys*(pro+(1-pro)*(((type-3)&0x80)>>7))));
	}
	
	
	@Override
	public int getNeedDataSize() {
		return 2;
	}

	protected double getProgress(float partialTicks) {
		if (partialTicks <= 0) {
			return data.get(offset)/1000000;
		} else if (partialTicks >= 1) {
			return data.get(offset + 1)/1000000;
		} else {
			double a = data.get(0) * (1 - partialTicks) /1000000;
			double b = data.get(0) * partialTicks / 1000000;
			return a + b;
		}
	}

}
