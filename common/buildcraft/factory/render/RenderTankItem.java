package buildcraft.factory.render;

import buildcraft.core.render.RenderItemWithData;
import buildcraft.BuildCraftFactory;
import buildcraft.factory.TileTank;
import net.minecraft.block.Block;

public class RenderTankItem extends RenderItemWithData<TileTank>{

	@Override
	protected TileTank createFantomTile(){
		return new TileTank();
	}

	@Override
	protected Block getBlockToRender(){
		return BuildCraftFactory.tankBlock;
	}

	@Override
	protected boolean shouldRenderEmptyTile() {
		return false;
	}
}