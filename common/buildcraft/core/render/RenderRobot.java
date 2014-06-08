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

import net.minecraft.block.Block;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;

import buildcraft.BuildCraftSilicon;
import buildcraft.core.EntityLaser;
import buildcraft.core.ItemRobot;
import buildcraft.core.robots.EntityRobot;

public class RenderRobot extends Render implements IItemRenderer {

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

		GL11.glEnable(GL11.GL_LIGHTING);
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

		if (item.getItem() == BuildCraftSilicon.robotItem) {
			ItemRobot robot = (ItemRobot) item.getItem();
			RenderManager.instance.renderEngine.bindTexture(robot.getTextureRobot(item));
		}

		float factor = (float) (1.0 / 16.0);

		if (type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			GL11.glTranslated(0.25F, 0.5F, 0);
		}

		box.render(1F / 16F);

		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}

	/**
	 * This is a refactor from the code of RenderPlayer.
	 */
	public void doRenderItemAtHand(EntityRobot robot, ItemStack currentItem) {
		ItemStack itemstack1 = currentItem;
		float f3, f5;

		if (itemstack1 != null) {
			GL11.glPushMatrix();
			// this.modelBipedMain.bipedRightArm.postRender(0.0625F);
			GL11.glTranslatef(-0.0625F, 0.4375F, 0.0625F);

			// if (par1AbstractClientPlayer.fishEntity != null)
			// {
			// itemstack1 = new ItemStack(Items.stick);
			// }

			EnumAction enumaction = EnumAction.none;

			// if (par1AbstractClientPlayer.getItemInUseCount() > 0)
			// {
			// enumaction = itemstack1.getItemUseAction();
			// }

			IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer(itemstack1, ItemRenderType.EQUIPPED);
			boolean is3D = customRenderer != null && customRenderer.shouldUseRenderHelper(ItemRenderType.EQUIPPED,
					itemstack1,
					ItemRendererHelper.BLOCK_3D);

			if (is3D || itemstack1.getItem() instanceof ItemBlock
					&& RenderBlocks.renderItemIn3d(Block.getBlockFromItem(itemstack1.getItem()).getRenderType())) {
				f3 = 0.5F;
				GL11.glTranslatef(0.0F, 0.1875F, -0.3125F);
				f3 *= 0.75F;
				GL11.glRotatef(20.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(-f3, -f3, f3);
			} else if (itemstack1.getItem() == Items.bow) {
				f3 = 0.625F;
				GL11.glTranslatef(0.0F, 0.125F, 0.3125F);
				GL11.glRotatef(-20.0F, 0.0F, 1.0F, 0.0F);
				GL11.glScalef(f3, -f3, f3);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else if (itemstack1.getItem().isFull3D()) {
				f3 = 0.625F;

				if (itemstack1.getItem().shouldRotateAroundWhenRendering()) {
					GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
					GL11.glTranslatef(0.0F, -0.125F, 0.0F);
				}

				/*
				 * if (par1AbstractClientPlayer.getItemInUseCount() > 0 &&
				 * enumaction == EnumAction.block) { GL11.glTranslatef(0.05F,
				 * 0.0F, -0.1F); GL11.glRotatef(-50.0F, 0.0F, 1.0F, 0.0F);
				 * GL11.glRotatef(-10.0F, 1.0F, 0.0F, 0.0F);
				 * GL11.glRotatef(-60.0F, 0.0F, 0.0F, 1.0F); }
				 */

				GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
				GL11.glScalef(f3, -f3, f3);
				GL11.glRotatef(-100.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
			} else {
				f3 = 0.375F;
				GL11.glTranslatef(0.25F, 0.1875F, -0.1875F);
				GL11.glScalef(f3, f3, f3);
				GL11.glRotatef(60.0F, 0.0F, 0.0F, 1.0F);
				GL11.glRotatef(-90.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(20.0F, 0.0F, 0.0F, 1.0F);
			}

			float f4;
			int k;
			float f12;

			if (itemstack1.getItem().requiresMultipleRenderPasses()) {
				for (k = 0; k < itemstack1.getItem().getRenderPasses(itemstack1.getItemDamage()); ++k) {
					int i = itemstack1.getItem().getColorFromItemStack(itemstack1, k);
					f12 = (i >> 16 & 255) / 255.0F;
					f4 = (i >> 8 & 255) / 255.0F;
					f5 = (i & 255) / 255.0F;
					GL11.glColor4f(f12, f4, f5, 1.0F);
					this.renderManager.itemRenderer.renderItem(robot, itemstack1, k);
				}
			} else {
				k = itemstack1.getItem().getColorFromItemStack(itemstack1, 0);
				float f11 = (k >> 16 & 255) / 255.0F;
				f12 = (k >> 8 & 255) / 255.0F;
				f4 = (k & 255) / 255.0F;
				GL11.glColor4f(f11, f12, f4, 1.0F);
				this.renderManager.itemRenderer.renderItem(robot, itemstack1, 0);
			}

			GL11.glPopMatrix();
		}
	}

}
