/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import io.netty.buffer.ByteBuf;

public abstract class PacketCoordinates extends Packet {

    public BlockPos pos;
    private int id;

    public PacketCoordinates() {}

    public PacketCoordinates(int id, int dimId, BlockPos pos) {
        this.id = id;
        this.dimensionId = id;
        this.pos = pos;
    }

    @Override
    public void writeData(ByteBuf data, World world, EntityPlayer player) {
        super.writeData(data, world, player);
        data.writeByte(id);
        data.writeInt(pos.getX());
        data.writeInt(pos.getY());
        data.writeInt(pos.getZ());
    }

    @Override
    public void readData(ByteBuf data, World world, EntityPlayer player) {
        super.readData(data, world, player);
        id = data.readByte();
        pos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
    }

    @Override
    public int getID() {
        return id;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PacketCoordinates [pos=");
        builder.append(pos);
        builder.append(", id=");
        builder.append(id);
        builder.append(", super=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }

}
