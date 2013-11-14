package buildcraft.transport.render;

import buildcraft.BuildCraftTransport;
import buildcraft.core.CoreConstants;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.PipeIconProvider;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraftforge.client.IItemRenderer;
import static net.minecraftforge.client.IItemRenderer.ItemRenderType.EQUIPPED_FIRST_PERSON;
import org.lwjgl.opengl.GL11;

public class FacadeItemRenderer implements IItemRenderer {

	private void renderFacadeItem(RenderBlocks render, ItemStack item, float translateX, float translateY, float translateZ) {

		int decodedMeta = ItemFacade.getMetaData(item);
		int decodedBlockId = ItemFacade.getBlockId(item);

		try {
			int color = Item.itemsList[decodedBlockId].getColorFromItemStack(new ItemStack(decodedBlockId, 1, decodedMeta), 0);
			float r = (float) (color >> 16 & 0xff) / 255F;
			float g = (float) (color >> 8 & 0xff) / 255F;
			float b = (float) (color & 0xff) / 255F;
			GL11.glColor4f(r, g, b, 1.0F);
		} catch (Throwable error) {
		}

		Tessellator tessellator = Tessellator.instance;

		Block block = Block.blocksList[decodedBlockId];
		if (block == null)
			return;

		// Render Facade
		GL11.glPushMatrix();
		block.setBlockBounds(0F, 0F, 1F - 1F / 16F, 1F, 1F, 1F);
		render.setRenderBoundsFromBlock(block);
		GL11.glTranslatef(translateX, translateY, translateZ);

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -1F, 0.0F);
		render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(0, decodedMeta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(1, decodedMeta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(2, decodedMeta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(3, decodedMeta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, block.getIcon(4, decodedMeta));
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, block.getIcon(5, decodedMeta));
		tessellator.draw();
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		GL11.glPopMatrix();

		// Render StructurePipe
		block = BuildCraftTransport.genericPipeBlock;
		Icon textureID = BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal()); // Structure pipe

		block.setBlockBounds(CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS - 1F / 16F);
		block.setBlockBoundsForItemRender();
		render.setRenderBoundsFromBlock(block);
		GL11.glTranslatef(translateX, translateY, translateZ + 0.25F);

		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, -0F, 0.0F);
		render.renderFaceYNeg(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 1.0F, 0.0F);
		render.renderFaceYPos(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, -1F);
		render.renderFaceZNeg(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(0.0F, 0.0F, 1.0F);
		render.renderFaceZPos(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(-1F, 0.0F, 0.0F);
		render.renderFaceXNeg(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		tessellator.startDrawingQuads();
		tessellator.setNormal(1.0F, 0.0F, 0.0F);
		render.renderFaceXPos(block, 0.0D, 0.0D, 0.0D, textureID);
		tessellator.draw();
		GL11.glTranslatef(0.5F, 0.5F, 0.5F);
		block.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
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
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {

		switch (type) {
			case ENTITY:
				GL11.glScalef(0.50F, 0.50F, 0.50F);
				renderFacadeItem((RenderBlocks) data[0], item, -0.6F, 0f, -0.6F);
				break;
			case EQUIPPED:
			case EQUIPPED_FIRST_PERSON:
				renderFacadeItem((RenderBlocks) data[0], item, 0F, 0F, 0f);
				break;
			case INVENTORY:
				GL11.glScalef(1.1F, 1.1F, 1.1F);
				renderFacadeItem((RenderBlocks) data[0], item, -0.3f, -0.35f, -0.7f);
				break;
			default:
		}
	}
}
