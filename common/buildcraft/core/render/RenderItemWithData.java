package buildcraft.core.render;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import org.lwjgl.opengl.GL11;

public abstract class RenderItemWithData<T extends TileEntity> implements IItemRenderer{

	private final T renderTile = createFantomTile();
	private final RenderBlocks renderBlocks = new RenderBlocks();

	protected abstract T createFantomTile();

	protected abstract Block getBlockToRender();

	protected abstract void resetTile(T tile);

	@Override
	public boolean handleRenderType(ItemStack is, ItemRenderType type){
		return type != ItemRenderType.FIRST_PERSON_MAP;
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack is, ItemRendererHelper helper){
		return true;
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack is, Object... data){
		if (is.hasTagCompound() && is.getTagCompound().hasKey("tileData", 10)) {
			renderTile.readFromNBT(is.getTagCompound().getCompoundTag("tileData"));
		} else {
			resetTile(renderTile);
		}
		double shift = 0.0;
		if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			shift = 0.5;
		}
		GL11.glPushMatrix();
		TileEntityRendererDispatcher.instance.renderTileEntityAt(renderTile, shift - 0.5, shift - 0.5, shift - 0.5, 1.0F);
		Block block = getBlockToRender();
		if (block != null) {
			GL11.glTranslated(shift, shift, shift);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			renderBlocks.renderBlockAsItem(block, is.getItemDamage(), 1.0F);
			GL11.glDepthMask(true);
			GL11.glDisable(GL11.GL_BLEND);
		}
		GL11.glPopMatrix();
	}
}