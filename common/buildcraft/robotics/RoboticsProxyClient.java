/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Loader;
import net.minecraftforge.client.MinecraftForgeClient;

import buildcraft.BuildCraftRobotics;
import buildcraft.robotics.render.RenderRobot;
import buildcraft.robotics.render.RenderZonePlan;
import buildcraft.robotics.render.RobotStationItemRenderer;

public class RoboticsProxyClient extends RoboticsProxy {
	public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityRobot.class, new RenderRobot());
		MinecraftForgeClient.registerItemRenderer(BuildCraftRobotics.robotItem, new RenderRobot());
		ClientRegistry.bindTileEntitySpecialRenderer(TileZonePlan.class, new RenderZonePlan());

		// TODO: Move robot station textures locally
		if (Loader.isModLoaded("BuildCraft|Transport")) {
			loadBCTransport();
		}
	}

	private void loadBCTransport() {
		MinecraftForgeClient.registerItemRenderer(BuildCraftRobotics.robotStationItem, new RobotStationItemRenderer());
	}
}
