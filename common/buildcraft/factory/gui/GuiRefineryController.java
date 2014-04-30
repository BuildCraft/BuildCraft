package buildcraft.factory.gui;

import buildcraft.core.DefaultProps;
import buildcraft.factory.TileRefineryController;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class GuiRefineryController extends GuiContainer {

	private static final int FRAME_LOCK = 30;

	private static final ResourceLocation TEXTURE_GUI = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/refinery_controller.png");
	private static final ResourceLocation TEXTURE_ELEMENTS = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_GUI + "/refinery_controller_elements.png");

	private final EntityPlayer player;

	private final TileRefineryController tile;

	private float rotation = 0;

	private float lastTime = 0L;

	public GuiRefineryController(EntityPlayer player, TileRefineryController tile) {
		super(new ContainerRefineryController(player, tile));
		this.player = player;
		this.tile = tile;

		this.xSize = 196;
		this.ySize = 254;
	}

	@Override
	public void drawScreen(int x, int y, float delta) {
		if (System.nanoTime() > lastTime) {
			lastTime = System.nanoTime() + (1000L / FRAME_LOCK);

			rotation += (1 + delta);

			if (rotation > 360) {
				rotation = 0;
			}
		}

		super.drawScreen(x, y, delta);
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int x, int y) {
		float scale = 8;

		GL11.glPushMatrix();

		GL11.glEnable(GL11.GL_DEPTH_TEST);

		GL11.glTranslated((xSize / 2), (ySize / 4), 100F);

		GL11.glTranslated(0.5, 0.5, ((float) tile.length / (float) 2));

		GL11.glScaled(-scale, scale, scale);

		GL11.glRotated(180, 0, 0, 1);
		GL11.glRotated(30, 1, 0, 0);
		GL11.glRotated(rotation, 0, 1, 0);

		GL11.glTranslated(-0.5, -0.5, -((float)tile.length / (float)2));

		TileEntityRendererDispatcher.instance.renderTileEntityAt(tile, 0, 0, 0, 0);

		GL11.glPopMatrix();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int x, int y) {
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		int j = (width - xSize) / 2;
		int k = (height - ySize) / 2;

		// Model view background
		mc.renderEngine.bindTexture(TEXTURE_ELEMENTS);
		drawTexturedModalRect(j + 45, k + 24, 0, 136, 106, 76);

		mc.renderEngine.bindTexture(TEXTURE_GUI);
		drawTexturedModalRect(j, k, 0, 0, xSize, ySize);
	}

}
