/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

import java.util.Date;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.IItemRenderer;

import buildcraft.BuildCraftSilicon;
import buildcraft.core.EntityLaser;
import buildcraft.core.ItemRobot;
import buildcraft.core.robots.EntityRobot;

public class RenderRobot extends Render implements IItemRenderer {

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
		doRender((EntityRobot) entity, x, y, z, f, f1);
	}

	private void doRender(EntityRobot robot, double x, double y, double z, float f, float f1) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);
		
		try {
			renderManager.renderEngine.bindTexture(robot.getTexture());
		} catch (Exception e) {
			e.printStackTrace();
			// TODO: Figure out why the NPE inside Minecraft happens.
			GL11.glPopMatrix();
			return;
		}
		float factor = (float) (1.0 / 16.0);

		box.render(factor);

		if (robot.getStackInSlot(0) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.125F, 0, -0.125F);
			doRenderItem(robot.getStackInSlot(0), 1.0F);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(1) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(+0.125F, 0, -0.125F);
			doRenderItem(robot.getStackInSlot(1), 1.0F);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(2) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(+0.125F, 0, +0.125F);
			doRenderItem(robot.getStackInSlot(2), 1.0F);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(3) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.125F, 0, +0.125F);
			doRenderItem(robot.getStackInSlot(3), 1.0F);
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
					int i = itemstack1.getItem().getColorFromItemStack(itemstack1, k);
					float f12 = (i >> 16 & 255) / 255.0F;
					float f4 = (i >> 8 & 255) / 255.0F;
					float f5 = (i & 255) / 255.0F;
					GL11.glColor4f(f12, f4, f5, 1.0F);
					this.renderManager.itemRenderer.renderItem(robot, itemstack1, k);
				}
			} else {
				int k = itemstack1.getItem().getColorFromItemStack(itemstack1, 0);
				float f11 = (k >> 16 & 255) / 255.0F;
				float f12 = (k >> 8 & 255) / 255.0F;
				float f4 = (k & 255) / 255.0F;
				GL11.glColor4f(f11, f12, f4, 1.0F);
				this.renderManager.itemRenderer.renderItem(robot, itemstack1, 0);
			}

			GL11.glPopMatrix();
		}

		if (robot.laser.isVisible) {
			robot.laser.head.x = robot.posX;
			robot.laser.head.y = robot.posY;
			robot.laser.head.z = robot.posZ;

			RenderLaser.doRenderLaser(renderManager.renderEngine, robot.laser, EntityLaser.LASER_TEXTURES [1]);
		} else {

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
			GL11.glTranslated(-0.0, 1.0, 0.7);
		} else if (type == ItemRenderType.ENTITY) {
			GL11.glScaled(0.6, 0.6, 0.6);
		} else if (type == ItemRenderType.INVENTORY) {
			GL11.glScaled(1.5, 1.5, 1.5);
		}

		box.render(1F / 16F);

		GL11.glPopMatrix();
	}

	private void doRenderItem(ItemStack stack, float light) {
		float renderScale = 0.5f;
		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0.28F, 0);
		GL11.glScalef(renderScale, renderScale, renderScale);
		dummyEntityItem.setEntityItemStack(stack);
		customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);

		GL11.glPopMatrix();

	}

}
