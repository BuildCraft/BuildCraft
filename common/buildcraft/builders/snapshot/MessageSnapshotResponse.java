/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.net.IMessage;
import buildcraft.api.net.IMessageHandler;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

public class MessageSnapshotResponse implements IMessage {
    private Snapshot snapshot;

    @SuppressWarnings("unused")
    public MessageSnapshotResponse() {
    }

    public MessageSnapshotResponse(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Override
    public void toBytes(PacketBuffer buf) {

//        byte[] bytes = NbtSquisher.squishBuildCraftV1(Snapshot.writeToNBT(snapshot));
//        buf.writeInt(bytes.length);
//        buf.writeBytes(bytes);
//        try {
//            CompressedStreamTools.write(Snapshot.writeToNBT(snapshot), new ByteBufOutputStream(buf));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        // Calen: no Exception
//        try {
//            CompressedStreamTools.writeCompressed(Snapshot.writeToNBT(snapshot), new ByteBufOutputStream(buf));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        buf.writeNbt(Snapshot.writeToNBT(snapshot));
    }

    @Override
    public void fromBytes(PacketBuffer buf) {
        try {
//            snapshot = Snapshot.readFromNBT(NbtSquisher.expand(buf.readBytes(buf.readInt()).array()));
//            snapshot = Snapshot.readFromNBT(CompressedStreamTools.read(new ByteBufInputStream(buf), NBTSizeTracker.INFINITE));
//            snapshot = Snapshot.readFromNBT(CompressedStreamTools.readCompressed(new ByteBufInputStream(buf)));
            snapshot = Snapshot.readFromNBT(buf.readNbt());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final IMessageHandler<MessageSnapshotResponse, IMessage> HANDLER = (message, ctx) ->
    {
        ClientSnapshots.INSTANCE.onSnapshotReceived(message.snapshot);
        return null;
    };
}
