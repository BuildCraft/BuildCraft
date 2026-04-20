package ct.buildcraft.factory.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

import ct.buildcraft.factory.BCFactorySprites;
import ct.buildcraft.lib.gui.ContainerScreenBase;
import ct.buildcraft.lib.gui.component.TankComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenHeatExchange extends ContainerScreenBase<MenuHeatExchange>{

	private static final ResourceLocation TEXTURE_BASE = BCFactorySprites.HEAT_EXCHANGE;
	
	protected static final TankComponent[] tanks = {
		new TankComponent(44, 12, 16, 38, 2000, -1, -1),
		new TankComponent(44, 64, 33, 16, 2000, -1, -1),
		new TankComponent(98, 12, 33, 16, 2000, -1, -1),
		new TankComponent(116,43 ,16, 38, 2000, -1, -1),
	};
	
	public ScreenHeatExchange(MenuHeatExchange menu, Inventory inventory, Component name) {
		super(menu, inventory, name, 4, TEXTURE_BASE);
		titleLabelY -= 3;
		inventoryLabelY += 8;
		for(int i = 0; i<4;i++)
			add(tanks[i], true);
		setup(menu.data);
	}
	

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
		super.render(pose, mouseX, mouseY, partialTick);
		this.renderTooltip(pose, mouseX, mouseY);
	}


	@Override
	protected void renderBg(PoseStack pose, float p_97788_, int p_97789_, int p_97790_) {
		super.renderBg(pose, p_97788_, p_97789_, p_97790_);
	}
	
	



}
