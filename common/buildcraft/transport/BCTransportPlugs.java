package buildcraft.transport;

import net.minecraft.util.ResourceLocation;

import buildcraft.transport.api_move.PipeAPI;
import buildcraft.transport.api_move.PluggableDefinition;
import buildcraft.transport.plug.PluggableBlocker;

public class BCTransportPlugs {

    public static PluggableDefinition blocker;

    public static void preInit() {
        blocker = new PluggableDefinition(new ResourceLocation("buildcrafttransport:blocker"), PluggableBlocker.CREATOR, PluggableBlocker.LOADER);
        PipeAPI.pluggableRegistry.registerPluggable(blocker);
    }
}
