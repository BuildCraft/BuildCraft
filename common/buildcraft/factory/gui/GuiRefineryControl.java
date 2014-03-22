package buildcraft.factory.gui;

import org.lwjgl.opengl.GL11;

import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import buildcraft.core.DefaultProps;
import buildcraft.core.gui.GuiAdvancedInterface;
import buildcraft.core.utils.StringUtils;
import buildcraft.factory.TileRefineryControl;

public class GuiRefineryControl extends GuiAdvancedInterface{
	
	private static final ResourceLocation texture = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/ledger.png");
	private int x;
	private int y;
	private int z;
	private World world;
	
	public GuiRefineryControl(InventoryPlayer inventory, TileRefineryControl refineryControl) {
		super(new ContainerRefineryControl(inventory, refineryControl), inventory, texture);
		x = refineryControl.xCoord;
		y = refineryControl.yCoord;
		z = refineryControl.zCoord;
		world = refineryControl.getWorldObj();
		
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(texture);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}
	
	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		String title = StringUtils.localize("tile.refineryControl.name");
		fontRendererObj.drawString(title, getCenteredOffset(title), 6, 0x404040);
		if (!(buildcraft.core.utils.MultiBlockCheck.isPartOfAMultiBlock("refinery", x, y, z, world))){
			fontRendererObj.drawString("NO REFINERY DETECTED", getCenteredOffset("NO REFINERY DETECTED"), 40, 0x404040);
			} else {
				fontRendererObj.drawString("REFINERY DETECTED", getCenteredOffset("REFINERY DETECTED"), 40, 0x404040);
				}
		}
	}
