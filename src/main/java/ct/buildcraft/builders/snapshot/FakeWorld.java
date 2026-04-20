/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package ct.buildcraft.builders.snapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import ct.buildcraft.api.schematics.ISchematicBlock;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientLevel.ClientLevelData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEvent.Context;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.ticks.LevelTickAccess;
import net.minecraft.world.ticks.TickPriority;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.crafting.conditions.ICondition.IContext;

@SuppressWarnings("NullableProblems")
@OnlyIn(Dist.CLIENT)
public class FakeWorld extends Level {
    private static final ResourceKey<Biome> BIOME = Biomes.PLAINS;
    @SuppressWarnings("WeakerAccess")
    public static final BlockPos BLUEPRINT_OFFSET = new BlockPos(0, 127, 0);
    
    public final FakeChunkProvider chunkProvider = new FakeChunkProvider(this);
    
    protected final List<Entity> entitys = new ArrayList<Entity>();
    
    protected final LevelEntityGetter<Entity> entityGetter = new LevelEntityGetter(){
    	
		@Override
		public EntityAccess get(int p_156931_) {
			return null;
		}

		@Override
		public EntityAccess get(UUID p_156939_) {
			return null;
		}

		@Override
		public Iterable getAll() {
			return null;
		}

		@Override
		public void get(EntityTypeTest p_156935_, Consumer p_156936_) {
		}

		@Override
		public void get(AABB p_156937_, Consumer p_156938_) {
		}

		@Override
		public void get(EntityTypeTest p_156932_, AABB p_156933_, Consumer p_156934_) {
		}
    	
    };
    
    public final ClientLevel superLevel ;
    
    @SuppressWarnings("WeakerAccess")
    public FakeWorld(ClientLevel level) {
        super(
            new ClientLevelData(Difficulty.EASY, false, false),
            Level.OVERWORLD,
            level.dimensionTypeRegistration(),
            level.getProfilerSupplier(),
            true,
            true,
            BiomeManager.obfuscateSeed(0),
            1000000);
        superLevel = level;
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
                            setBlock(pos, Blocks.QUARTZ_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
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
    
    @Override
	public BlockPos getSharedSpawnPos() {
    	return BLUEPRINT_OFFSET;
	}

    @Override
	public ChunkSource getChunkSource() {
		return chunkProvider;
	}

	@Override
	public boolean hasChunk(int p_46794_, int p_46795_) {
		return true;
	}

    @Override
	public boolean hasChunkAt(int p_151578_, int p_151579_) {
		return true;
	}
    
    //should not call the method below
    @Deprecated
    public void scheduleTick(BlockPos p_186465_, Block p_186466_, int p_186467_, TickPriority p_186468_) {
    }
    
    @Deprecated
    public void scheduleTick(BlockPos p_186461_, Block p_186462_, int p_186463_) {
    }
    
    @Deprecated
	@Override
	public LevelTickAccess<Block> getBlockTicks() {
		return null;
	}
    
    @Deprecated
	@Override
	public LevelTickAccess<Fluid> getFluidTicks() {
		return null;
	}

    @Deprecated
	@Override
	public void levelEvent(Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_) {
	}

    @Deprecated
	@Override
	public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, Context p_220406_) {
	}

    @Deprecated
	@Override
	public RegistryAccess registryAccess() {
		return superLevel.registryAccess();
	}

    @Deprecated
	@Override
	public List<? extends Player> players() {
		return ImmutableList.of();
	}

    @Deprecated
	@Override
	public Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
		return superLevel.getUncachedNoiseBiome(p_204159_, p_204160_, p_204161_);
	}

    @Deprecated
	@Override
	public float getShade(Direction p_45522_, boolean p_45523_) {
		return 0;
	}

    @Deprecated
	@Override
	public void sendBlockUpdated(BlockPos p_46612_, BlockState p_46613_, BlockState p_46614_, int p_46615_) {
	}

    @Deprecated
	@Override
	public void playSeededSound(Player p_220363_, double p_220364_, double p_220365_, double p_220366_,
			SoundEvent p_220367_, SoundSource p_220368_, float p_220369_, float p_220370_, long p_220371_) {
	}

    @Deprecated
	@Override
	public void playSeededSound(Player p_220372_, Entity p_220373_, SoundEvent p_220374_, SoundSource p_220375_,
			float p_220376_, float p_220377_, long p_220378_) {
	}

	@Override
	public String gatherChunkSourceStats() {
		return "FakeChunk";
	}

	@Deprecated
	@Override
	public Entity getEntity(int p_46492_) {
		return null;
	}

	@Deprecated
	@Override
	public MapItemSavedData getMapData(String p_46650_) {
		return null;
	}

	@Deprecated
	@Override
	public void setMapData(String p_151533_, MapItemSavedData p_151534_) {
	}

	@Deprecated
	@Override
	public int getFreeMapId() {
		return 0;
	}

	@Deprecated
	@Override
	public void destroyBlockProgress(int p_46506_, BlockPos p_46507_, int p_46508_) {
	}

	@Deprecated
	@Override
	public Scoreboard getScoreboard() {
		return new Scoreboard();
	}

	@Deprecated
	@Override
	public RecipeManager getRecipeManager() {
		return new RecipeManager(IContext.EMPTY);
	}

	public void addEntity(Entity entity) {
		this.entitys.add(entity);
	}
	
	@Override
	protected LevelEntityGetter<Entity> getEntities() {
		return null;
	}
}
