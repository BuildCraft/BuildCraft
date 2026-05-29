/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.net;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;

import net.minecraft.server.network.ServerPlayerEntity;

import buildcraft.api.core.BCDebugging;

/**
 * STUB(R.Chen): the Forge {@code SimpleNetworkWrapper}/{@code IMessage} registration machinery
 * (registerMessageClass / setHandler / fmlPostInit / sendToDimension / sendToAllAround) is dropped.
 * On Fabric, packet types register themselves via {@link BCNetworkManager}; this class is now a thin
 * delegate preserving the {@code sendTo*} call-site signatures used by {@code TileBC_Neptune}.
 */
public class MessageManager {
    public static final boolean DEBUG = BCDebugging.shouldDebugLog("lib.messages");

    private MessageManager() {}

    /** Legacy: {@code MessageManager.sendToServer(msg)} — client-only. */
    @Environment(EnvType.CLIENT)
    public static <T extends FabricPacket> void sendToServer(T message) {
        BCNetworkManager.sendToServer(message);
    }

    /** Legacy: {@code MessageManager.sendTo(msg, playerMP)}. */
    public static <T extends FabricPacket> void sendTo(T message, ServerPlayerEntity player) {
        BCNetworkManager.sendTo(message, player);
    }
}
