package ct.buildcraft.lib.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.BCLog;
import ct.buildcraft.lib.gui.component.ContainerComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;

public abstract class ContainerScreenBase<T extends AbstractContainerMenu> extends AbstractContainerScreen<T>{

	protected final ResourceLocation TEXTURE_BASE;
	protected final ContainerComponent[] components;
	protected final boolean[] listenClick;
	protected int index = 0;
	protected int offset = 0;
	protected int size;
	
	public ContainerScreenBase(T menu, Inventory inventory, Component name, int ComponentSize, ResourceLocation back) {
		super(menu, inventory, name);
		this.TEXTURE_BASE = back;
		this.components = new ContainerComponent[ComponentSize];
		this.listenClick = new boolean[ComponentSize];
		this.size = ComponentSize;
	}
	
	public ContainerScreenBase(T menu, Inventory inventory, Component name, int ComponentSize) {
		super(menu, inventory, name);
		this.TEXTURE_BASE = null;
		this.components = new ContainerComponent[ComponentSize];
		this.listenClick = new boolean[ComponentSize];
		this.size = ComponentSize;
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTick);
		for(int i = 0;i<size;i++) 
			components[i].render(pose, mouseX, mouseY, partialTick, this);
		if(TEXTURE_BASE == null)
			return;
		RenderSystem.setShaderTexture(0, TEXTURE_BASE);
		for(int i = 0;i<size;i++) {
			components[i].postRender(pose, mouseX, mouseY, partialTick, this);
		}
		
	}
	
	@Override
	protected void renderBg(PoseStack pose, float p_97788_, int p_97789_, int p_97790_) {
		if(TEXTURE_BASE == null)
			return;
		RenderSystem.setShaderTexture(0, TEXTURE_BASE);
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
	}

	
	@Override
	public boolean mouseClicked(double x, double y, int mouse) {
		for(int i = 0;i<size;i++) {
			if(listenClick[i])
				if(components[i].onClick(x, y, mouse))
					return true;
		}
		return super.mouseClicked(x, y, mouse);
	}
	
	@Override
	public boolean mouseReleased(double x, double y, int mouse) {
		for(int i = 0;i<size;i++) {
			if(listenClick[i])
				if(components[i].mouseRelease(x, y, mouse))
					return true;
		}
		return super.mouseReleased(x, y, mouse);
	}

	public void add(ContainerComponent com, boolean shouldListenClick) {
		if(index<0||index>=size) {
			BCLog.logger.error("ContainerScreenBase.add:index %d out of range %d",index, size);
			size = 0;
			return ;
		}
		components[index] = com;
		listenClick[index++] = shouldListenClick;
		com.setDataoffset(offset);
		offset += com.getNeedDataSize();
	}
	
	public void setup(ContainerData data) {
		if(data.getCount() != offset) {
			BCLog.logger.error("ContainerScreenBase.setDatas:the input data count does not equal this size");
			size = 0;
			return ;
		}
		for(int i = 0;i<size;i++) {
			components[i].setup(this, data);
		}
	}

	@Override
	public void onClose() {
		for(int i = 0;i<size;i++) {
			components[i].onClose();
		}
		super.onClose();
	}

	@Override
	protected void renderTooltip(PoseStack pose, int x, int y) {
		for(int i = 0;i<size;i++) {
			components[i].renderTooltip(pose, x, y);;
		}
		super.renderTooltip(pose, x, y);
	}
	
	
	
	
	


}
