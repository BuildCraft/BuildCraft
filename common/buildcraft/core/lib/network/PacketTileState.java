/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.network;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.ISerializable;
import buildcraft.core.network.PacketIds;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class PacketTileState extends PacketCoordinates {

    private ByteBuf state;

    private class StateWithId {
        public byte stateId;
        public ISerializable state;

        public StateWithId(byte stateId, ISerializable state) {
            this.stateId = stateId;
            this.state = state;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("StateWithId [stateId=");
            builder.append(stateId);
            builder.append(", state=");
            builder.append(state == null ? "null" : state.getClass());
            builder.append("]");
            return builder.toString();
        }
    }

    private List<StateWithId> stateList = new LinkedList<StateWithId>();

    /** Default constructor for incoming packets */
    public PacketTileState() {}

    /** Constructor for outgoing packets
     *
     * @param pos - the coordinates the tile to sync */
    public PacketTileState(TileEntity tile) {
        super(PacketIds.STATE_UPDATE, tile.getWorld().provider.getDimensionId(), tile.getPos());
        isChunkDataPacket = true;
    }

    @Override
    public int getID() {
        return PacketIds.STATE_UPDATE;
    }

    public void addStateForSerialization(byte stateId, ISerializable state) {
        stateList.add(new StateWithId(stateId, state));
    }

    @Override
    public void writeData(ByteBuf data, World world, EntityPlayer player) {
        super.writeData(data, world, player);

        ByteBuf tmpState = Unpooled.buffer();

        tmpState.writeByte(stateList.size());
        for (StateWithId stateWithId : stateList) {
            tmpState.writeByte(stateWithId.stateId);
            stateWithId.state.writeData(tmpState);
        }

        data.writeShort((short) tmpState.readableBytes());
        data.writeBytes(tmpState.readBytes(tmpState.readableBytes()));
    }

    @Override
    public void readData(ByteBuf data, World world, EntityPlayer player) {
        super.readData(data, world, player);

        state = Unpooled.buffer();
        int length = data.readUnsignedShort();
        state.writeBytes(data.readBytes(length));
    }

    @Override
    public void applyData(World world) {
        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof ISyncedTile) {
            ISyncedTile tile1 = (ISyncedTile) tile;
            byte stateCount = state.readByte();
            for (int i = 0; i < stateCount; i++) {
                byte stateId = state.readByte();
                tile1.getStateInstance(stateId).readData(state);
                tile1.afterStateUpdated(stateId);
            }
        } else {
            BCLog.logger.info("ignored the packet @ " + pos + " as (" + tile + " instanceof ISyncedTile) was false!");
        }
    }

    @Override
    public String toString() {
        final int maxLen = 10;
        StringBuilder builder = new StringBuilder();
        builder.append("PacketTileState [state=");
        builder.append(state == null ? "-1" : state.readableBytes());
        builder.append(", stateList=");
        builder.append(stateList != null ? stateList.subList(0, Math.min(stateList.size(), maxLen)) : null);
        builder.append(", super=");
        builder.append(super.toString());
        builder.append("]");
        return builder.toString();
    }
}
