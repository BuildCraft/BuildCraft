package buildcraft.factory.gui;

import buildcraft.core.DefaultProps;
import buildcraft.factory.TileRefineryController;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiRefineryController extends GuiContainer {

	private static final ResourceLocation TEXTURE_GUI = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/refinery_controller.png");
	private static final ResourceLocation TEXTURE_ELEMENTS = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/refinery_controller_elements.png");

	private final EntityPlayer player;

	private final TileRefineryController tile;

	public GuiRefineryController(EntityPlayer player, TileRefineryController tile) {
		super(new ContainerRefineryController(player, tile));
		this.player = player;
		this.tile = tile;

		this.xSize = 196;
		this.ySize = 254;
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		float scale = 8;

		GL11.glPushMatrix();

		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glTranslated((xSize / 2) - 8, (ySize / 4) - 8, 100F);

		GL11.glScaled(-scale, scale, scale);

		GL11.glRotated(180, 1, 0, 0);
		GL11.glRotated(180, 0, 0, 1);

		GL11.glTranslated(0.5, 0.5, ((float)tile.length / (float)2));
		GL11.glRotated(45, 1, 0, 0);
		GL11.glRotated(45, 0, 1, 0);
		GL11.glTranslated(-0.5, -0.5, -((float)tile.length / (float)2));

//		Minecraft.getMinecraft().renderEngine.bindTexture(ModelBoiler.TEXTURE_FRONT);
//		RenderBoiler.modelFront.render(0.0625F);

		TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, 0, 0, 0, 0);

		GL11.glPopMatrix();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.renderEngine.bindTexture(TEXTURE_GUI);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

}
