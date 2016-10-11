package buildcraft.transport.api_move;

import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.transport.PipeEvent;

/** Designates a tile that can contain a pipe, up to 6 sided pluggables, and up to 4 different pipe wires. */
public interface IPipeHolder {
    World getPipeWorld();

    BlockPos getPipePos();

    TileEntity getPipeTile();

    IPipe getPipe();

    PipePluggable getPluggable(EnumFacing side);

    TileEntity getNeighbouringTile(EnumFacing side);

    IPipe getNeighbouringPipe(EnumFacing side);

    IWireManager getWireManager();

    void fireEvent(PipeEvent event);

    void scheduleRenderUpdate();

    /** @param parts The parts that want to send a network update. */
    void scheduleNetworkUpdate(PipeMessageReceiver... parts);

    /** Sends a custom message from a pluggable or pipe centre to the server/client (depending on which side this is
     * currently on). */
    void sendMessage(PipeMessageReceiver to, IWriter writer);

    public enum PipeMessageReceiver {
        BEHAVIOUR(null),
        FLOW(null),
        PLUGGABLE_DOWN(EnumFacing.DOWN),
        PLUGGABLE_UP(EnumFacing.UP),
        PLUGGABLE_NORTH(EnumFacing.NORTH),
        PLUGGABLE_SOUTH(EnumFacing.SOUTH),
        PLUGGABLE_WEST(EnumFacing.WEST),
        PLUGGABLE_EAST(EnumFacing.EAST);
        // Wires are updated differently (they never use this API)

        public static final PipeMessageReceiver[] VALUES = values();
        public static final PipeMessageReceiver[] PLUGGABLES = new PipeMessageReceiver[6];

        static {
            for (PipeMessageReceiver type : VALUES) {
                if (type.face != null) {
                    PLUGGABLES[type.face.ordinal()] = type;
                }
            }
        }

        public final EnumFacing face;

        PipeMessageReceiver(EnumFacing face) {
            this.face = face;
        }
    }

    public interface IWriter {
        void write(PacketBuffer buffer);
    }
}
