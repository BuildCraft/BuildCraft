/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.lib.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.core.ISerializable;

import io.netty.buffer.ByteBuf;

public class PacketTileUpdate extends PacketUpdate {
    public BlockPos pos;

    public PacketTileUpdate() {
        super();
    }

    public PacketTileUpdate(TileEntity tile, ISerializable ser) {
        super(ser);
        tempWorld = tile.getWorld();
        pos = tile.getPos();
    }

    @Override
    public void writeIdentificationData(ByteBuf data) {
        data.writeInt(pos.getX());
        data.writeInt(pos.getY());
        data.writeInt(pos.getZ());
    }

    @Override
    public void readIdentificationData(ByteBuf data) {
        pos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
    }

    public boolean targetExists(World world) {
        return !world.isAirBlock(pos);
    }

    public TileEntity getTarget(World world) {
        return world.getTileEntity(pos);
    }

    @Override
    public void applyData(World world, EntityPlayer player) {
        if (!targetExists(world)) {
            return;
        }

        TileEntity tile = getTarget(world);

        if (!(tile instanceof ISerializable)) {
            return;
        }

        ISerializable ser = (ISerializable) tile;
        ser.readData(payloadData);
    }
}
