/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import buildcraft.BuildCraftCore;
import buildcraft.core.StackAtPosition;
import buildcraft.core.builders.BuildingItem;
import buildcraft.core.builders.IBuildingItemsProvider;

public class RenderBuildingItems {

	private final EntityItem dummyEntityItem = new EntityItem(null);
	private final RenderItem customRenderItem;

	// optimization - buildToolBlocks are the most often appearing ones
	private Item buildToolItem;
	private int buildToolGlList;

	public RenderBuildingItems() {
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
	}

	public void render(TileEntity tile, double x, double y, double z) {
		IBuildingItemsProvider provider = (IBuildingItemsProvider) tile;
		GL11.glPushMatrix();

		GL11.glTranslated(x, y, z);
		GL11.glTranslated(-tile.xCoord, -tile.yCoord, -tile.zCoord);

		if (provider.getBuilders() != null) {
			for (BuildingItem i : provider.getBuilders()) {
				doRenderItem(i, 1.0F);
			}
		}

		GL11.glPopMatrix();
	}

	public void renderToList(ItemStack stack, int list) {
		GL11.glNewList(list, GL11.GL_COMPILE_AND_EXECUTE);

		float renderScale = 0.7f;
		GL11.glScalef(renderScale, renderScale, renderScale);
		dummyEntityItem.setEntityItemStack(stack);
		customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
		GL11.glEndList();
	}

	private void doRenderItem(BuildingItem i, float light) {
		if (buildToolItem == null) {
			buildToolItem = Item.getItemFromBlock(BuildCraftCore.buildToolBlock);
			buildToolGlList = GL11.glGenLists(1);
			renderToList(new ItemStack(buildToolItem), buildToolGlList);
		}
		if (i == null) {
			return;
		}

		i.displayUpdate();

		for (StackAtPosition s : i.getStacks()) {
			if (s.display) {
				GL11.glPushMatrix();
				GL11.glTranslatef((float) s.pos.x, (float) s.pos.y + 0.25F,
						(float) s.pos.z);

				if (s.stack.getItem() == buildToolItem) {
					GL11.glCallList(buildToolGlList);
				} else if (!s.generatedListId) {
					s.generatedListId = true;
					s.glListId = GL11.glGenLists(1);
					renderToList(s.stack, s.glListId);
				} else {
					GL11.glCallList(s.glListId);
				}

				GL11.glPopMatrix();
			}
		}
	}

}
