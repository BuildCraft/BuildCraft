package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.PipeEventHandler;
import buildcraft.api.transport.PipeEventPower;
import buildcraft.api.transport.neptune.IPipe;

public class PipeBehaviourStone extends PipeBehaviourSeparate {
    public PipeBehaviourStone(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourStone(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @PipeEventHandler
    public static void configurePower(PipeEventPower.Configure event) {
        event.setMaxPower(MjAPI.MJ * 8);
        event.setPowerResistance(MjAPI.MJ / 40);// 1/40th lost, or 0.025%
    }
}
