/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.transport;

// STUB(R.Chen): Forge SidedProxy removed. Client-only functionality (particle effects, sound,
// model registration, wire-cache clear) moves to BCTransportClientInitializer (Phase 5).
// Server-side GUI handler replaced by Fabric ScreenHandlerType (Phase 5).
public class BCTransportProxy {

    public static BCTransportProxy getProxy() {
        return new BCTransportProxy();
    }

    public void preInit() {}
    public void init() {}
    public void postInit() {}

    // STUB(R.Chen): Forge IGuiHandler.getServerGuiElement / getClientGuiElement → Phase 5.
    public Object getServerGuiElement(int id, net.minecraft.entity.player.PlayerEntity player,
        net.minecraft.world.World world, int x, int y, int z) {
        return null;
    }
}
