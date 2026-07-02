package ct.buildcraft.builders.gui;

import ct.buildcraft.builders.BCBuildersSprites;
import ct.buildcraft.lib.gui.ContainerScreenBase;
import ct.buildcraft.lib.gui.component.TankComponent;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;

public class ScreenBuilder extends ContainerScreenBase<MenuBuilder>{

	private static final ResourceLocation TEXTURE_BASE1 = null;//BCBuildersSprites.BUILDER_GUI;
	private static final ResourceLocation TEXTURE_BASE2 = null;//BCBuildersSprites.BUILDER_BLUEPRINT_GUI;
	
	protected static final TankComponent tank0 = new TankComponent(141, 112, 16, 47, 8000, 0, 55);
	protected static final TankComponent tank1 = new TankComponent(159, 112, 16, 47, 8000, 0, 55);
	protected static final TankComponent tank2 = new TankComponent(177, 112, 16, 47, 8000, 0, 55);
	protected static final TankComponent tank3 = new TankComponent(195, 112, 16, 47, 8000, 0, 55);
	
	protected final ContainerData data;
	
	public ScreenBuilder(MenuBuilder be, Inventory p_97742_, Component p_97743_) {
		super(be, p_97742_, p_97743_, 4, TEXTURE_BASE2);
		data = be.data;
		inventoryLabelX -= 40;
		inventoryLabelY += 23;
		titleLabelX -= 40;
		titleLabelY -= 32;
		this.add(tank0, true);
		this.add(tank1, true);
		this.add(tank2, true);
		this.add(tank3, true);
		setup(data);
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
	    super.render(pose, mouseX, mouseY, partialTick);
	    this.renderTooltip(pose, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, TEXTURE_BASE1);
		this.blit(pose, this.leftPos + (this.imageWidth - 256)/2, this.topPos + (this.imageHeight - 232)/2, 0, 0, 256, 221);
		RenderSystem.setShaderTexture(0, TEXTURE_BASE2);
		this.blit(pose, this.leftPos + (this.imageWidth - 256 +64)/2, this.topPos + (this.imageHeight - 232)/2, 30, 0, this.imageWidth+50, 221);
		
		
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
