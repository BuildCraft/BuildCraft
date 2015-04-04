/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robots.render;

import java.util.Date;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.IItemRenderer;

import buildcraft.BuildCraftSilicon;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityLaser;
import buildcraft.core.render.RenderLaser;
import buildcraft.core.render.RenderUtils;
import buildcraft.robots.EntityRobot;
import buildcraft.robots.ItemRobot;

public class RenderRobot extends Render implements IItemRenderer {

	private static final ResourceLocation overlay = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_overlay.png");
	
	private final EntityItem dummyEntityItem = new EntityItem(null);
	private final RenderItem customRenderItem;

	private ModelBase model = new ModelBase() {
	};
	private ModelRenderer box;

	public RenderRobot() {
		customRenderItem = new RenderItem() {
			@Override
			public boolean shouldBob() {
				return false;
			}

			@Override
			public boolean shouldSpreadItems() {
				return false;
			}
		};
		customRenderItem.setRenderManager(RenderManager.instance);

		box = new ModelRenderer(model, 0, 0);
		box.addBox(-4F, -4F, -4F, 8, 8, 8);
		box.rotationPointX = 0;
		box.rotationPointY = 0;
		box.rotationPointZ = 0;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		doRender((EntityRobot) entity, x, y, z);
	}

	private void doRender(EntityRobot robot, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);

		if (robot.getStackInSlot(0) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.125F, 0, -0.125F);
			doRenderItem(robot.getStackInSlot(0));
			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(1) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(+0.125F, 0, -0.125F);
			doRenderItem(robot.getStackInSlot(1));
			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(2) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(+0.125F, 0, +0.125F);
			doRenderItem(robot.getStackInSlot(2));
			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(3) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.125F, 0, +0.125F);
			doRenderItem(robot.getStackInSlot(3));
			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.itemInUse != null) {
			GL11.glPushMatrix();

			GL11.glRotatef((float) (-robot.itemAngle1 / (2 * Math.PI) * 360) + 180, 0, 1, 0);
			GL11.glRotatef((float) (robot.itemAngle2 / (2 * Math.PI) * 360), 0, 0, 1);

			if (robot.itemActive) {
				long newDate = new Date().getTime();
				robot.itemActiveStage = (robot.itemActiveStage + (newDate - robot.lastUpdateTime) / 10) % 45;
				GL11.glRotatef(robot.itemActiveStage, 0, 0, 1);
				robot.lastUpdateTime = newDate;
			}

			GL11.glTranslatef(-0.4F, 0, 0);
			GL11.glRotatef(-45F + 180F, 0, 1, 0);
			GL11.glScalef(0.8F, 0.8F, 0.8F);

			ItemStack itemstack1 = robot.itemInUse;

			if (itemstack1.getItem().requiresMultipleRenderPasses()) {
				for (int k = 0; k < itemstack1.getItem().getRenderPasses(itemstack1.getItemDamage()); ++k) {
					RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, k));
					this.renderManager.itemRenderer.renderItem(robot, itemstack1, k);
				}
			} else {
				RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, 0));
				this.renderManager.itemRenderer.renderItem(robot, itemstack1, 0);
			}

			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.laser.isVisible) {
			robot.laser.head.x = robot.posX;
			robot.laser.head.y = robot.posY;
			robot.laser.head.z = robot.posZ;

			RenderLaser.doRenderLaser(renderManager.renderEngine, robot.laser, EntityLaser.LASER_TEXTURES[1]);
		}

		if (robot.getTexture() != null) {
			renderManager.renderEngine.bindTexture(robot.getTexture());
			doRenderRobot(1F / 16F, renderManager.renderEngine);
		}
		
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {
		return ((EntityRobot) entity).getTexture();
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return true;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		if (RenderManager.instance == null || RenderManager.instance.renderEngine == null) {
			return;
		}

		GL11.glPushMatrix();

		if (item.getItem() == BuildCraftSilicon.robotItem) {
			ItemRobot robot = (ItemRobot) item.getItem();
			RenderManager.instance.renderEngine.bindTexture(robot.getTextureRobot(item));
		}

		if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			GL11.glTranslated(0.0, 1.0, 0.7);
		} else if (type == ItemRenderType.ENTITY) {
			GL11.glScaled(0.6, 0.6, 0.6);
		} else if (type == ItemRenderType.INVENTORY) {
			GL11.glScaled(1.5, 1.5, 1.5);
		}
		
		doRenderRobot(1F / 16F, RenderManager.instance.renderEngine);
		
		GL11.glPopMatrix();
	}

	private void doRenderItem(ItemStack stack) {
		float renderScale = 0.5f;
		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0.28F, 0);
		GL11.glScalef(renderScale, renderScale, renderScale);
		dummyEntityItem.setEntityItemStack(stack);
		customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);

		GL11.glPopMatrix();
	}
	
	private void doRenderRobot(float factor, TextureManager texManager) {
		box.render(factor);

		GL11.glPushMatrix();
		texManager.bindTexture(overlay);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_ALPHA_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
		GL11.glDepthMask(true);
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680, 0);
		box.render(factor);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}
}
