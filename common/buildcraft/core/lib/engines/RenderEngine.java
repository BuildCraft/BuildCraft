/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.engines;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftCore.RenderMode;
import buildcraft.core.lib.render.IInventoryRenderer;

public class RenderEngine extends TileEntitySpecialRenderer implements IInventoryRenderer {

	private static final float[] angleMap = new float[6];

	static {
		angleMap[ForgeDirection.EAST.ordinal()] = (float) -Math.PI / 2;
		angleMap[ForgeDirection.WEST.ordinal()] = (float) Math.PI / 2;
		angleMap[ForgeDirection.UP.ordinal()] = 0;
		angleMap[ForgeDirection.DOWN.ordinal()] = (float) Math.PI;
		angleMap[ForgeDirection.SOUTH.ordinal()] = (float) Math.PI / 2;
		angleMap[ForgeDirection.NORTH.ordinal()] = (float) -Math.PI / 2;
	}

	private ModelBase model = new ModelBase() {
	};

	private ModelRenderer box;
	private ModelRenderer trunk;
	private ModelRenderer movingBox;
	private ModelRenderer chamber;

	private ResourceLocation baseTexture;
	private ResourceLocation chamberTexture;
	private ResourceLocation trunkTexture;

	public RenderEngine() {
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

	public RenderEngine(ResourceLocation baseTexture, ResourceLocation chamberTexture, ResourceLocation trunkTexture) {
		this();
		this.baseTexture = baseTexture;
		this.chamberTexture = chamberTexture;
		this.trunkTexture = trunkTexture;
		field_147501_a = TileEntityRendererDispatcher.instance;
	}

	public RenderEngine(TileEngineBase engine) {
		this(engine.getBaseTexture(), engine.getChamberTexture(), engine.getTrunkTexture(TileEngineBase.EnergyStage.BLUE));
	}

	@Override
	public void inventoryRender(double x, double y, double z, float f, float f1) {
		render(0.25F, ForgeDirection.UP, baseTexture, chamberTexture, trunkTexture, x, y, z);
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y, double z, float f) {
		TileEngineBase engine = (TileEngineBase) tileentity;

		if (engine != null) {
			/* float progress = engine.progress + (engine.progressPart != 0 ? f * engine.getPistonSpeed() : 0);
			if (progress > 1) {
				progress = 0;
			} -- uncomment out for smooth engine rendering */

			render(engine.progress, engine.orientation, engine.getBaseTexture(), engine.getChamberTexture(), engine.getTrunkTexture(engine.getEnergyStage()), x, y, z);
		}
	}

	private void render(float progress, ForgeDirection orientation, ResourceLocation baseTexture, ResourceLocation chamberTexture, ResourceLocation trunkTexture, double x, double y, double z) {
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

		float[] angle = {0, 0, 0};
		float[] translate = {orientation.offsetX, orientation.offsetY, orientation.offsetZ};

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

		bindTexture(chamberTexture);

		float chamberf = 2F / 16F;
		int chamberc = ((int) step + 4) / 2;

		for (int i = 0; i <= step + 2; i += 2) {
			chamber.render(factor);
			GL11.glTranslatef(translate[0] * chamberf, translate[1] * chamberf, translate[2] * chamberf);
		}

		GL11.glTranslatef(-translate[0] * chamberf * chamberc, -translate[1] * chamberf * chamberc, -translate[2] * chamberf * chamberc);

		bindTexture(trunkTexture);

		trunk.render(factor);

		GL11.glPopAttrib();
		GL11.glPopMatrix();
	}
}
