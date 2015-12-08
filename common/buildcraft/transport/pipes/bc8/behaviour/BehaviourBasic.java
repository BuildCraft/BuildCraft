package buildcraft.transport.pipes.bc8.behaviour;

import com.google.common.collect.ImmutableList;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import buildcraft.api.transport.pipe_bc8.BCPipeEventHandler;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeBehaviour_BC8;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8;
import buildcraft.transport.pipes.bc8.behaviour.BehaviourFactoryBasic.EnumListStatus;

import io.netty.buffer.ByteBuf;

public class BehaviourBasic extends PipeBehaviour_BC8 {
    public final ImmutableList<PipeDefinition_BC8> connectionList;
    public final EnumListStatus blacklist;

    public BehaviourBasic(PipeDefinition_BC8 definition, IPipe_BC8 pipe, ImmutableList<PipeDefinition_BC8> connectionList, EnumListStatus blacklist) {
        super(definition, pipe);
        this.connectionList = connectionList;
        this.blacklist = blacklist;
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

    @Override
    public int getIconIndex(EnumFacing side) {
        return 0;
    }

    @BCPipeEventHandler
    public void pipeConnectEvent(IPipeEventConnection_BC8.AttemptCreate.Pipe connect) {
        boolean contains = connectionList.contains(connect.with().getBehaviour().definition);
        if (blacklist == EnumListStatus.BLACKLIST) {
            if (contains) {
                connect.disallow();
            }
        } else {// Must be a whitelist
            if (!contains) {
                connect.disallow();
            }
        }
    }
}
