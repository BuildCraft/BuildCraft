/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0.
 */
package buildcraft.lib.compat;

import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;

/** 1.12.2 → 1.20.1 compat helpers for World methods that changed. */
public final class WorldCompat {

    private WorldCompat() {}

    /** Replaces World.getCombinedLight(pos, minLight) - returns packed LM texture coordinates. */
    public static int getCombinedLight(World world, BlockPos pos, int minLight) {
        int block = Math.max(world.getLightLevel(LightType.BLOCK, pos), minLight);
        int sky = world.getLightLevel(LightType.SKY, pos);
        return LightmapTextureManager.pack(sky, block);
    }
}
