/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;

import buildcraft.BuildCraftBuilders;
import buildcraft.builders.render.RenderArchitect;
import buildcraft.builders.render.RenderBuilderTile;
import buildcraft.builders.render.RenderConstructionMarker;
import buildcraft.builders.render.RenderFiller;
import buildcraft.builders.render.RenderFrame;
import buildcraft.core.lib.EntityBlock;
import buildcraft.core.lib.render.RenderMultiTESR;
import buildcraft.core.lib.render.RenderVoid;
import buildcraft.core.render.RenderBuilder;
import buildcraft.core.render.RenderLEDTile;

public class BuilderProxyClient extends BuilderProxy {
	public static IIcon drillTexture, drillSideTexture;
	public static IIcon drillHeadTexture;

	@Override
	public void registerClientHook() {

	}

	@Override
	public void registerBlockRenderers() {
		super.registerBlockRenderers();

		ClientRegistry.bindTileEntitySpecialRenderer(TileBuilder.class, new RenderBuilderTile());
		ClientRegistry.bindTileEntitySpecialRenderer(TileConstructionMarker.class, new RenderConstructionMarker());

		ClientRegistry.bindTileEntitySpecialRenderer(TileFiller.class, new RenderMultiTESR(new TileEntitySpecialRenderer[]{
				new RenderLEDTile(BuildCraftBuilders.fillerBlock),
				new RenderFiller()
		}));

		ClientRegistry.bindTileEntitySpecialRenderer(TileQuarry.class, new RenderMultiTESR(new TileEntitySpecialRenderer[]{
				new RenderLEDTile(BuildCraftBuilders.quarryBlock),
				new RenderBuilder()
		}));

		ClientRegistry.bindTileEntitySpecialRenderer(TileArchitect.class, new RenderMultiTESR(new TileEntitySpecialRenderer[]{
				new RenderLEDTile(BuildCraftBuilders.architectBlock),
				new RenderArchitect()
		}));

		RenderingRegistry.registerEntityRenderingHandler(EntityMechanicalArm.class, new RenderVoid());

		frameRenderId = RenderingRegistry.getNextAvailableRenderId();
		RenderingRegistry.registerBlockHandler(frameRenderId, new RenderFrame());
	}


	@Override
	public EntityBlock newDrill(World w, double i, double j, double k, double l, double d, double e, boolean xz) {
		EntityBlock eb = super.newDrill(w, i, j, k, l, d, e, xz);
		if (xz) {
			eb.texture = new IIcon[6];
			for (int a = 0; a < 6; a++) {
				eb.texture[a] = a >= 2 ? drillSideTexture : drillTexture;
			}
		} else {
			eb.setTexture(drillTexture);
		}
		return eb;
	}

	@Override
	public EntityBlock newDrillHead(World w, double i, double j, double k, double l, double d, double e) {
		EntityBlock eb = super.newDrillHead(w, i, j, k, l, d, e);
		eb.setTexture(drillHeadTexture);
		return eb;
	}
}
