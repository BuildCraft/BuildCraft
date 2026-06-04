/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */
package buildcraft.lib;

import java.util.function.Supplier;

import net.minecraft.world.World;

/**
 * Fabric 1.20.1 replacement for the Forge {@code @SidedProxy} BCLibProxy.
 *
 * The inner ServerProxy / ClientProxy classes and the {@code @SidedProxy} DI pattern are dropped.
 * {@link BCLibClientInitializer} installs a client-world supplier at startup via
 * {@link #setClientWorldSupplier(Supplier)}, allowing common code to call {@link #getClientWorld()}
 * without importing client-only classes.
 *
 * TODO(R.Chen): getPlayerForContext, addScheduledTask, getServerTile, getGameDirectory,
 * getLoadedResourcePackFiles, getStreamForIdentifier — port or stub in a later pass.
 */
public class BCLibProxy {
    private static Supplier<World> clientWorldSupplier = () -> null;
    private static final BCLibProxy INSTANCE = new BCLibProxy();

    private BCLibProxy() {}

    public static BCLibProxy getProxy() {
        return INSTANCE;
    }

    /**
     * Called from {@link BCLibClientInitializer} to wire in
     * {@code () -> MinecraftClient.getInstance().world}.
     */
    public static void setClientWorldSupplier(Supplier<World> supplier) {
        clientWorldSupplier = supplier;
    }

    /** Returns the current client-side world, or {@code null} on the server. */
    public World getClientWorld() {
        return clientWorldSupplier.get();
    }
}
