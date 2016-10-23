package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventItem;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.PipeBehaviour;

public class PipeBehaviourVoid extends PipeBehaviour {
    public PipeBehaviourVoid(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourVoid(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return 0;
    }

    @PipeEventHandler
    public void reachCentre(PipeEventItem.ReachCenter reachCenter) {
        reachCenter.stack.stackSize = 0;
    }
}
