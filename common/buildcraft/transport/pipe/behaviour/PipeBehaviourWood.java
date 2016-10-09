package buildcraft.transport.pipe.behaviour;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import buildcraft.core.lib.inventory.filters.StackFilter;
import buildcraft.transport.api_move.IFlowItems;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.PipeBehaviour;
import buildcraft.transport.api_move.PipeFlow;

public class PipeBehaviourWood extends PipeBehaviour {
    private int lastExtract = 0;

    private EnumFacing currentDir = EnumFacing.UP;

    public PipeBehaviourWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourWood(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public void configureFlow(PipeFlow flow) {
        if (flow instanceof IFlowItems) {
            IFlowItems itemFlow = (IFlowItems) flow;
            itemFlow.setTargetSpeed(0.05);
        }
    }

    @Override
    public int getTextureIndex(EnumFacing face) {
        return (face != null && face == currentDir) ? 1 : 0;
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeBehaviour other) {
        return !(other instanceof PipeBehaviourWood);
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return true;
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }

        lastExtract++;
        if (lastExtract >= 40) {
            lastExtract = 0;
            PipeFlow flow = pipe.getFlow();
            if (flow instanceof IFlowItems) {
                ((IFlowItems) flow).tryExtractStack(1, currentDir, StackFilter.ALL);
            }
        }
    }
}
