package ct.buildcraft.transport.client.gui;

import ct.buildcraft.transport.BCTransportSprites;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenPipeDiawood extends AbstractContainerScreen<MenuPipeDiawood>{

	private static final ResourceLocation TEXTURE_BASE = BCTransportSprites.DIAWOOD_GUI;
	private static final ResourceLocation TEXTURE_BUTTON = BCTransportSprites.DIAWOOD_BUTTON_GUI;
	
	public ScreenPipeDiawood(MenuPipeDiawood be, Inventory p_97742_, Component p_97743_) {
		super(be, p_97742_, p_97743_);
		inventoryLabelY -=4;
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
	    this.renderBackground(pose);
	    super.render(pose, mouseX, mouseY, partialTick);
	    RenderSystem.setShaderTexture(0, TEXTURE_BUTTON);
	    int holding = holdingOnButton(mouseX, mouseY);
	    int now = menu.modeData.get();
	    this.blit(pose, leftPos+7, topPos+41, 18 + (holding==1?18:0) + (now==0?36:0), 0, 18, 18);
	    this.blit(pose, leftPos+8, topPos+42, 19, 19, 16, 16);
	    this.blit(pose, leftPos+25, topPos+41, 18 + (holding==2?18:0) + (now==1?36:0), 0, 18, 18);
	    this.blit(pose, leftPos+26, topPos+42, 37, 19, 16, 16);
	    this.blit(pose, leftPos+43, topPos+41, 18 + (holding==3?18:0) + (now==2?36:0), 0, 18, 18);
	    this.blit(pose, leftPos+44, topPos+42, 55, 19, 16, 16);
	    this.renderTooltip(pose, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, TEXTURE_BASE);
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}
	
	@Override
	protected void renderTooltip(PoseStack pose, int x, int y) {
		switch(holdingOnButton(x, y)) {
		case 0:
			break;
		case 1:
			renderTooltip(pose, Component.translatable("write list"), x, y);
			break;
		case 2:
			renderTooltip(pose, Component.translatable("black"), x, y);
			break;
		case 3:
			renderTooltip(pose, Component.translatable("loop"), x, y);
		}
		super.renderTooltip(pose, x, y);
	}

	@Override
	public boolean mouseClicked(double x, double y, int p_97750_) {
		int i = holdingOnButton(x, y);
		if(i>0) {
			menu.clickMenuButton(null, i-1);
			this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, i-1);
		}
		return super.mouseClicked(x, y, p_97750_);
	}
	
	private int holdingOnButton(double x, double y) {
		x-=leftPos;
		y-=topPos;
		if(x>62||x<7||y>59||y<41)
			return 0;
		if(x<25)
			return 1;
		if(x>43)
			return 3;
		return 2;
	}
	
	


}
