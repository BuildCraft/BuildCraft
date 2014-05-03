/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core;

import net.minecraft.world.World;

import buildcraft.core.robots.EntityRobot;

public class ItemRobot extends ItemBuildCraft {

	Class<? extends EntityRobot> robotClass;

	public ItemRobot(Class<? extends EntityRobot> robotClass) {
		super(CreativeTabBuildCraft.ITEMS);

		this.robotClass = robotClass;
	}

	public EntityRobot createRobot (World world) {
		try {
			return this.robotClass.getConstructor(World.class).newInstance(world);
		} catch (Throwable e) {
			e.printStackTrace();
			return null;
		}
	}

}
