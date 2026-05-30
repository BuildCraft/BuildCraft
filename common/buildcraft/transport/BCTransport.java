/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

// STUB(R.Chen): was Forge @Mod(BCTransport.MODID) with FML event handlers.
// Rewritten as BCTransportInitializer (implements ModInitializer) in Phase 4F.
public class BCTransport {
    public static final String MODID = "buildcrafttransport";
    public static final BCTransport INSTANCE = new BCTransport();

    public static void preInit() {
        BCTransportRegistries.preInit();
        BCTransportConfig.preInit();
        BCTransportBlocks.preInit();
        BCTransportPipes.preInit();
        BCTransportPlugs.preInit();
        BCTransportItems.preInit();
        BCTransportStatements.preInit();
    }

    public static void init() {
        BCTransportRegistries.init();
    }

    public static void postInit() {}
}
