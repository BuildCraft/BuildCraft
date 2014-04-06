package buildcraft.factory.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.gui.GuiBuildCraft;
import buildcraft.core.render.RenderUtils;
import buildcraft.core.utils.StringUtils;
import buildcraft.energy.TileEngineIron;
import buildcraft.energy.gui.GuiEngine;
import buildcraft.factory.TileRefineryControl;

public class GuiRefineryControl extends GuiBuildCraft{
	
	private static final ResourceLocation texture = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/refinery_control_gui.png");
	private static final ResourceLocation BLOCK_TEXTURE = TextureMap.locationBlocksTexture;
	private int x;
	private int y;
	private int z;
	private World world;
	
	public GuiRefineryControl(InventoryPlayer inventory, TileRefineryControl refineryControl) {
		super(new ContainerRefineryControl(inventory, refineryControl), refineryControl, texture);
		x = refineryControl.xCoord;
		y = refineryControl.yCoord;
		z = refineryControl.zCoord;
		world = refineryControl.getWorldObj();
		
	}
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		super.drawGuiContainerForegroundLayer(par1, par2);
		TileRefineryControl refinery = (TileRefineryControl) tile;
		String title = StringUtils.localize("tile.refineryControl.name");
		refinery.sendNetworkUpdate();
		if (buildcraft.core.utils.MultiBlockCheck.isPartOfAMultiBlock("refinery", x, y, z, world)){
			fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x008000);
			fontRendererObj.drawString("Oil: "+ refinery.input.getAmountOfLiquid(), 10, 50, 0x404040);
			fontRendererObj.drawString("Fuel: "+ refinery.output.getAmountOfLiquid(), 10, 65, 0x404040);
			fontRendererObj.drawString("Temprature: "+refinery.getTemprature(), 10, 80, 0x404040);
			} else {
				fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0xff0000);
				}
		}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		super.drawGuiContainerBackgroundLayer(f, x, y);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		TileRefineryControl  refineryB = (TileRefineryControl) tile;
		if (refineryB.valvesAssinged){
	        drawFluid(refineryB.input.getLiquid(), refineryB.input.getScaledFluid(58), j + 104, k + 19, 16, 58);
	        drawFluid(refineryB.output.getLiquid() ,refineryB.output.getScaledFluid(58), j + 122, k + 19, 16, 58);
		}
		mc.renderEngine.bindTexture(texture);
		drawTexturedModalRect(j + 104, k + 19, 176, 0, 16, 60);
	    drawTexturedModalRect(j + 122, k + 19, 176, 0, 16, 60);
		
	}

	private void drawFluid(FluidStack fluid, int level, int x, int y, int width, int height){
		if(fluid == null || fluid.getFluid() == null) {
			return;
		}
		IIcon icon = fluid.getFluid().getIcon(fluid);
		mc.renderEngine.bindTexture(BLOCK_TEXTURE);
		RenderUtils.setGLColorFromInt(fluid.getFluid().getColor(fluid));
		int fullX = width / 16;
		int fullY = height / 16;
		int lastX = width - fullX * 16;
		int lastY = height - fullY * 16;
		int fullLvl = (height - level) / 16;
		int lastLvl = (height - level) - fullLvl * 16;
		for(int i = 0; i < fullX; i++) {
			for(int j = 0; j < fullY; j++) {
				if(j >= fullLvl) {
					drawCutIcon(icon, x + i * 16, y + j * 16, 16, 16, j == fullLvl ? lastLvl : 0);
				}
			}
		}
		for(int i = 0; i < fullX; i++) {
			drawCutIcon(icon, x + i * 16, y + fullY * 16, 16, lastY, fullLvl == fullY ? lastLvl : 0);
		}
		for(int i = 0; i < fullY; i++) {
			if(i >= fullLvl) {
				drawCutIcon(icon, x + fullX * 16, y + i * 16, lastX, 16, i == fullLvl ? lastLvl : 0);
			}
		}
		drawCutIcon(icon, x + fullX * 16, y + fullY * 16, lastX, lastY, fullLvl == fullY ? lastLvl : 0);
	}

	//The magic is here
	private void drawCutIcon(IIcon icon, int x, int y, int width, int height, int cut){
		Tessellator tess = Tessellator.instance;
		tess.startDrawingQuads();
		tess.addVertexWithUV(x, y + height, zLevel, icon.getMinU(), icon.getInterpolatedV(height));
		tess.addVertexWithUV(x + width, y + height, zLevel, icon.getInterpolatedU(width), icon.getInterpolatedV(height));
		tess.addVertexWithUV(x + width, y + cut, zLevel, icon.getInterpolatedU(width), icon.getInterpolatedV(cut));
		tess.addVertexWithUV(x, y + cut, zLevel, icon.getMinU(), icon.getInterpolatedV(cut));
		tess.draw();
	}
	}
