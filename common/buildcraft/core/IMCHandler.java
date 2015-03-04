package buildcraft.core;

import cpw.mods.fml.common.event.FMLInterModComms.IMCEvent;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

public abstract class IMCHandler {
	public abstract void processIMCEvent(IMCEvent event, IMCMessage m);
}
