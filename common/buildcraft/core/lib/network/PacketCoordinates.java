/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;

import buildcraft.core.lib.network.base.Packet;

public abstract class PacketCoordinates extends Packet {

    public BlockPos pos;

    public transient final TileEntity tile;

    public PacketCoordinates() {
        tile = null;
    }

    public PacketCoordinates(TileEntity tile) {
        this.tile = tile;
        this.tempWorld = tile.getWorld();
        this.dimensionId = tempWorld.provider.getDimensionId();
        this.pos = tile.getPos();
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeInt(pos.getX());
        data.writeInt(pos.getY());
        data.writeInt(pos.getZ());
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        pos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("PacketCoordinates [pos=");
        builder.append(pos);
        builder.append(", super=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
}
