/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.render;

import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import net.minecraftforge.client.IItemRenderer;

import buildcraft.api.gates.IGateExpansion;
import buildcraft.transport.gates.ItemGate;

public class GateItemRenderer implements IItemRenderer {

	RenderItem renderItem = new RenderItem();

	@Override
	public boolean handleRenderType(ItemStack stack, ItemRenderType type) {
		return type == ItemRenderType.INVENTORY
				|| type == ItemRenderType.ENTITY
				|| type == ItemRenderType.EQUIPPED
				|| type == ItemRenderType.EQUIPPED_FIRST_PERSON;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack stack, ItemRendererHelper helper) {
		return helper == ItemRendererHelper.ENTITY_BOBBING;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack stack, Object... data) {
		if (type == ItemRenderType.INVENTORY) {
			render(ItemRenderType.INVENTORY, stack);
		} else if (type == ItemRenderType.ENTITY) {
			if (RenderManager.instance.options.fancyGraphics) {
				renderAsEntity(stack, (EntityItem) data[1]);
			} else {
				renderAsEntityFlat(stack);
			}
		} else if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			renderIn3D(stack);
		}
	}

	private void renderIn3D(ItemStack stack) {
		GL11.glPushMatrix();

		renderLayerIn3D(ItemGate.getLogic(stack).getIconItem());
		GL11.glScalef(1, 1, 1.05f);
		renderLayerIn3D(ItemGate.getMaterial(stack).getIconItem());

		for (IGateExpansion expansion : ItemGate.getInstalledExpansions(stack)) {
			renderLayerIn3D(expansion.getOverlayItem());
		}

		GL11.glPopMatrix();
	}

	private void renderLayerIn3D(IIcon icon) {
		if (icon == null) {
			return;
		}
		GL11.glPushMatrix();
		Tessellator tessellator = Tessellator.instance;

		float uv1 = icon.getMinU();
		float uv2 = icon.getMaxU();
		float uv3 = icon.getMinV();
		float uv4 = icon.getMaxV();

		ItemRenderer.renderItemIn2D(tessellator, uv2, uv3, uv1, uv4, icon.getIconWidth(), icon.getIconHeight(), 0.0625F);
		GL11.glPopMatrix();
	}

	private void renderAsEntity(ItemStack stack, EntityItem entity) {
		GL11.glPushMatrix();
		byte iterations = 1;
		if (stack.stackSize > 1) {
			iterations = 2;
		}
		if (stack.stackSize > 15) {
			iterations = 3;
		}
		if (stack.stackSize > 31) {
			iterations = 4;
		}

		Random rand = new Random(187L);

		float offsetZ = 0.0625F + 0.021875F;

		GL11.glRotatef(((entity.age + 1.0F) / 20.0F + entity.hoverStart) * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
		GL11.glTranslatef(-0.5F, -0.25F, -(offsetZ * iterations / 2.0F));

		for (int count = 0; count < iterations; ++count) {
			if (count > 0) {
				float offsetX = (rand.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
				float offsetY = (rand.nextFloat() * 2.0F - 1.0F) * 0.3F / 0.5F;
				GL11.glTranslatef(offsetX, offsetY, offsetZ);
			} else {
				GL11.glTranslatef(0f, 0f, offsetZ);
			}

			renderIn3D(stack);
		}
		GL11.glPopMatrix();
	}

	private void renderAsEntityFlat(ItemStack stack) {
		GL11.glPushMatrix();
		byte iterations = 1;
		if (stack.stackSize > 1) {
			iterations = 2;
		}
		if (stack.stackSize > 15) {
			iterations = 3;
		}
		if (stack.stackSize > 31) {
			iterations = 4;
		}

		Random rand = new Random(187L);

		for (int ii = 0; ii < iterations; ++ii) {
			GL11.glPushMatrix();
			GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
			GL11.glRotatef(180 - RenderManager.instance.playerViewY, 0.0F, 1.0F, 0.0F);

			if (ii > 0) {
				float var12 = (rand.nextFloat() * 2.0F - 1.0F) * 0.3F;
				float var13 = (rand.nextFloat() * 2.0F - 1.0F) * 0.3F;
				float var14 = (rand.nextFloat() * 2.0F - 1.0F) * 0.3F;
				GL11.glTranslatef(var12, var13, var14);
			}

			GL11.glTranslatef(0.5f, 0.8f, 0);
			GL11.glRotatef(180.0F, 0.0F, 0.0F, 1.0F);
			GL11.glScalef(1f / 16f, 1f / 16f, 1);

			render(ItemRenderType.ENTITY, stack);
			GL11.glPopMatrix();
		}
		GL11.glPopMatrix();
	}

	private void render(ItemRenderType type, ItemStack stack) {
		GL11.glPushMatrix();
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_ALPHA_TEST); // In certain cases gets disabled by this point
		IIcon icon = ItemGate.getLogic(stack).getIconItem();
		renderItem.renderIcon(0, 0, icon, 16, 16);

		if (type == ItemRenderType.ENTITY) {
			GL11.glTranslatef(0, 0, -0.01f);
		}

		icon = ItemGate.getMaterial(stack).getIconItem();
		if (icon != null) {
			renderItem.renderIcon(0, 0, icon, 16, 16);
		}

		for (IGateExpansion expansion : ItemGate.getInstalledExpansions(stack)) {
			icon = expansion.getOverlayItem();
			if (icon != null) {
				renderItem.renderIcon(0, 0, icon, 16, 16);
			}
		}
		GL11.glEnable(GL11.GL_LIGHTING);
		GL11.glPopMatrix();
	}
}
