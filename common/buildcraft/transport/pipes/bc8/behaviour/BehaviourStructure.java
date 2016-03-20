package buildcraft.transport.pipes.bc8.behaviour;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeBehaviour_BC8;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;

public class BehaviourStructure extends PipeBehaviour_BC8 {
    public BehaviourStructure(PipeDefinition_BC8 definition, IPipe_BC8 pipe) {
        super(definition, pipe);
    }

    @Override
    public int getIconIndex(EnumFacing side) {
        return 0;
    }

    @Override
    public NBTBase writeToNBT() {
        return null;
    }

    @Override
    public PipeBehaviour_BC8 readFromNBT(NBTBase nbt) {
        return this;
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {}

    @Override
    public PipeBehaviour_BC8 readFromByteBuf(ByteBuf buf) {
        return this;
    }
}
