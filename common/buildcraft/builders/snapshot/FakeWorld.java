/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.blocks.BlockConstants;
import buildcraft.api.core.IFakeWorld;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.BCBuilders;
import buildcraft.lib.misc.ProfilerUtil;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITagCollectionSupplier;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DimensionType;
import net.minecraft.world.EmptyTickList;
import net.minecraft.world.ITickList;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.storage.ISpawnWorldInfo;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.OptionalLong;

@SuppressWarnings("NullableProblems")
@OnlyIn(Dist.CLIENT)
public class FakeWorld extends IFakeWorld {
    // private static final RegistryKey<Biome> BIOME = Biomes.PLAINS;
    @SuppressWarnings("WeakerAccess")
    public static final BlockPos BLUEPRINT_OFFSET = new BlockPos(0, 127, 0);
    private static final DimensionType DIMENSION_TYPE = new DimensionType(
            OptionalLong.empty(), // fixedTime
            true, // hasSkylight
            false, // hasCeiling
            false, // ultraWarm
            true, // natural
            1.0D, // coordinateScale
//            false, // createDragonFight
            false, // piglinSafe
            true, // bedWorks
            false, // respawnAnchorWorks
            true, // hasRaids
//            -64, // minY
//            384, // height
            384, // logicalHeight
            BlockTags.INFINIBURN_OVERWORLD.getName(), // infiniburn
            DimensionType.OVERWORLD_EFFECTS, // effectsLocation
            0.0F
    );
    private static final ISpawnWorldInfo LEVEL_DATA = new ClientWorld.ClientWorldInfo(Difficulty.PEACEFUL, true, false);

    private final FakeChunkProvider chunkProvider;
    private final LongSet tickingChunks = new LongOpenHashSet();

    private final DynamicRegistries REGISTRY_ACCESS = DynamicRegistries.builtin();

    @SuppressWarnings("WeakerAccess")
    public FakeWorld() {
//        super(
//                new SaveHandlerMP(),
//                new WorldInfo(
//                        new WorldSettings(
//                                0,
//                                GameType.CREATIVE,
//                                true,
//                                false,
//                                WorldType.DEFAULT
//                        ),
//                        "fake"
//                ),
//                new WorldProvider() {
//                    @Override
//                    public DimensionType getDimensionType() {
//                        return DimensionType.OVERWORLD;
//                    }
//                },
//                new Profiler(),
//                true
//        );
        super(
                LEVEL_DATA,
                RegistryKey.create(
                        Registry.DIMENSION_REGISTRY,
                        new ResourceLocation(BCBuilders.MODID, "fake")
                ),
                DIMENSION_TYPE,
                ProfilerUtil::newProfiler,
                /*pIsClientSide*/ true,
                false,
                0
        );
        chunkProvider = new FakeChunkProvider(this);
    }


    public void clear() {
        ((FakeChunkProvider) chunkProvider).chunks.clear();
    }

    @SuppressWarnings("WeakerAccess")
    public void uploadSnapshot(Snapshot snapshot) {
        for (int z = 0; z < snapshot.size.getZ(); z++) {
            for (int y = 0; y < snapshot.size.getY(); y++) {
                for (int x = 0; x < snapshot.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).offset(BLUEPRINT_OFFSET);
                    if (snapshot instanceof Blueprint) {
                        ISchematicBlock schematicBlock = ((Blueprint) snapshot).palette
                                .get(((Blueprint) snapshot).data[snapshot.posToIndex(x, y, z)]);
                        if (!schematicBlock.isAir()) {
                            schematicBlock.buildWithoutChecks(this, pos);
                        }
                    }
                    if (snapshot instanceof Template) {
                        if (((Template) snapshot).data.get(snapshot.posToIndex(x, y, z))) {
                            setBlock(pos, Blocks.QUARTZ_BLOCK.defaultBlockState(), BlockConstants.UPDATE_ALL);
                        }
                    }
                }
            }
        }
        if (snapshot instanceof Blueprint) {
            ((Blueprint) snapshot).entities.forEach(schematicEntity ->
                    schematicEntity.buildWithoutChecks(this, FakeWorld.BLUEPRINT_OFFSET)
            );
        }
    }

    @Nullable
    @Override
    public TileEntity getBlockEntity(BlockPos p_46716_) {
        return this.getChunkAt(p_46716_).getBlockEntity(p_46716_, Chunk.CreateEntityType.IMMEDIATE);
    }

    // Calen: only in ServerWorld
//    @Override
//    public BlockPos getSpawnPoint()
//    public BlockPos getSharedSpawnPos() {
//        return BLUEPRINT_OFFSET;
//    }

    @Override
    public ITickList<Block> getBlockTicks() {
        return EmptyTickList.empty();
    }

    @Override
    public ITickList<Fluid> getLiquidTicks() {
        return EmptyTickList.empty();
    }

    @Override
//    protected IChunkProvider createChunkProvider()
    public FakeChunkProvider getChunkSource() {
        return this.chunkProvider;
    }

    @Override
    public void levelEvent(@Nullable PlayerEntity p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_) {

    }

    @Override
//    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty)
    public boolean hasChunk(int v, int z) {
        return true;
    }

//    @Override
//    public Holder<Biome> getBiome(BlockPos pos) {
//        return ForgeRegistries.BIOMES.getValue(BIOME.location());
//    }

    @Override
    public Biome getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
        return null;
    }

//    @Override
//    public Biome getBiomeForCoordsBody(BlockPos pos) {
//        return BIOME;
//    }


    @Override
    public void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity p_46543_, double p_46544_, double p_46545_, double p_46546_, SoundEvent p_46547_, SoundCategory p_46548_, float p_46549_, float p_46550_) {

    }

    @Override
    public void playSound(@Nullable PlayerEntity p_46551_, Entity p_46552_, SoundEvent p_46553_, SoundCategory p_46554_, float p_46555_, float p_46556_) {

    }

    @Override
    public String gatherChunkSourceStats() {
        return null;
    }

    @Nullable
    @Override
    public Entity getEntity(int p_46492_) {
        return null;
    }

    @Nullable
    @Override
    public MapData getMapData(String p_46650_) {
        return null;
    }

    @Override
    public void setMapData(MapData p_151534_) {

    }

    @Override
    public int getFreeMapId() {
        return 0;
    }

    @Override
    public void destroyBlockProgress(int p_46506_, BlockPos p_46507_, int p_46508_) {

    }

    @Override
    public Scoreboard getScoreboard() {
        return null;
    }

    @Override
    public RecipeManager getRecipeManager() {
        return null;
    }

    @Override
    public DynamicRegistries registryAccess() {
        return REGISTRY_ACCESS;
    }

    @Override
    public float getShade(Direction p_45522_, boolean p_45523_) {
        return 0;
    }

    @Override
    public List<? extends PlayerEntity> players() {
        return Lists.newArrayList();
    }

    @Override
    public ITagCollectionSupplier getTagManager() {
        return null;
    }
}
