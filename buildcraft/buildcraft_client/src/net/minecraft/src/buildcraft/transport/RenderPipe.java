package net.minecraft.src.buildcraft.transport;

import org.lwjgl.opengl.GL11;

import net.minecraft.src.Block;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.GLAllocation;
import net.minecraft.src.ItemStack;
import net.minecraft.src.RenderBlocks;
import net.minecraft.src.RenderManager;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TileEntity;
import net.minecraft.src.TileEntitySpecialRenderer;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.transport.PipeTransportItems.EntityData;
import net.minecraft.src.forge.ITextureProvider;
import net.minecraft.src.forge.MinecraftForgeClient;

public class RenderPipe extends TileEntitySpecialRenderer {
	private RenderBlocks renderBlocks;

	public RenderPipe() {
		renderBlocks = new RenderBlocks();
	}

	@Override
	public void renderTileEntityAt(TileEntity tileentity, double x, double y,
			double z, float f) {

		TileGenericPipe pipe = ((TileGenericPipe) tileentity);

		if (pipe.pipe.transport instanceof PipeTransport) {
			renderSolids(pipe.pipe, x, y, z);
		}
	}

	private void renderSolids(Pipe pipe, double x, double y, double z) {
		GL11.glPushMatrix();
		GL11.glDisable(2896 /* GL_LIGHTING */);

		for (EntityData data : ((PipeTransportItems) pipe.transport).travelingEntities
				.values()) {
			doRenderItem(data.item, x + data.item.posX - pipe.xCoord, y
					+ data.item.posY - pipe.yCoord, z + data.item.posZ
					- pipe.zCoord, pipe.worldObj.getLightBrightness(
					pipe.xCoord, pipe.yCoord, pipe.zCoord));
		}

		GL11.glEnable(2896 /* GL_LIGHTING */);
		GL11.glPopMatrix();
	}

	private void doRenderItem(EntityPassiveItem entityitem, double x, double y,
			double z, double brigntess) {
		if (entityitem == null || entityitem.item == null) {
			return;
		}

		ItemStack itemstack = entityitem.item;

		GL11.glPushMatrix();

		byte byte0 = 1;
		if (entityitem.item.stackSize > 1) {
			byte0 = 2;
		}
		if (entityitem.item.stackSize > 5) {
			byte0 = 3;
		}
		if (entityitem.item.stackSize > 20) {
			byte0 = 4;
		}

		GL11.glTranslatef((float) x, (float) y, (float) z);

		GL11.glEnable(32826 /* GL_RESCALE_NORMAL_EXT */);

		if (itemstack.itemID < Block.blocksList.length
				&& itemstack.itemID > 0
				&& RenderBlocks
						.renderItemIn3d(Block.blocksList[itemstack.itemID]
								.getRenderType())) {
			GL11.glTranslatef(0, 0.25F, 0);
			Block block = Block.blocksList[itemstack.itemID];

			if (block instanceof ITextureProvider) {
				MinecraftForgeClient.bindTexture(((ITextureProvider) block)
						.getTextureFile());
			} else {
				MinecraftForgeClient.bindTexture("/terrain.png");
			}

			float f4 = 0.25F;
			if (!Block.blocksList[itemstack.itemID].renderAsNormalBlock()
					&& itemstack.itemID != Block.stairSingle.blockID) {
				f4 = 0.5F;
			}
			GL11.glScalef(f4, f4, f4);
			for (int j = 0; j < byte0; j++) {
				GL11.glPushMatrix();
				renderBlocks.renderBlockOnInventory(
						Block.blocksList[itemstack.itemID],
						itemstack.getItemDamage(), (float) brigntess);
				GL11.glPopMatrix();
			}

		} else {
			GL11.glTranslatef(0, 0.10F, 0);
			GL11.glScalef(0.5F, 0.5F, 0.5F);
			int i = itemstack.getIconIndex();
			if (itemstack.getItem() instanceof ITextureProvider) {
				MinecraftForgeClient.bindTexture(((ITextureProvider) itemstack
						.getItem()).getTextureFile());
			} else if (itemstack.itemID < 256) {
				MinecraftForgeClient.bindTexture("/terrain.png");
			} else {
				MinecraftForgeClient.bindTexture("/gui/items.png");
			}
			Tessellator tessellator = Tessellator.instance;
			float f6 = (float) ((i % 16) * 16 + 0) / 256F;
			float f8 = (float) ((i % 16) * 16 + 16) / 256F;
			float f10 = (float) ((i / 16) * 16 + 0) / 256F;
			float f11 = (float) ((i / 16) * 16 + 16) / 256F;
			float f12 = 1.0F;
			float f13 = 0.5F;
			float f14 = 0.25F;
			for (int k = 0; k < byte0; k++) {
				GL11.glPushMatrix();

				GL11.glRotatef(180F - RenderManager.instance.playerViewY, 0.0F,
						1.0F, 0.0F);
				tessellator.startDrawingQuads();
				tessellator.setNormal(0.0F, 1.0F, 0.0F);
				tessellator.addVertexWithUV(0.0F - f13, 0.0F - f14, 0.0D, f6,
						f11);
				tessellator.addVertexWithUV(f12 - f13, 0.0F - f14, 0.0D, f8,
						f11);
				tessellator.addVertexWithUV(f12 - f13, 1.0F - f14, 0.0D, f8,
						f10);
				tessellator.addVertexWithUV(0.0F - f13, 1.0F - f14, 0.0D, f6,
						f10);
				tessellator.draw();
				GL11.glPopMatrix();
			}

		}
		GL11.glDisable(32826 /* GL_RESCALE_NORMAL_EXT */);
		GL11.glPopMatrix();
	}
}
