/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.misc;

import net.minecraft.world.World;

public class WorldUtil {
    public static boolean isWorldCreative(World world) {
        // TODO(R.Chen): Yarn 1.20.1 renamed World.getWorldInfo() -> getLevelProperties()
        // and GameType.getGameType() -> getGameMode(). Update once we wire up a real
        // 1.20.1 compile classpath and can name-check against the remapped jar.
        return world.getWorldInfo().getGameType().isCreative();
    }
}
