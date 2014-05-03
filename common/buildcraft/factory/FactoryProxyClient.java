/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import java.lang.reflect.Method;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

import buildcraft.BuildCraftFactory;
import buildcraft.api.core.BCLog;
import buildcraft.builders.RenderBuilder;
import buildcraft.core.EntityBlock;
import buildcraft.core.render.RenderVoid;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingEntityBlocks.EntityRenderIndex;
import buildcraft.factory.gui.GuiAutoCrafting;
import buildcraft.factory.render.RenderHopper;
import buildcraft.factory.render.RenderRefinery;
import buildcraft.factory.render.RenderTank;

public class FactoryProxyClient extends FactoryProxy {

	public static IIcon pumpTexture;
	public static IIcon drillTexture;
	public static IIcon drillHeadTexture;

	@Override
	public void initializeTileEntities() {
		super.initializeTileEntities();

		if (BuildCraftFactory.tankBlock != null) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileTank.class, new RenderTank());
		}

		if (BuildCraftFactory.refineryBlock != null) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileRefinery.class, new RenderRefinery());
			RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftFactory.refineryBlock, 0), new RenderRefinery());
		}

		if (BuildCraftFactory.hopperBlock != null) {
			ClientRegistry.bindTileEntitySpecialRenderer(TileHopper.class, new RenderHopper());
			RenderingEntityBlocks.blockByEntityRenders.put(new EntityRenderIndex(BuildCraftFactory.hopperBlock, 0), new RenderHopper());
		}

		ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, new RenderBuilder());
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
			BCLog.logger.fine("NEI detected, adding NEI overlay");
		} catch (Exception e) {
			BCLog.logger.fine("NEI not detected.");
		}
	}

	@Override
	public EntityBlock newPumpTube(World w) {
		EntityBlock eb = super.newPumpTube(w);
		eb.texture = pumpTexture;
		return eb;
	}

	@Override
	public EntityBlock newDrill(World w, double i, double j, double k, double l, double d, double e) {
		EntityBlock eb = super.newDrill(w, i, j, k, l, d, e);
		eb.texture = drillTexture;
		return eb;
	}

	@Override
	public EntityBlock newDrillHead(World w, double i, double j, double k, double l, double d, double e) {
		EntityBlock eb = super.newDrillHead(w, i, j, k, l, d, e);
		eb.texture = drillHeadTexture;
		return eb;
	}
}
