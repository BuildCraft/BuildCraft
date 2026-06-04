/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

package buildcraft.transport.wire;

// STUB(R.Chen): MessageWireSystemsPowered — Forge SimpleImpl IMessage → Fabric FabricPacket (Phase 5).
// The wire-power sync from server→client is deferred until the network layer is wired.

import java.util.HashMap;
import java.util.Map;

public class MessageWireSystemsPowered {
    public Map<Integer, Boolean> hashesPowered = new HashMap<>();

    public MessageWireSystemsPowered() {
    }

    public MessageWireSystemsPowered(Map<Integer, Boolean> hashesPowered) {
        this.hashesPowered = hashesPowered;
    }
}
