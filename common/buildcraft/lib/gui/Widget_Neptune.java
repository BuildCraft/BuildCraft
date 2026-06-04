/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.lib.gui;

import java.io.IOException;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import buildcraft.lib.net.IPayloadReceiver;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.MessageUpdateTile;
import buildcraft.lib.net.PacketBufferBC;

/** Defines some sort of separate element that exists on both the server and client. Doesn't draw directly. */
public abstract class Widget_Neptune<C extends ContainerBC_Neptune> implements IPayloadReceiver {
    public final C container;

    public Widget_Neptune(C container) {
        this.container = container;
    }

    public boolean isRemote() {
        return container.player.getWorld().isClient;
    }

    protected final void sendWidgetData(IPayloadWriter writer) {
        container.sendWidgetData(this, writer);
    }

    // STUB(R.Chen): IMessage return → MessageUpdateTile; MessageContext → Object (Phase 5 net migration).
    public MessageUpdateTile handleWidgetDataServer(Object ctx, PacketBufferBC buffer) throws IOException {
        return null;
    }

    @Environment(EnvType.CLIENT)
    public MessageUpdateTile handleWidgetDataClient(Object ctx, PacketBufferBC buffer) throws IOException {
        return null;
    }

    @Override
    public MessageUpdateTile receivePayload(Object ctx, PacketBufferBC buffer) throws IOException {
        if (container.player.getWorld().isClient) {
            return handleWidgetDataClient(ctx, buffer);
        } else {
            return handleWidgetDataServer(ctx, buffer);
        }
    }
}
