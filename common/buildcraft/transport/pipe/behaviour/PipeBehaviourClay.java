package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventItem;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.IPipe.ConnectedType;
import buildcraft.api.transport.neptune.PipeBehaviour;

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
                /* We only really need to increase the precedence, but using a larger number (12) means that it doesn't
                 * matter what plugs are attached (say filters) and this will always prefer to go into inventories above
                 * the correct filters. (Although note that the filters still matter) */
                ordering.increasePrecedence(face, 12);
            }
        }
    }
}
