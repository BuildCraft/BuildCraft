package buildcraft.api.events;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.player.EntityPlayer;

public class PipePlacedEvent extends Event {
	public EntityPlayer player;
	public String pipeType;

	public PipePlacedEvent(EntityPlayer player, String pipeType){
		this.player = player;
		this.pipeType = pipeType;
	}

}
