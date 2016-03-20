/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network.base;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public abstract class Packet {
    protected boolean isChunkDataPacket = false;
    public int dimensionId;
    public World tempWorld;
    boolean hasDoneByteStuff = false;

    /** Set by the channel handler AFTER read time. Package-private to hint that this should NOT be used at read time,
     * but at apply time. */
    EntityPlayer player = null;

    /** Default no-args constructor for constructing a received packet */
    public Packet() {
        dimensionId = PacketHandler.INVALID_DIM_ID;
    }

    public Packet(int dimId) {
        this.dimensionId = dimId;
    }

    public Packet(World world) {
        this(world.provider.getDimensionId());
        tempWorld = world;
    }

    public void readData(ByteBuf data) {
        dimensionId = data.readInt();
        hasDoneByteStuff = true;
    }

    public void writeData(ByteBuf data) {
        if (dimensionId == PacketHandler.INVALID_DIM_ID) throw new IllegalStateException("Invalid Dimension ID!");
        data.writeInt(dimensionId);
        hasDoneByteStuff = true;
    }

    /** Called in the main world tick to apply any data that cannot be applied in a different thread. So, everything. */
    public abstract void applyData(World world, EntityPlayer player);

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Packet [isChunkDataPacket=");
        builder.append(isChunkDataPacket);
        builder.append(", dimensionId=");
        builder.append(dimensionId);
        builder.append("]");
        return builder.toString();
    }
}
