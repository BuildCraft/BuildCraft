/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import java.io.File;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.GameType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class FakeWorldClient extends World implements IFakeWorld {
    private FakeChunkProvider provider;

    public FakeWorldClient() {
        super(new ISaveHandler() {
            @Nullable
            @Override
            public WorldInfo loadWorldInfo() {
                return null;
            }

            @Override
            public void checkSessionLock() throws MinecraftException {

            }

            @Override
            public IChunkLoader getChunkLoader(WorldProvider provider) {
                return new FakeChunkLoader();
            }

            @Override
            public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {

            }

            @Override
            public void saveWorldInfo(WorldInfo worldInformation) {

            }

            @Override
            public IPlayerFileData getPlayerNBTManager() {
                return null;
            }

            @Override
            public void flush() {

            }

            @Override
            public File getWorldDirectory() {
                return null;
            }

            @Override
            public File getMapFileFromName(String mapName) {
                return null;
            }

            @Override
            public TemplateManager getStructureTemplateManager() {
                return null;
            }
        }, new WorldInfo(
                new WorldSettings(0, GameType.NOT_SET, true, false, WorldType.DEFAULT), "The dimension of blueprints"),
                new BlankWorldProvider(),
                Minecraft.getMinecraft().mcProfiler, true);
        provider = new FakeChunkProvider(this);
    }

    @Override
    public FakeChunkProvider getFakeChunkProvider() {
        return provider;
    }

    @Override
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
        return getFakeChunkProvider().getLoadedChunk(chunkX, chunkZ);
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return provider;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }


}
