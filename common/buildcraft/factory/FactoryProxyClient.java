package buildcraft.factory;

import buildcraft.BuildCraftFactory;
import buildcraft.core.render.RenderVoid;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingEntityBlocks.EntityRenderIndex;
import buildcraft.factory.render.RenderHopper;
import buildcraft.factory.render.RenderRefinery;
import buildcraft.factory.render.RenderTank;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

public class FactoryProxyClient extends FactoryProxy {
	@Override
	public void initializeTileEntities() {
		super.initializeTileEntities();
		ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
		ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, new RenderRefinery());
		RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftFactory.refineryBlock, 0), new RenderRefinery());

		if(!BuildCraftFactory.hopperDisabled) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileHopper.class, new RenderHopper());
			RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftFactory.hopperBlock, 0), new RenderHopper());
		}

	}

	@Override
	public void initializeEntityRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityMechanicalArm.class, new RenderVoid());
	}
}
