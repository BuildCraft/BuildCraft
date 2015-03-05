/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.robotics;

import cpw.mods.fml.client.registry.RenderingRegistry;
import net.minecraftforge.client.MinecraftForgeClient;
import buildcraft.BuildCraftRobotics;
import buildcraft.robotics.render.RenderRobot;
import buildcraft.robotics.render.RobotStationItemRenderer;

public class RoboticsProxyClient extends RoboticsProxy {
	public static final RobotStationItemRenderer robotStationItemRenderer = new RobotStationItemRenderer();

	@Override
	public void registerRenderers() {
		RenderingRegistry.registerEntityRenderingHandler(EntityRobot.class, new RenderRobot());
		MinecraftForgeClient.registerItemRenderer(BuildCraftRobotics.robotItem, new RenderRobot());
		MinecraftForgeClient.registerItemRenderer(BuildCraftRobotics.robotStationItem, robotStationItemRenderer);
	}
}
