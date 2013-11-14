/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy.render;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.core.DefaultProps;
import buildcraft.core.IInventoryRenderer;
import buildcraft.energy.TileEngine;
import buildcraft.energy.TileEngine.EnergyStage;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.ForgeDirection;
import static net.minecraftforge.common.ForgeDirection.DOWN;
import static net.minecraftforge.common.ForgeDirection.EAST;
import static net.minecraftforge.common.ForgeDirection.NORTH;
import static net.minecraftforge.common.ForgeDirection.SOUTH;
import static net.minecraftforge.common.ForgeDirection.UP;
import static net.minecraftforge.common.ForgeDirection.WEST;
import org.lwjgl.opengl.GL11;

public class RenderEngine extends TileEntitySpecialRenderer implements IInventoryRenderer {

	private static final ResourceLocation CHAMBER_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/chamber.png");
	private static final ResourceLocation TRUNK_BLUE_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_blue.png");
	private static final ResourceLocation TRUNK_GREEN_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_green.png");
	private static final ResourceLocation TRUNK_YELLOW_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_yellow.png");
	private static final ResourceLocation TRUNK_RED_TEXTURE = new ResourceLocation(DefaultProps.TEXTURE_PATH_BLOCKS + "/trunk_red.png");
	private ModelBase model = new ModelBase() {
	};
	private ModelRenderer box;
	private ModelRenderer trunk;
	private ModelRenderer movingBox;
	private ModelRenderer chamber;
	private ResourceLocation baseTexture;
	private static final float[] angleMap = new float[6];

	static {
		angleMap[EAST.ordinal()] = (float) -Math.PI / 2;
		angleMap[WEST.ordinal()] = (float) Math.PI / 2;
		angleMap[UP.ordinal()] = 0;
		angleMap[DOWN.ordinal()] = (float) Math.PI;
		angleMap[SOUTH.ordinal()] = (float) Math.PI / 2;
		angleMap[NORTH.ordinal()] = (float) -Math.PI / 2;
	}

	public RenderEngine() {

		// constructor:
		box = new ModelRenderer(model, 0, 1);
		box.addBox(-8F, -8F, -8F, 16, 4, 16);
		box.rotationPointX = 8;
		box.rotationPointY = 8;
		box.rotationPointZ = 8;

		trunk = new ModelRenderer(model, 1, 1);
		trunk.addBox(-4F, -4F, -4F, 8, 12, 8);
		trunk.rotationPointX = 8F;
		trunk.rotationPointY = 8F;
		trunk.rotationPointZ = 8F;

		movingBox = new ModelRenderer(model, 0, 1);
		movingBox.addBox(-8F, -4, -8F, 16, 4, 16);
		movingBox.rotationPointX = 8F;
		movingBox.rotationPointY = 8F;
		movingBox.rotationPointZ = 8F;

		chamber = new ModelRenderer(model, 1, 1);
		chamber.addBox(-5F, -4, -5F, 10, 2, 10);
		chamber.rotationPointX = 8F;
		chamber.rotationPointY = 8F;
		chamber.rotationPointZ = 8F;
	}

	public RenderEngine(ResourceLocation baseTexture) {
		this();
		this.baseTexture = baseTexture;
		setTileEntityRenderer(TileEntityRenderer.instance);
	}

	@Override
	public void inventoryRender(double x, double y, double z, float f, float f1) {
		render(EnergyStage.BLUE, 0.25F, ForgeDirection.UP, baseTexture, x, y, z);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {

		TileEngine engine = ((TileEngine) tileentity);

		if (engine != null) {
			render(engine.getEnergyStage(), engine.progress, engine.orientation, engine.getTextureFile(), x, y, z);
		}
	}

	private void render(EnergyStage energy, float progress, ForgeDirection orientation, ResourceLocation baseTexture, double x, double y, double z) {

		if (BuildCraftCore.render == RenderMode.NoDynamic) {
			return;
		}

		GL11.glPushMatrix();
		GL11.glPushAttrib(GL11.GL_ENABLE_BIT);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glColor3f(1, 1, 1);

		GL11.glTranslatef((float) x, (float) y, (float) z);

		float step;

		if (progress > 0.5) {
			step = 7.99F - (progress - 0.5F) * 2F * 7.99F;
		} else {
			step = progress * 2F * 7.99F;
		}

		float translatefact = step / 16;

		float[] angle = { 0, 0, 0 };
		float[] translate = { orientation.offsetX, orientation.offsetY, orientation.offsetZ };

		switch (orientation) {
			case EAST:
			case WEST:
			case DOWN:
				angle[2] = angleMap[orientation.ordinal()];
				break;
			case SOUTH:
			case NORTH:
			default:
				angle[0] = angleMap[orientation.ordinal()];
				break;
		}

		box.rotateAngleX = angle[0];
		box.rotateAngleY = angle[1];
		box.rotateAngleZ = angle[2];

		trunk.rotateAngleX = angle[0];
		trunk.rotateAngleY = angle[1];
		trunk.rotateAngleZ = angle[2];

		movingBox.rotateAngleX = angle[0];
		movingBox.rotateAngleY = angle[1];
		movingBox.rotateAngleZ = angle[2];

		chamber.rotateAngleX = angle[0];
		chamber.rotateAngleY = angle[1];
		chamber.rotateAngleZ = angle[2];

		float factor = (float) (1.0 / 16.0);

		bindTexture(baseTexture);

		box.render(factor);

		GL11.glTranslatef(translate[0] * translatefact, translate[1] * translatefact, translate[2] * translatefact);
		movingBox.render(factor);
		GL11.glTranslatef(-translate[0] * translatefact, -translate[1] * translatefact, -translate[2] * translatefact);

		bindTexture(CHAMBER_TEXTURE);

		float chamberf = 2F / 16F;

		for (int i = 0; i <= step + 2; i += 2) {
			chamber.render(factor);
			GL11.glTranslatef(translate[0] * chamberf, translate[1] * chamberf, translate[2] * chamberf);
		}

		for (int i = 0; i <= step + 2; i += 2) {
			GL11.glTranslatef(-translate[0] * chamberf, -translate[1] * chamberf, -translate[2] * chamberf);
		}

		ResourceLocation texture;

		switch (energy) {
			case BLUE:
				texture = TRUNK_BLUE_TEXTURE;
				break;
			case GREEN:
				texture = TRUNK_GREEN_TEXTURE;
				break;
			case YELLOW:
				texture = TRUNK_YELLOW_TEXTURE;
				break;
			default:
				texture = TRUNK_RED_TEXTURE;
				break;
		}

		bindTexture(texture);

		trunk.render(factor);

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
