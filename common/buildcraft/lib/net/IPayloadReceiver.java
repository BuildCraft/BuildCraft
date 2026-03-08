/* Copyright (c) 2016 SpaceToad and the BuildCraft team
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/. */
package buildcraft.lib.net;

import buildcraft.api.net.IMessage;
import net.minecraftforge.network.NetworkEvent;

import java.io.IOException;

public interface IPayloadReceiver {
    IMessage receivePayload(NetworkEvent.Context ctx, PacketBufferBC buffer) throws IOException;
}
