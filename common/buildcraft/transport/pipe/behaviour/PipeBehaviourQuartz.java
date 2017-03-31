package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;

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
    public static void modifySpeed(PipeEventItem.ModifySpeed event) {
        event.modifyTo(SPEED_TARGET, SPEED_DELTA);
    }
}
