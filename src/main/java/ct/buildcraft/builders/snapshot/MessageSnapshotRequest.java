/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import ct.buildcraft.lib.net.MessageManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class MessageSnapshotRequest{
    private Snapshot.Key key;

    public MessageSnapshotRequest(Snapshot.Key key) {
        this.key = key;
    }

    public void toBytes(FriendlyByteBuf buf) {
        key.writeToByteBuf((buf));
    }

    public MessageSnapshotRequest(FriendlyByteBuf buf) {
        key = new Snapshot.Key((buf));
    }

    public static final BiConsumer<MessageSnapshotRequest, Supplier<NetworkEvent.Context>> HANDLER = (message, ctx) -> {
        Snapshot snapshot = GlobalSavedDataSnapshots.get(LogicalSide.SERVER).getSnapshot(message.key);
        if(snapshot != null)
        	MessageManager.sendTo(new MessageSnapshotResponse(snapshot), ctx.get().getSender());
    };
}
