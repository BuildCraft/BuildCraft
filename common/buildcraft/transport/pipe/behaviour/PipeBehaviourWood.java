package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjRedstoneReceiver;
import buildcraft.api.mj.MjAPI;

import buildcraft.core.lib.inventory.filters.StackFilter;
import buildcraft.transport.api_move.IFlowItems;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.IPipe.ConnectedType;
import buildcraft.transport.api_move.PipeBehaviour;
import buildcraft.transport.api_move.PipeFlow;

public class PipeBehaviourWood extends PipeBehaviourDirectional implements IMjRedstoneReceiver {
    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return (face != null && face == getCurrentDir()) ? 1 : 0;
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWood);
    }

    @Override
    protected boolean canFaceDirection(EnumFacing dir) {
        return pipe.getConnectedType(dir) == ConnectedType.TILE;
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

        PipeFlow flow = pipe.getFlow();
        if (flow instanceof IFlowItems) {
            ((IFlowItems) flow).tryExtractStack(1, getCurrentDir(), StackFilter.ALL);
        }
        return 0;
    }
}
