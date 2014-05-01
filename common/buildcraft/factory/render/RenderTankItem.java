package buildcraft.factory.render;

import buildcraft.core.render.RenderItemWithData;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraftforge.client.IItemRenderer;
import buildcraft.BuildCraftFactory;
import net.minecraft.item.ItemStack;
import buildcraft.factory.TileTank;
import org.lwjgl.opengl.GL11;

public class RenderTankItem extends RenderItemWithData<TileTank>{

	@Override
	protected TileTank createFantomTile() {
		return new TileTank();
	}

	@Override
	protected Block getBlockToRender() {
		return BuildCraftFactory.tankBlock;
	}

	@Override
	protected boolean doesRenderEmptyTile(){
		return false;
	}
}
