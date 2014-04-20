package buildcraft.factory.render;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.client.IItemRenderer;
import buildcraft.BuildCraftFactory;
import net.minecraft.item.ItemStack;
import buildcraft.factory.TileTank;
import org.lwjgl.opengl.GL11;

public class RenderTankItem implements IItemRenderer{

	private final TileTank renderTile = new TileTank();
	private final RenderBlocks renderBlocks = new RenderBlocks();

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
			renderTile.tank.setFluid(null);
		}
		double shift = 0.0;
		if (type == ItemRenderType.EQUIPPED || type == ItemRenderType.EQUIPPED_FIRST_PERSON) {
			shift = 0.5;
		}
		GL11.glPushMatrix();
		TileEntityRendererDispatcher.instance.renderTileEntityAt(renderTile, shift - 0.5, shift - 0.5, shift - 0.5, 1.0F);
		GL11.glTranslated(shift, shift, shift);
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDepthMask(false);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		renderBlocks.renderBlockAsItem(BuildCraftFactory.tankBlock, is.getItemDamage(), 1.0F);
		GL11.glDepthMask(true);
		GL11.glDisable(GL11.GL_BLEND);
		GL11.glPopMatrix();
	}
}