package ct.buildcraft.transport.client.gui;

import ct.buildcraft.lib.gui.ContainerScreenBase;
import ct.buildcraft.lib.gui.component.EmzuliButton;
import ct.buildcraft.transport.BCTransportSprites;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ScreenPipeEmzuli extends ContainerScreenBase<MenuPipeEmzuli>{

	private static final ResourceLocation TEXTURE_BASE = BCTransportSprites.EMZULI_GUI;
	protected static final EmzuliButton[] buttons = {
			new EmzuliButton(50, 21),
			new EmzuliButton(50, 49),
			new EmzuliButton(105, 21),
			new EmzuliButton(105, 49)
	};
	
	public ScreenPipeEmzuli(MenuPipeEmzuli be, Inventory p_97742_, Component p_97743_) {
		super(be, p_97742_, p_97743_, 4, TEXTURE_BASE);
		for(int i = 0; i< 4;i++) 
			this.add(buttons[i], true);
		setup(be.data);
//		this.topPos = (this.height+76 - this.imageHeight) / 2;
	}
	
	

	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
	    super.render(pose, mouseX, mouseY, partialTick);
	    
	    this.renderTooltip(pose, mouseX, mouseY);
	}
	
	@Override
	protected void renderBg(PoseStack pose, float partialTick, int mouseX, int mouseY) {
		RenderSystem.setShaderTexture(0, TEXTURE_BASE);
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight+52);
	}
	
	
/*	@Override
	protected void init() {
		super.init();
		buttons[0] = new EmzuliSwitch(50,21,0,Component.empty());
		buttons[1] = new EmzuliSwitch(50,49,1,Component.empty());
		buttons[2] = new EmzuliSwitch(105,21,2,Component.empty());
		buttons[3] = new EmzuliSwitch(105,49,3,Component.empty());
		
	}*/
	
	






	
	
	


}
