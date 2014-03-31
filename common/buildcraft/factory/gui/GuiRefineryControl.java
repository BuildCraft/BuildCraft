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
		if (buildcraft.core.utils.MultiBlockCheck.isPartOfAMultiBlock("refinery", x, y, z, world)){
			fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x008000);
			} else {
				fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0xff0000);
				}
		fontRendererObj.drawString("Oil: "+ refinery.AmountOfOil(), 10, 50, 0x404040);
		fontRendererObj.drawString("Fuel: "+ refinery.AmountOfFuel(), 10, 65, 0x404040);
		
		}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1F, 1F, 1F, 1F);
		
		Minecraft.getMinecraft().getTextureManager().
		bindTexture(texture);
		
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		
		int height;
		TileRefineryControl  refinery = (TileRefineryControl) world.getTileEntity(x, y, z);
		if (refinery != null){
			height = refinery.getScaledInput(45);
			this.drawTexturedModalRect(guiLeft + 8, guiTop + 53 - height, 176, 62 - height, 16, height);
		}
			}
	}
