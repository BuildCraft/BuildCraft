package buildcraft.api.transport.pipe_bc8;

import java.util.Map;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEvent_BC8;

/** Represents a pipe in a world. Most older (BC version less than 8.x) events, functions and querys can be called by
 * firing the appropriate event, and inspecting the event afterwards. Most functionality has been delegated to */
/* Note that this does not have a "get pipe tile" method, as pipes are not bound to just tile entities */
public interface IPipe_BC8 {
    World getWorld();

    IPipePropertyProvider getProperties();

    Map<EnumFacing, IConnection_BC8> getConnections();

    void fireEvent(IPipeEvent_BC8 event);
}
