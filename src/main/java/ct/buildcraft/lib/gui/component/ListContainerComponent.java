package ct.buildcraft.lib.gui.component;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.api.core.BCLog;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.ContainerData;

public class ListContainerComponent implements ContainerComponent {

	protected final ContainerComponent[] components;
	protected final boolean[] listenClick;
	protected int index = 0;
	protected int offset = 0;
	protected final int size;
	
	public ListContainerComponent(int size) {
		this.components = new ContainerComponent[size];
		this.listenClick = new boolean[size];
		this.size = size;
	}

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick, AbstractContainerScreen<?> screen) {
		for(int i = 0;i<size;i++) {
			components[i].render(pose, mouseX, mouseY, partialTick, screen);
		}
	}
	
	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTick,
			AbstractContainerScreen<?> screen) {
		for(int i = 0;i<size;i++) {
			components[i].postRender(pose, mouseX, mouseY, partialTick, screen);
		}
	}
	
	@Override
	public boolean onClick(double x, double y, int mouse) {
		for(int i = 0;i<size;i++) {
			if(listenClick[i])
				if(components[i].onClick(x, y, mouse))
					return true;
		}
		return false;
	}
	
	
	@Override
	public boolean mouseRelease(double x, double y, int mouse) {
		for(int i = 0;i<size;i++) {
			if(listenClick[i])
				if(components[i].mouseRelease(x, y, mouse))
					return true;
		}
		return false;
	}

	public ListContainerComponent add(ContainerComponent com, boolean shouldListenClick) {
		if(index<0||index>=size) {
			BCLog.logger.error("ListContainerComponent.add:index %d out of range %d",index, size);
			return this;
		}
		components[index] = com;
		listenClick[index++] = shouldListenClick;
		com.setDataoffset(offset);
		offset += com.getNeedDataSize();
		return this;
	}
	
	public void setUp(AbstractContainerScreen<?> screen, ContainerData... data) {
		if(data.length != size) {
			BCLog.logger.error("ListContainerComponent.setDatas:the input array length does not equal this size");
			return ;
		}
		for(int i = 0;i<size;i++) {
			components[i].setup(screen, data[i]);
		}
	}
	
	
	public void onClose() {
		for(int i = 0;i<size;i++) {
			components[i].onClose();;
		}
	}

	@Override
	public int getNeedDataSize() {
		return 0;
	}

}
