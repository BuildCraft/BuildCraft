package buildcraft.transport.plug;

import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventItem;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.PipePluggable;

public class FilterEventHandler {
    @PipeEventHandler
    public static void sideCheck(PipeEventItem.SideCheck event) {
        for (EnumFacing side : EnumFacing.VALUES) {
            if (!event.isAllowed(side)) {
                continue;
            }
            IPipe neighbour = event.holder.getNeighbouringPipe(side);
            if (neighbour == null) {
                continue;
            }
            PipePluggable plug = neighbour.getHolder().getPluggable(side.getOpposite());
            if (plug instanceof PluggableLens) {
                PluggableLens lens = (PluggableLens) plug;
                lens.sideCheckAnyPos(event, side);
            }
        }
    }
}
