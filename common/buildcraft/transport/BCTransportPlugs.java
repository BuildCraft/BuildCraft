package buildcraft.transport;

import net.minecraft.util.ResourceLocation;

import buildcraft.transport.api_move.PipeAPI;
import buildcraft.transport.api_move.PluggableDefinition;
import buildcraft.transport.plug.PluggableStop;

public class BCTransportPlugs {

    public static PluggableDefinition stop;

    public static void preInit() {
        stop = new PluggableDefinition(new ResourceLocation("buildcrafttransport:stop"), PluggableStop.CREATOR, PluggableStop.LOADER);
        PipeAPI.pluggableRegistry.registerPluggable(stop);
    }
}
