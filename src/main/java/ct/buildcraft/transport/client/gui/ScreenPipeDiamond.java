package ct.buildcraft.transport.client.gui;

import ct.buildcraft.transport.BCTransportSprites;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenPipeDiamond extends AbstractContainerScreen<MenuPipeDiamond>{

	private static final ResourceLocation TEXTURE_BASE = BCTransportSprites.DIAMOND_GUI;
	
	public ScreenPipeDiamond(MenuPipeDiamond be, Inventory p_97742_, Component p_97743_) {
		super(be, p_97742_, p_97743_);
		titleLabelY -= 36;
		inventoryLabelY += 20;
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
	    this.renderBackground(pose);
	    super.render(pose, mouseX, mouseY, partialTick);
	    this.renderTooltip(pose, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, TEXTURE_BASE);
		this.blit(pose, this.leftPos, this.topPos-36, 0, 0, this.imageWidth, this.imageHeight+52);
	}
	
	@Override
	protected void renderTooltip(PoseStack pose, int x, int y) {
		super.renderTooltip(pose, x, y);
	}

	@Override
	public boolean mouseClicked(double x, double y, int p_97750_) {
		return super.mouseClicked(x, y, p_97750_);
	}
	
	
	
	


}
