package buildcraft.api.events;

import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.eventhandler.Event;

public class PipePlacedEvent extends Event {
	public EntityPlayer player;
	public String pipeType;
	public int x, y, z;

	public PipePlacedEvent(EntityPlayer player, String pipeType, int x, int y, int z) {
		this.player = player;
		this.pipeType = pipeType;
		this.x = x;
		this.y = y;
		this.z = z;
	}

}
