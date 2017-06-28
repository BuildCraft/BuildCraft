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
    private Snapshot.Key key;

    public MessageSnapshotRequest() {
    }

    public MessageSnapshotRequest(Snapshot.Key key) {
        this.key = key;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        key.writeToByteBuf(new PacketBufferBC(buf));
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        key = new Snapshot.Key(new PacketBufferBC(buf));
    }

    public static final IMessageHandler<MessageSnapshotRequest, MessageSnapshotResponse> HANDLER = (message, ctx) -> {
        Snapshot snapshot = GlobalSavedDataSnapshots.get(Side.SERVER).getSnapshot(message.key);
        return snapshot != null ? new MessageSnapshotResponse(snapshot) : null;
    };
}
