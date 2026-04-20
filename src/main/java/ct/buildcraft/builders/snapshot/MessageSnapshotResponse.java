/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.io.IOException;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class MessageSnapshotResponse {
    private Snapshot snapshot;

    public MessageSnapshotResponse(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    public void toBytes(FriendlyByteBuf buf) {

//        byte[] bytes = NbtSquisher.squishBuildCraftV1(Snapshot.writeToNBT(snapshot));
//        buf.writeInt(bytes.length);
//        buf.writeBytes(bytes);
//        try {
//            CompressedStreamTools.write(Snapshot.writeToNBT(snapshot), new ByteBufOutputStream(buf));
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
        try {
        	NbtIo.writeCompressed(Snapshot.writeToNBT(snapshot), new ByteBufOutputStream(buf));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public MessageSnapshotResponse(FriendlyByteBuf buf) {
        try {
//            snapshot = Snapshot.readFromNBT(NbtSquisher.expand(buf.readBytes(buf.readInt()).array()));
//            snapshot = Snapshot.readFromNBT(CompressedStreamTools.read(new ByteBufInputStream(buf), NBTSizeTracker.INFINITE));
            snapshot = Snapshot.readFromNBT(NbtIo.readCompressed(new ByteBufInputStream(buf)));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final BiConsumer<MessageSnapshotResponse, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        ClientSnapshots.INSTANCE.onSnapshotReceived(message.snapshot);
    };
}
