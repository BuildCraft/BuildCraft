package buildcraft.transport.pipes.bc8;

import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTBase;

import buildcraft.api.transport.pipe_bc8.BCPipeEventHandler;
import buildcraft.api.transport.pipe_bc8.IPipeListener;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8.AttemptCreate;
import buildcraft.transport.api.impl.EnumPipeType;

public class PipeTransportPower_BC8 implements IPipeListener {
    public final IPipe_BC8 pipe;

    public PipeTransportPower_BC8(IPipe_BC8 pipe) {
        this.pipe = pipe;
    }

    // Pipe Event handling

    @BCPipeEventHandler
    public void attemptConnection(IPipeEventConnection_BC8.AttemptCreate event) {
        Object other = event.getConnection().getOther();
        if (event instanceof AttemptCreate.Pipe) {
            AttemptCreate.Pipe pipe = (AttemptCreate.Pipe) event;
            /* This isn't the best check but the alternative is going through all of the listener objects in the pipe
             * and checking if it implements IPowerConnection. */
            if (pipe.getPipe().getBehaviour().definition.type == EnumPipeType.POWER) {
                event.couldAccept();
            }
        }
        throw new RuntimeException("IMPLEMENT PipeTransportPower_BC8.attemptConnection");
    }

    // State read and writing

    @Override
    public PipeTransportPower_BC8 readFromNBT(NBTBase nbt) {
        return this;
    }

    @Override
    public NBTBase writeToNBT() {
        return null;
    }

    @Override
    public PipeTransportPower_BC8 readFromByteBuf(ByteBuf buf) {
        return this;
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {}
}
