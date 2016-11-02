package buildcraft.transport.pipe.behaviour;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.transport.neptune.IFlowItems;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.PipeBehaviour;
import buildcraft.api.transport.neptune.PipeFlow;

public class PipeBehaviourObsidian extends PipeBehaviour implements IMjRedstoneReceiver {
    public PipeBehaviourObsidian(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourObsidian(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    // IMjRedstoneReceiver

    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        return MjAPI.MJ;
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        // TODO: Make this require more or less than 1 Mj Per item
        // Also make this extract different numbers of items depending
        // on how much power was put in

        EntityItem potential = null;
        // TODO: Add a search function to try and find a suitable item!

        PipeFlow flow = pipe.getFlow();
        if (flow instanceof IFlowItems) {

        }
        return microJoules;
    }
}
