package buildcraft.core;

import net.minecraftforge.fml.common.event.FMLInterModComms.IMCEvent;
import net.minecraftforge.fml.common.event.FMLInterModComms.IMCMessage;

public abstract class IMCHandler {
    public abstract void processIMCEvent(IMCEvent event, IMCMessage m);
}
