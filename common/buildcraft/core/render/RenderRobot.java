/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.IItemRenderer;

import buildcraft.BuildCraftCore;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityLaser;
import buildcraft.core.robots.EntityRobot;

public class RenderRobot extends Render implements IItemRenderer {

	public static final ResourceLocation TEXTURE_BASE = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_base.png");
	public static final ResourceLocation TEXTURE_BUILDER = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_builder.png");
	public static final ResourceLocation TEXTURE_PICKER = new ResourceLocation("buildcraft", DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_picker.png");

	protected ModelBase model = new ModelBase() {
	};
	private ModelRenderer box;

	public RenderRobot() {
		box = new ModelRenderer(model, 0, 0);
		box.addBox(-4F, -4F, -4F, 8, 8, 8);
		box.rotationPointX = 0;
		box.rotationPointY = 0;
		box.rotationPointZ = 0;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		doRender((EntityRobot) entity, x, y, z, f, f1);
	}

	private void doRender(EntityRobot robot, double x, double y, double z, float f, float f1) {
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glTranslated(x, y, z);

		renderManager.renderEngine.bindTexture(robot.getTexture());

		float factor = (float) (1.0 / 16.0);

		box.render(factor);

		if (robot.laser.isVisible) {
			robot.laser.head.x = robot.posX;
			robot.laser.head.y = robot.posY;
			robot.laser.head.z = robot.posZ;

			RenderLaser.doRenderLaser(renderManager.renderEngine, robot.laser, EntityLaser.LASER_TEXTURES [1]);
		}

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();

	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return TEXTURE_BASE;
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item,
			ItemRendererHelper helper) {

		if (helper == ItemRendererHelper.BLOCK_3D) {
			return true;
		} else {
			return helper == ItemRendererHelper.INVENTORY_BLOCK;
		}
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (RenderManager.instance == null
				|| RenderManager.instance.renderEngine == null) {
			return;
		}

		RenderBlocks renderBlocks = (RenderBlocks) data[0];

		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);

		// FIXME: Texture localisation should be factorized between items and
		// entities.
		if (item.getItem() == BuildCraftCore.robotBaseItem) {
			RenderManager.instance.renderEngine.bindTexture(TEXTURE_BASE);
		} else if (item.getItem() == BuildCraftCore.robotBuilderItem) {
			RenderManager.instance.renderEngine.bindTexture(TEXTURE_BUILDER);
		} else if (item.getItem() == BuildCraftCore.robotPickerItem) {
			RenderManager.instance.renderEngine.bindTexture(TEXTURE_PICKER);
		}

		float factor = (float) (1.0 / 16.0);

		if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			GL11.glTranslated(0.25F, 0.5F, 0);
		}

		box.render(1F / 16F);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}
}
