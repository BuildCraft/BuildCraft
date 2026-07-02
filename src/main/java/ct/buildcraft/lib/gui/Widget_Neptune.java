/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.lib.gui;

import java.io.IOException;

import ct.buildcraft.lib.net.IPayloadReceiver;
import ct.buildcraft.lib.net.IPayloadWriter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

/** Defines some sort of separate element that exists on both the server and client. Doesn't draw directly. */
public abstract class Widget_Neptune<C extends MenuBC_Neptune> implements IPayloadReceiver {
    public final C container;

    public Widget_Neptune(C container) {
        this.container = container;
    }

    public boolean isRemote() {
        return container.playerInventory.player.level.isClientSide;
    }

    // Net updating

    protected final void sendWidgetData(IPayloadWriter writer) {
        container.sendWidgetData(this, writer);
    }

    public void handleWidgetDataServer(NetworkEvent.Context ctx, FriendlyByteBuf buffer) throws IOException {
    }

    @OnlyIn(Dist.CLIENT)
    public void handleWidgetDataClient(NetworkEvent.Context ctx, FriendlyByteBuf buffer) throws IOException {
    }

    @Override
    public void receivePayload(NetworkEvent.Context ctx, FriendlyByteBuf buffer) throws IOException {
        if (ctx.getDirection().getReceptionSide() == LogicalSide.CLIENT) {
            handleWidgetDataClient(ctx, buffer);
        } else {
            handleWidgetDataServer(ctx, buffer);
        }
    }
}
