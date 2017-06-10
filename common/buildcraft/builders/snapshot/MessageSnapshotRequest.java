/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import io.netty.buffer.ByteBuf;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.lib.net.PacketBufferBC;

public class MessageSnapshotRequest implements IMessage {
    private Snapshot.Header header;

    public MessageSnapshotRequest() {
    }

    public MessageSnapshotRequest(Snapshot.Header header) {
        this.header = header;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        header.writeToByteBuf(new PacketBufferBC(buf));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        header = new Snapshot.Header(new PacketBufferBC(buf));
    }

    public static final IMessageHandler<MessageSnapshotRequest, MessageSnapshotResponse> HANDLER = (message, ctx) -> {
        Snapshot snapshot = GlobalSavedDataSnapshots.get(Side.SERVER).getSnapshotByHeader(message.header);
        return snapshot != null ? new MessageSnapshotResponse(snapshot) : null;
    };
}
