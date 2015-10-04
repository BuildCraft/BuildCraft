/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import io.netty.buffer.ByteBuf;

public abstract class Packet {

    protected boolean isChunkDataPacket = false;
    public int dimensionId;
    public World tempWorld;
    public int unique_packet_id = -1;

    public abstract int getID();

    public void readData(ByteBuf data, World world, EntityPlayer player) {
        unique_packet_id = data.readInt();
        dimensionId = data.readInt();
    }

    public void writeData(ByteBuf data, World world, EntityPlayer player) {
        data.writeInt(unique_packet_id);
        data.writeInt(world.provider.getDimensionId());
    }

    /** Called in the main world tick to apply any data that cannot be applied in a different thread. So, everything. */
    public abstract void applyData(World world);

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
