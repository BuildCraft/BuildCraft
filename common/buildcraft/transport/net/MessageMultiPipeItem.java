/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.net.PacketBufferBC;

// STUB(R.Chen): full implementation in Phase 4E.
// The Forge SimpleImpl message plumbing (IMessage / IMessageHandler / MessageContext) and the
// client-side HANDLER (which dispatches into PipeFlowItems) are dropped until the Fabric networking
// layer and the PipeFlowItems flow are migrated. The wire format (read/write) and the
// TravellingItemData record are kept so producers/consumers of the queue keep compiling.
public class MessageMultiPipeItem {

    private static final int MAX_ITEMS_PER_PIPE = 10;
    private static final int MAX_POSITIONS = 4000;
    public final Map<BlockPos, List<TravellingItemData>> items = new HashMap<>();

    public MessageMultiPipeItem() {
    }

    public void fromBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        int blockCount = buf.readShort();
        for (int b = 0; b < blockCount; b++) {
            BlockPos pos = buf.readBlockPos();
            List<TravellingItemData> posItems = new ArrayList<>();
            items.put(pos, posItems);
            int itemCount = buf.readUnsignedByte();
            for (int i = 0; i < itemCount; i++) {
                posItems.add(new TravellingItemData(buf));
            }
        }
    }

    public void toBytes(ByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        int blockCount = Math.min(items.size(), MAX_POSITIONS);
        buf.writeShort(blockCount);
        int blockIndex = 0;
        for (Entry<BlockPos, List<TravellingItemData>> entry : items.entrySet()) {
            buf.writeBlockPos(entry.getKey());
            List<TravellingItemData> list = entry.getValue();
            int itemCount = Math.min(list.size(), MAX_ITEMS_PER_PIPE);
            buf.writeByte(itemCount);
            for (int i = 0; i < itemCount; i++) {
                list.get(i).toBuffer(buf);
            }
            if (++blockIndex >= blockCount) {
                break;
            }
        }
    }

    public void append(BlockPos pos, int stackId, byte stackCount, boolean toCenter, Direction side,
        DyeColor colour, byte timeToDest) {
        List<TravellingItemData> list = items.get(pos);
        if (list == null) {
            if (items.size() >= MAX_POSITIONS) {
                return;
            }
            list = new ArrayList<>();
            items.put(pos, list);
        }
        if (list.size() >= MAX_ITEMS_PER_PIPE) {
            return;
        }
        list.add(new TravellingItemData(stackId, stackCount, toCenter, side, colour, timeToDest));
    }

    public static class TravellingItemData {
        public final int stackId;
        public final byte stackCount;
        public final boolean toCenter;
        public final Direction side;
        public final @Nullable DyeColor colour;
        public final byte timeToDest;

        public TravellingItemData(int stackId, byte stackCount, boolean toCenter, Direction side, DyeColor colour,
            byte timeToDest) {
            this.stackId = stackId;
            this.stackCount = stackCount;
            this.toCenter = toCenter;
            this.side = side;
            this.colour = colour;
            this.timeToDest = timeToDest;
        }

        TravellingItemData(PacketBufferBC buf) {
            stackId = buf.readVarInt();
            stackCount = buf.readByte();
            toCenter = buf.readBoolean();
            side = buf.readEnumValue(Direction.class);
            colour = MessageUtil.readEnumOrNull(buf, DyeColor.class);
            timeToDest = buf.readByte();
        }

        void toBuffer(PacketBufferBC buf) {
            buf.writeVarInt(stackId);
            buf.writeByte(stackCount);
            buf.writeBoolean(toCenter);
            buf.writeEnumValue(side);
            MessageUtil.writeEnumOrNull(buf, colour);
            buf.writeByte(timeToDest);
        }
    }
}
