/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import buildcraft.api.core.IFakeWorld;
import buildcraft.api.schematics.ISchematicBlock;
import buildcraft.builders.BCBuilders;
import buildcraft.lib.misc.ProfilerUtil;
import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

@SuppressWarnings("NullableProblems")
@OnlyIn(Dist.CLIENT)
public class FakeWorld extends IFakeWorld {
    // private static final ResourceKey<Biome> BIOME = Biomes.PLAINS;
    @SuppressWarnings("WeakerAccess")
    public static final BlockPos BLUEPRINT_OFFSET = new BlockPos(0, 127, 0);
    public static final ResourceKey<DimensionType> DIMENSION_TYPE_KEY = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceLocation.tryBuild(BCBuilders.MODID, "fake"));
    private static final WritableLevelData LEVEL_DATA = new ClientLevel.ClientLevelData(Difficulty.PEACEFUL, true, false);

    private final FakeChunkProvider chunkProvider;
    final EntityLookup<Entity> entityStorage;
    final EntitySectionStorage<Entity> sectionStorage;
    private final LongSet tickingChunks = new LongOpenHashSet();
    LevelEntityGetter<Entity> ENTITY_GETTER;

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
                ResourceKey.create(
                        Registries.DIMENSION,
                        new ResourceLocation(BCBuilders.MODID, "fake")
                ),
                Minecraft.getInstance().level.registryAccess(),
                Minecraft.getInstance().level.registryAccess().registryOrThrow(Registries.DIMENSION_TYPE).getHolderOrThrow(DIMENSION_TYPE_KEY),
                ProfilerUtil::newProfiler,
                /*pIsClientSide*/ true,
                false,
                0,
                1000000
        );
        chunkProvider = new FakeChunkProvider(this);

        this.entityStorage = new EntityLookup<>();
        this.sectionStorage = new EntitySectionStorage<>(Entity.class, (p_157647_) ->
        {
            return this.tickingChunks.contains(p_157647_) ? Visibility.TICKING : Visibility.TRACKED;
        });
        ENTITY_GETTER = new LevelEntityGetterAdapter<>(this.entityStorage, this.sectionStorage);
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
                            setBlock(pos, Blocks.QUARTZ_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
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
    public BlockEntity getBlockEntity(BlockPos p_46716_) {
        return this.getChunkAt(p_46716_).getBlockEntity(p_46716_, LevelChunk.EntityCreationType.IMMEDIATE);
    }

    // Calen: only in ServerLevel
//    @Override
//    public BlockPos getSpawnPoint()
//    public BlockPos getSharedSpawnPos() {
//        return BLUEPRINT_OFFSET;
//    }

    @Override

    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
//    protected IChunkProvider createChunkProvider()
    public FakeChunkProvider getChunkSource() {
        return this.chunkProvider;
    }

    @Override
    public void levelEvent(@Nullable Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_) {

    }

    @Override
    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, Context p_220406_) {

    }

    @Override
    public void gameEvent(@Nullable Entity p_151549_, GameEvent p_151550_, BlockPos p_151551_) {

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
    public Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
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
    public void playSeededSound(@org.jetbrains.annotations.Nullable Player p_262953_, double p_263004_, double p_263398_, double p_263376_, Holder<SoundEvent> p_263359_, SoundSource p_263020_, float p_263055_, float p_262914_, long p_262991_) {

    }

    @Override
    public void playSeededSound(@org.jetbrains.annotations.Nullable Player p_220372_, Entity p_220373_, Holder<SoundEvent> p_263500_, SoundSource p_220375_, float p_220376_, float p_220377_, long p_220378_) {

    }

    @Override
    public void playSound(@Nullable Player p_46543_, double p_46544_, double p_46545_, double p_46546_, SoundEvent p_46547_, SoundSource p_46548_, float p_46549_, float p_46550_) {

    }

    @Override
    public void playSound(@Nullable Player p_46551_, Entity p_46552_, SoundEvent p_46553_, SoundSource p_46554_, float p_46555_, float p_46556_) {

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
    public MapItemSavedData getMapData(String p_46650_) {
        return null;
    }

    @Override
    public void setMapData(String p_151533_, MapItemSavedData p_151534_) {

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
    protected LevelEntityGetter<Entity> getEntities() {
        return ENTITY_GETTER;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return null;
    }

    @Override
    public float getShade(Direction p_45522_, boolean p_45523_) {
        return 0;
    }

    @Override
    public List<? extends Player> players() {
        return Lists.newArrayList();
    }
}
