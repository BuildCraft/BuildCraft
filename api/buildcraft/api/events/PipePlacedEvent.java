package buildcraft.api.events;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.eventhandler.Event;

public class PipePlacedEvent extends Event {
	public EntityPlayer player;
	public String pipeType;

	public PipePlacedEvent(EntityPlayer player, String pipeType) {
		this.player = player;
		this.pipeType = pipeType;
	}

}
