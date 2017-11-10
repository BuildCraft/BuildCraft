/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.factory;

import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.client.registry.ClientRegistry;

import buildcraft.BuildCraftFactory;
import buildcraft.core.lib.EntityBlock;
import buildcraft.core.render.RenderLEDTile;
import buildcraft.core.render.RenderingEntityBlocks;
import buildcraft.core.render.RenderingEntityBlocks.EntityRenderIndex;
import buildcraft.factory.render.RenderHopper;
import buildcraft.factory.render.RenderRefinery;
import buildcraft.factory.render.RenderTank;

public class FactoryProxyClient extends FactoryProxy {
	public static IIcon pumpTexture;

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

		ClientRegistry.bindTileEntitySpecialRenderer(TileMiningWell.class, new RenderLEDTile(BuildCraftFactory.miningWellBlock));
		ClientRegistry.bindTileEntitySpecialRenderer(TilePump.class, new RenderLEDTile(BuildCraftFactory.pumpBlock));
	}

	@Override
	public void initializeEntityRenders() {
	}

	@Override
	public EntityBlock newPumpTube(World w) {
		EntityBlock eb = super.newPumpTube(w);
		eb.setTexture(pumpTexture);
		return eb;
	}
}
