package buildcraft.factory;

import java.lang.reflect.Method;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftFactory;
import buildcraft.core.render.RenderVoid;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingEntityBlocks.EntityRenderIndex;
import buildcraft.factory.gui.GuiAutoCrafting;
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

		if (!BuildCraftFactory.hopperDisabled) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileHopper.class, new RenderHopper());
			RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftFactory.hopperBlock, 0), new RenderHopper());
		}

	}

	@Override
	public void initializeEntityRenders() {
		RenderingRegistry.registerEntityRenderingHandler(EntityMechanicalArm.class, new RenderVoid());
	}

	@Override
	public void initializeNEIIntegration() {
		try {
			Class<?> neiRenderer = Class.forName("codechicken.nei.DefaultOverlayRenderer");
			Method method = neiRenderer.getMethod("registerGuiOverlay", Class.class, String.class, int.class, int.class);
			method.invoke(null, GuiAutoCrafting.class, "crafting", 5, 11);
			BuildCraftCore.bcLog.fine("NEI detected, adding NEI overlay");
		} catch (Exception e) {
			BuildCraftCore.bcLog.fine("NEI not detected.");
		}
	}
}
