/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import net.minecraftforge.client.IItemRenderer;

import buildcraft.BuildCraftTransport;
import buildcraft.core.lib.render.FakeBlock;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.transport.PipeIconProvider;

public class RobotStationItemRenderer implements IItemRenderer {
	private void renderPlugItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {
		FakeBlock block = FakeBlock.INSTANCE;
		Tessellator tessellator = Tessellator.instance;
		IIcon textureID = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeRobotStation.ordinal()); // Structure pipe

		GL11.glTranslatef(translateX, translateY, translateZ + 0.25F);

		block.setBlockBounds(0.25F, 0.1875F, 0.25F, 0.75F, 0.25F, 0.75F);
		render.setRenderBoundsFromBlock(block);
		RenderUtils.drawBlockItem(render, tessellator, block, textureID);

		block.setBlockBounds(0.4325F, 0.25F, 0.4325F, 0.5675F, 0.4375F, 0.5675F);
		render.setRenderBoundsFromBlock(block);
		RenderUtils.drawBlockItem(render, tessellator, block, textureID);
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		switch (type) {
			case ENTITY:
				return true;
			case EQUIPPED:
				return true;
			case EQUIPPED_FIRST_PERSON:
				return true;
			case INVENTORY:
				return true;
			default:
				return false;
		}
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		return helper != ItemRendererHelper.BLOCK_3D;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		switch (type) {
			case ENTITY:
				GL11.glScalef(0.50F, 0.50F, 0.50F);
				renderPlugItem((RenderBlocks) data[0], item, -0.6F, 0f, -0.6F);
				break;
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
				GL11.glRotatef(70, 0, 0, 1F);
				GL11.glRotatef(-55, 1, 0, 0);
				GL11.glScalef(2F, 2F, 2F);
				GL11.glTranslatef(0, -0.6F, -0.4F);
				renderPlugItem((RenderBlocks) data[0], item, 0F, 0F, 0f);
				break;
			case INVENTORY:
				GL11.glScalef(1.1F, 1.1F, 1.1F);
				renderPlugItem((RenderBlocks) data[0], item, -0.3f, -0.35f, -0.7f);
				break;
			default:
		}
	}
}
