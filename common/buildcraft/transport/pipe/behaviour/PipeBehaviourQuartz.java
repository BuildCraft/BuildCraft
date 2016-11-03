package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventItem;
import buildcraft.api.transport.neptune.IPipe;

public class PipeBehaviourQuartz extends PipeBehaviourSeparate {
    private static final double SPEED_DELTA = 0.002;
    private static final double SPEED_TARGET = 0.04;

    public PipeBehaviourQuartz(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourQuartz(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public void modifySpeed(PipeEventItem.ModifySpeed event) {
        event.modifyTo(SPEED_TARGET, SPEED_DELTA);
    }
}
