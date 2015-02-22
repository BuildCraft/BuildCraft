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
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import buildcraft.BuildCraftSilicon;
import buildcraft.core.DefaultProps;
import buildcraft.core.EntityLaser;
import buildcraft.core.ItemRobot;
import buildcraft.core.robots.EntityRobot;

public class RenderRobot extends Render /*implements IItemRenderer*/ {

	private static final ResourceLocation overlay = new ResourceLocation("buildcraft",
			DefaultProps.TEXTURE_PATH_ENTITIES + "/robot_overlay.png");
	
	private final EntityItem dummyEntityItem = new EntityItem(null);
	private final RenderItem customRenderItem;

	private ModelBase model = new ModelBase() {
	};
	private ModelRenderer box;

	public RenderRobot() {
		super(Minecraft.getMinecraft().getRenderManager());
		customRenderItem = Minecraft.getMinecraft().getRenderItem();
		
		box = new ModelRenderer(model, 0, 0);
		box.addBox(-4F, -4F, -4F, 8, 8, 8);
		box.rotationPointX = 0;
		box.rotationPointY = 0;
		box.rotationPointZ = 0;
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float f, float f1) {
		doRender((EntityRobot) entity, x, y, z, f);
	}

	private void doRender(EntityRobot robot, double x, double y, double z, float light) {
		GL11.glPushMatrix();
		GL11.glTranslated(x, y, z);

		if (robot.getStackInSlot(0) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.125F, 0, -0.125F);
			doRenderItem(robot.getStackInSlot(0), light);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(1) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(+0.125F, 0, -0.125F);
			doRenderItem(robot.getStackInSlot(1), light);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(2) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(+0.125F, 0, +0.125F);
			doRenderItem(robot.getStackInSlot(2), light);
			GL11.glPopMatrix();
		}

		if (robot.getStackInSlot(3) != null) {
			GL11.glPushMatrix();
			GL11.glTranslatef(-0.125F, 0, +0.125F);
			doRenderItem(robot.getStackInSlot(3), light);
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

			//TODO fix this back
			/*if (itemstack1.getItem().requiresMultipleRenderPasses()) {
				for (int k = 0; k < itemstack1.getItem().getRenderPasses(itemstack1.getItemDamage()); ++k) {
					RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, k));
					this.renderManager.itemRenderer.renderItem(robot, itemstack1, k);
				}
			} else {*/
				RenderUtils.setGLColorFromInt(itemstack1.getItem().getColorFromItemStack(itemstack1, 0));
				Minecraft.getMinecraft().getItemRenderer().renderItem(robot, itemstack1, TransformType.NONE);
			//}

			GL11.glColor3f(1, 1, 1);
			GL11.glPopMatrix();
		}

		if (robot.laser.isVisible) {
			robot.laser.head.x = robot.posX;
			robot.laser.head.y = robot.posY;
			robot.laser.head.z = robot.posZ;

			RenderLaser.doRenderLaser(robot.worldObj, renderManager.renderEngine, robot.laser, EntityLaser.LASER_TEXTURES [1]);
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

	/*@Override
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
	}*/

	/**
	 * This is a refactor from the code of RenderPlayer.
	 *
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

				/*GL11.glTranslatef(0.0F, 0.1875F, 0.0F);
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
	}*/

	private void doRenderItem(ItemStack stack, float light) {
		float renderScale = 0.5f;
		GL11.glPushMatrix();
		GL11.glTranslatef(0, 0.28F, 0);
		GL11.glScalef(renderScale, renderScale, renderScale);
		//dummyEntityItem.setEntityItemStack(stack);
		customRenderItem.renderItemModel(stack);

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
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 61680 % 65536, 61680 / 65536);
		box.render(factor);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}
}
