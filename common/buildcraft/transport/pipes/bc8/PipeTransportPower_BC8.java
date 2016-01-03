package buildcraft.transport.pipes.bc8;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.mutable.MutableInt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.Identifier;
import buildcraft.api.power.bc8.IPowerConnection;
import buildcraft.api.power.bc8.IPowerConnection.IPowerRelay;
import buildcraft.api.power.bc8.IPowerTunnel;
import buildcraft.api.transport.pipe_bc8.BCPipeEventHandler;
import buildcraft.api.transport.pipe_bc8.IPipeListener;
import buildcraft.api.transport.pipe_bc8.IPipeListenerFactory;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventConnection_BC8.AttemptCreate;
import buildcraft.transport.api.impl.EnumPipeType;

import io.netty.buffer.ByteBuf;

public class PipeTransportPower_BC8 implements IPipeListener, IPowerRelay {
    public final IPipe_BC8 pipe;
    /** A map of tunnels to the last amount of RF transfered. The server sends the client all of the deltas for each
     * {@link EnumFacing} of the pipe. */
    private final Map<IPowerTunnel, MutableInt> tunnels = new HashMap<>();
    /** The total amount of RF currently being transfered through this pipe. This is added to and subtracted from with
     * each of the notifyTunnel* methods. */
    private int totalRF = 0;

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
        else if (other instanceof IPowerConnection) {
            event.couldAccept();
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

    // IPowerRelay

    @Override
    public Map<Identifier, IPowerConnection> connections() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void notifyTunnelConnected(IPowerTunnel tunnel) {
        tunnels.put(tunnel, new MutableInt());
    }

    @Override
    public void notifyTunnelDisconnected(IPowerTunnel tunnel) {
        MutableInt mutInt = tunnels.remove(tunnel);
        totalRF -= mutInt.intValue();
    }

    @Override
    public void notifyDeltaChange(IPowerTunnel tunnel, int oldRf, int newRf, int delta) {
        if (!tunnels.containsKey(tunnel)) throw new IllegalArgumentException("Unknown tunnel!");
        MutableInt mutInt = tunnels.get(tunnel);
        mutInt.add(delta);
        totalRF += delta;
    }

    @Override
    public int maxUnitsTransfered() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Identifier itentifier() {
        // TODO Auto-generated method stub
        return null;
    }

    public enum Factory implements IPipeListenerFactory {
        INSTANCE;

        @Override
        public IPipeListener createNewListener(IPipe_BC8 pipe) {
            return new PipeTransportPower_BC8(pipe);
        }
    }
}
