/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui;

import java.io.IOException;

import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.net.IPayloadReceiver;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;

/** Defines some sort of separate element that exists on both the server and client. Doesn't draw directly. */
public abstract class Widget_Neptune<C extends ContainerBC_Neptune> implements IPayloadReceiver {
    public final C container;

    public Widget_Neptune(C container) {
        this.container = container;
    }

    public boolean isRemote() {
        return container.player.world.isRemote;
    }

    // Net updating

    protected final void sendWidgetData(IPayloadWriter writer) {
        container.sendWidgetData(this, writer);
    }

    public IMessage handleWidgetDataServer(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public IMessage handleWidgetDataClient(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        return null;
    }

    @Override
    public IMessage receivePayload(MessageContext ctx, PacketBufferBC buffer) throws IOException {
        if (ctx.side == Side.CLIENT) {
            return handleWidgetDataClient(ctx, buffer);
        } else {
            return handleWidgetDataServer(ctx, buffer);
        }
    }
}
