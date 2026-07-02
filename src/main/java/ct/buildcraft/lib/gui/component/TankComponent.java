package ct.buildcraft.lib.gui.component;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.lib.client.render.fluid.FluidRenderer;
import ct.buildcraft.lib.gui.TankContainerData;
import ct.buildcraft.lib.misc.LocaleUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

public class TankComponent extends AbstractComponent{
	
	protected int typeCache;
	protected Fluid fluidCache;
	protected final int capacity;
	protected final byte renderType;
	protected /*final*/ int tankx;
	protected /*final*/ int tanky;
	
	public TankComponent(int x, int y, int sx, int sy, int capacity, int tankx, int tanky) {
		super(x, y, sx, sy);
		this.capacity = capacity;
		renderType = U_TO_D;
		this.tankx = tankx;
		this.tanky = tanky;
	}
	
	public TankComponent(int x, int y, int sx, int sy, int capacity, int tankx, int tanky, byte type) {
		super(x, y, sx, sy);
		this.capacity = capacity;
		renderType = type;
		this.tankx = tankx;
		this.tanky = tanky;
	}
	
	//for debug
	public void resetSpritePos(int tankx, int tanky) {
		this.tankx = tankx;
		this.tanky = tanky;
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick, AbstractContainerScreen<?> screen) {
		int leftpos = screen.getGuiLeft();
		int toppos = screen.getGuiTop();
		int type = data.get(offset);
		if(type != typeCache)  {
			typeCache = type;
			fluidCache = TankContainerData.getFluid(type);
		}
		if(this.fluidCache != null)
			FluidRenderer.drawFluidForGui(this.fluidCache,leftpos+x,toppos+y+ys, leftpos+x+xs, toppos+y+ys-(ys*data.get(offset+1)/capacity), pose.last());
	}
	
	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTick, AbstractContainerScreen<?> screen) {
		if(tankx>=0&&tanky>=0)
			screen.blit(pose, screen.getGuiLeft()+x, screen.getGuiTop()+y+1, tankx, tanky, xs, ys);
	}

	@Override
	public void renderTooltip(PoseStack pose, int x, int y) {
		if(super.isHovering(x-screen.getGuiLeft(), y-screen.getGuiTop()))
			screen.renderComponentTooltip(pose, getToolTip(fluidCache, data.get(offset+1)), x, y);
	}


	@Override
	public int getNeedDataSize() {
		return 2;
	}

	protected List<Component> getToolTip(Fluid fluid,int amount) {
        List<Component> toolTip = Lists.newArrayList();
        if (amount > 0) 
        	toolTip.add(fluid.getFluidType().getDescription());
        toolTip.add((LocaleUtil.localizeFluidStaticAmount(amount, capacity)).withStyle(ChatFormatting.GRAY));
        return toolTip ;
    }
	
	

}
