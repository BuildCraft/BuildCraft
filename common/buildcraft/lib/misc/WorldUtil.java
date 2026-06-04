/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.world.GameMode;
import net.minecraft.world.World;

public class WorldUtil {
    public static boolean isWorldCreative(World world) {
        // Yarn 1.20.1: WorldProperties (returned by getLevelProperties()) does not expose
        // getGameMode() directly. MinecraftServer.getDefaultGameMode() is the reliable path;
        // returns false for client worlds (getServer() == null).
        // TODO(R.Chen): Verify behavior for integrated-server client worlds.
        return world.getServer() != null
            && world.getServer().getDefaultGameMode() == GameMode.CREATIVE;
    }
}
