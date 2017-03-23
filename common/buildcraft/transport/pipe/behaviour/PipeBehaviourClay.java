package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;

public class PipeBehaviourClay extends PipeBehaviour {
    public PipeBehaviourClay(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourClay(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public void orderSides(PipeEventItem.SideCheck ordering) {
        for (EnumFacing face : EnumFacing.VALUES) {
            ConnectedType type = pipe.getConnectedType(face);
            if (type == ConnectedType.TILE) {
                /* We only really need to increase the priority, but using a larger number (100) means that it doesn't
                 * matter what plugs are attached (e.g. filters) and this will always prefer to go into inventories
                 * above the correct filters. (Although note that the filters still matter) */
                ordering.increasePriority(face, 100);
            }
        }
    }

    @PipeEventHandler
    public void orderSides(PipeEventFluid.SideCheck ordering) {
        for (EnumFacing face : EnumFacing.VALUES) {
            ConnectedType type = pipe.getConnectedType(face);
            if (type == ConnectedType.TILE) {
                /* We only really need to increase the priority, but using a larger number (100) means that it doesn't
                 * matter what plugs are attached (e.g. filters) and this will always prefer to go into inventories
                 * above the correct filters. (Although note that the filters still matter) */
                ordering.increasePriority(face, 100);
            }
        }
    }
}
