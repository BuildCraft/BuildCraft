/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

// STUB(R.Chen): Forge MinecraftForge.EVENT_BUS subscribers → Fabric lifecycle callbacks.
// World-tick (wire system), server-tick (item queue), chunk-watch (player tracking),
// texture-stitch (wire cache clear) are wired in BCTransportInitializer (Phase 5).
public enum BCTransportEventDist {
    INSTANCE;
}
