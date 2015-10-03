package buildcraft.transport.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.Position;
import buildcraft.api.items.IItemCustomPipeRender;
import buildcraft.core.lib.render.RenderEntityBlock;
import buildcraft.core.lib.render.RenderUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;

public class PipeTransportItemsRenderer extends PipeTransportRenderer<PipeTransportItems> {
	private static final EntityItem dummyEntityItem = new EntityItem(null);
	private static final RenderItem customRenderItem;
	private static final int MAX_ITEMS_TO_RENDER = 10;

	static {
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

	private int getItemLightLevel(ItemStack stack) {
		if (stack.getItem() instanceof ItemBlock) {
			Block b = Block.getBlockFromItem(stack.getItem());
			return b.getLightValue();
		}
		return 0;
	}

	public void doRenderItem(TravelingItem travellingItem, double x, double y, double z, float light, EnumColor color) {
		if (travellingItem == null || travellingItem.getItemStack() == null) {
			return;
		}

		float renderScale = 0.7f;
		ItemStack itemstack = travellingItem.getItemStack();

		GL11.glPushMatrix();
		GL11.glTranslatef((float) x, (float) y + 0.25F, (float) z);
		GL11.glEnable(GL11.GL_ALPHA_TEST);

		//OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, skylight << 4, Math.max(blocklight, getItemLightLevel(itemstack)) << 4);

		if (travellingItem.hasDisplayList) {
			GL11.glCallList(travellingItem.displayList);
		} else {
			travellingItem.displayList = GLAllocation.generateDisplayLists(1);
			travellingItem.hasDisplayList = true;

			GL11.glNewList(travellingItem.displayList, GL11.GL_COMPILE_AND_EXECUTE);
			if (itemstack.getItem() instanceof IItemCustomPipeRender) {
				IItemCustomPipeRender render = (IItemCustomPipeRender) itemstack.getItem();
				float itemScale = render.getPipeRenderScale(itemstack);
				GL11.glScalef(renderScale * itemScale, renderScale * itemScale, renderScale * itemScale);
				itemScale = 1 / itemScale;

				if (!render.renderItemInPipe(itemstack, x, y, z)) {
					dummyEntityItem.setEntityItemStack(itemstack);
					customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
				}

				GL11.glScalef(itemScale, itemScale, itemScale);
			} else {
				GL11.glScalef(renderScale, renderScale, renderScale);
				dummyEntityItem.setEntityItemStack(itemstack);
				customRenderItem.doRender(dummyEntityItem, 0, 0, 0, 0, 0);
			}
			GL11.glEndList();
		}

		if (color != null) {
			bindTexture(TextureMap.locationBlocksTexture);
			RenderEntityBlock.RenderInfo block = new RenderEntityBlock.RenderInfo();

			block.texture = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.ItemBox.ordinal());

			float pix = 0.0625F;
			float min = -4 * pix;
			float max = 4 * pix;

			block.minY = min;
			block.maxY = max;

			block.minZ = min;
			block.maxZ = max;

			block.minX = min;
			block.maxX = max;

			RenderUtils.setGLColorFromInt(color.getLightHex());
			RenderEntityBlock.INSTANCE.renderBlock(block);
		}

		GL11.glPopMatrix();
	}

	@Override
	public void render(Pipe<PipeTransportItems> pipe, double x, double y, double z, float f) {
		GL11.glPushMatrix();

		int count = 0;
		for (TravelingItem item : pipe.transport.items) {
			if (count >= MAX_ITEMS_TO_RENDER) {
				break;
			}

			Position motion = new Position(0, 0, 0, item.toCenter ? item.input : item.output);
			motion.moveForwards(item.getSpeed() * f);

			doRenderItem(item, x + item.xCoord - pipe.container.xCoord + motion.x, y + item.yCoord - pipe.container.yCoord + motion.y, z + item.zCoord - pipe.container.zCoord + motion.z, 0.0F, item.color);
			count++;
		}

		GL11.glPopMatrix();
	}
}
