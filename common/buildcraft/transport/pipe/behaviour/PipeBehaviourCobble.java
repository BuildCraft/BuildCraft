package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventItem;
import buildcraft.api.transport.pipe.PipeEventPower;

public class PipeBehaviourCobble extends PipeBehaviourSeparate {
    private static final double SPEED_DELTA = 0.03;
    private static final double SPEED_TARGET = 0.04;

    public PipeBehaviourCobble(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourCobble(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public static void modifySpeed(PipeEventItem.ModifySpeed event) {
        event.modifyTo(SPEED_TARGET, SPEED_DELTA);
    }
}
