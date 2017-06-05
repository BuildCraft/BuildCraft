/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkGenerator;

import buildcraft.lib.BCLib;

public class BlankWorldProvider extends WorldProvider {
    @Override
    public DimensionType getDimensionType() {
        return BCLib.blueprintDimensionType;
    }

    @Override
    public boolean canRespawnHere() {
        return false;
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return super.getBiomeProvider();
    }

    @Override
    public boolean hasSkyLight() {
        return super.hasSkyLight();
    }

    @Override
    public boolean hasNoSky() {
        return super.hasNoSky();
    }

    @Override
    public String getSaveFolder() {
        return "The dimension of blueprints";
    }


    @Override
    public boolean canDoLightning(Chunk chunk) {
        return false;
    }

    @Override
    public boolean canDoRainSnowIce(Chunk chunk) {
        return false;
    }

    @Override
    public void onPlayerAdded(EntityPlayerMP player) {
        //player.getEntityWorld().getMinecraftServer().getPlayerList().transferPlayerToDimension(player, 0, new BCTeleporter(player.getServerWorld(), 0, 0, 0));
    }

    @Override
    public IChunkGenerator createChunkGenerator() {
        return new BlankChunkGenerator(super.world);
    }

    @Override
    public Biome getBiomeForCoords(BlockPos pos) {
        return FakeBiomeProvider.BIOME;
    }
}
