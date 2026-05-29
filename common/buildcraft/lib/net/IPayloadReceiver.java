/*
 * Copyright (c) 2016 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.lib.net;

import java.io.IOException;

public interface IPayloadReceiver {
    // STUB(R.Chen): Forge IMessage return + MessageContext arg replaced — returns the Fabric
    // MessageUpdateTile (a FabricPacket) and takes a generic context until BCNetworkManager
    // supplies a Fabric-native receiver context (player + side).
    MessageUpdateTile receivePayload(Object ctx, PacketBufferBC buffer) throws IOException;
}
