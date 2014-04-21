package buildcraft.core.render;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.IItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;

import static org.lwjgl.opengl.GL11.*;

public abstract class RenderItemWithData<T extends TileEntity> implements IItemRenderer{

	private final T renderTile = createFantomTile();
	private final RenderBlocks renderBlocks = new RenderBlocks();

	protected abstract T createFantomTile();

	protected abstract Block getBlockToRender();

	protected void resetTile(T tile) {}

	protected boolean shouldRenderEmptyTile() {
		return true;
	}

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
		boolean flag = is.hasTagCompound() && is.getTagCompound().hasKey("tileData", 10);
		double shift = 0.0;
		if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			shift = 0.5;
		}
		if (flag) {
			renderTile.readFromNBT(is.getTagCompound().getCompoundTag("tileData"));
		} else if (shouldRenderEmptyTile()){
			resetTile(renderTile);
		}
		if (flag || shouldRenderEmptyTile()) {
			TileEntityRendererDispatcher.instance
					.renderTileEntityAt(renderTile, shift - 0.5, shift - 0.5, shift - 0.5, 1.0F);
		}
		Block block = getBlockToRender();
		if (block != null) {
			glPushMatrix();
			glTranslated(shift, shift, shift);
			glEnable(GL_BLEND);
			glDepthMask(false);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			renderBlocks.renderBlockAsItem(block, is.getItemDamage(), 1.0F);
			glDepthMask(true);
			glDisable(GL_BLEND);
			glPopMatrix();
		}
	}
}