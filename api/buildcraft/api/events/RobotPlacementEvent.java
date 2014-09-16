package buildcraft.api.events;


import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.eventhandler.Cancelable;
import cpw.mods.fml.common.eventhandler.Event;

/**
 * Copyright (c) 2014, AEnterprise
 * http://buildcraftadditions.wordpress.com/
 * Buildcraft Additions is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://buildcraftadditions.wordpress.com/wiki/licensing-stuff/
 */
@Cancelable
public class RobotPlacementEvent extends Event {
	public EntityPlayer player;
	public String robotProgram;

	public RobotPlacementEvent(EntityPlayer player, String robotProgram){
		this.player = player;
		this.robotProgram = robotProgram;
	}

}
