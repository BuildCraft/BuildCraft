package ct.buildcraft.transport.client.gui;

import ct.buildcraft.transport.BCTransportSprites;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ScreenFilteredBuffer extends AbstractContainerScreen<MenuFilteredBuffer>{

	private static final ResourceLocation TEXTURE_BASE = BCTransportSprites.FILTERED_BUFFER_GUI;
	private static TextureAtlasSprite NOTHING ;
	
	public ScreenFilteredBuffer(MenuFilteredBuffer be, Inventory p_97742_, Component p_97743_) {
		super(be, p_97742_, p_97743_);
		inventoryLabelY +=5;
		if(NOTHING == null)
			NOTHING = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BCTransportSprites.FILTERED_BUFFER_NOTTHING_SLOT_GUI);
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
	    this.renderBackground(pose);
	    super.render(pose, mouseX, mouseY, partialTick);
	    RenderSystem.setShaderTexture(0, InventoryMenu.BLOCK_ATLAS);
	    for(int i = 0;i<9;i++) {
	    	Slot slot1 = menu.getSlot(36 + 2*i);
	    	Slot slot2 = menu.getSlot(37 + 2*i);
	    	if(!slot2.getItem().isEmpty())
	    		continue;
	    	ItemStack item = slot1.getItem();
	    	int x = slot1.x + leftPos;
    		int y = slot1.y + topPos + 34;
	    	if(item.isEmpty()) {
	    		blit(pose, x, y, 515, 16, 16, NOTHING);
	    	}
	    	else {
	    		itemRenderer.renderAndDecorateFakeItem(item, x, y);
	    	}
	    	RenderSystem.depthFunc(516);
	    	GuiComponent.fill(pose, x, y, x+16,  y + 16, 822083583);
	    	RenderSystem.depthFunc(515);
	    }
	    this.renderTooltip(pose, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, TEXTURE_BASE);
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
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
