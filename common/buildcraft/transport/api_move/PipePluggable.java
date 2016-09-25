package buildcraft.transport.api_move;

import java.io.IOException;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public abstract class PipePluggable {
    public final PluggableDefinition definition;
    public final IPipeHolder holder;
    public final EnumFacing side;

    public PipePluggable(PluggableDefinition definition, IPipeHolder holder, EnumFacing side) {
        this.definition = definition;
        this.holder = holder;
        this.side = side;
    }

    public void writePayload(PacketBuffer buffer, Side side) {

    }

    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) throws IOException {

    }

    public void onTick() {}

    /** @return A bounding box that will be used for collisions and raytracing. */
    public abstract AxisAlignedBB getBoundingBox();

    /** @return True if the pipe cannot connect outwards (it is blocked), or False if this does not block the pipe. */
    public boolean isBlocking() {
        return false;
    }

    public boolean hasCapability(Capability<?> cap) {
        return false;
    }

    public <T> T getCapability(Capability<T> cap) {
        return null;
    }

    // TODO: Model Key
}
