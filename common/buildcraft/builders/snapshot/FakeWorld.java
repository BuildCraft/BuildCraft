/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.builders.snapshot;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.DimensionType;
import net.minecraft.world.GameType;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.structure.template.TemplateManager;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import buildcraft.api.schematics.ISchematicBlock;

public class FakeWorld extends World {
    public static final Biome BIOME = Biomes.PLAINS;
    public static final BlockPos BLUEPRINT_OFFSET = new BlockPos(0, 127, 0);

    public static FakeWorld INSTANCE = new FakeWorld();

    private final List<ItemStack> drops = new ArrayList<>();
    public boolean editable = true;

    public FakeWorld() {
        super(
            new ISaveHandler() {
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
                    return null;
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
            },
            new WorldInfo(
                new WorldSettings(
                    0,
                    GameType.CREATIVE,
                    true,
                    false,
                    WorldType.DEFAULT
                ),
                "fake"
            ),
            new WorldProvider() {
                @Override
                public DimensionType getDimensionType() {
                    return DimensionType.OVERWORLD;
                }
            },
            new Profiler(),
            false
        );
        chunkProvider = new FakeChunkProvider(this);
    }

    public void clear() {
        ((FakeChunkProvider) chunkProvider).chunks.clear();
    }

    public void uploadBlueprint(Blueprint blueprint, boolean useStone) {
        for (int z = -1; z <= blueprint.size.getZ(); z++) {
            for (int y = -1; y <= blueprint.size.getY(); y++) {
                for (int x = -1; x <= blueprint.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).add(BLUEPRINT_OFFSET);
                    if (x == -1 || y == -1 || z == -1 ||
                        x == blueprint.size.getX() ||
                        y == blueprint.size.getY() ||
                        z == blueprint.size.getZ()) {
                        setBlockState(pos, useStone ? Blocks.STONE.getDefaultState() : Blocks.AIR.getDefaultState());
                    } else {
                        ISchematicBlock<?> schematicBlock = blueprint.palette.get(blueprint.data[x][y][z]);
                        schematicBlock.buildWithoutChecks(this, pos);
                    }
                }
            }
        }
    }

    public List<ItemStack> breakBlockAndGetDrops(BlockPos pos) {
        getBlockState(pos).getBlock().breakBlock(this, pos, getBlockState(pos));
        List<ItemStack> dropsCopy = new ArrayList<>(drops);
        drops.clear();
        return dropsCopy;
    }

    public List<ItemStack> killEntityAndGetDrops(Entity entity) {
        entity.move(MoverType.PLAYER, 1, 1, 1);
        if (drops.isEmpty()) {
            entity.isDead = false;
            entity.attackEntityFrom(
                DamageSource.causePlayerDamage(
                    new EntityPlayer(
                        this,
                        new GameProfile(UUID.randomUUID(), "fake")
                    ) {
                        @Override
                        public boolean isSpectator() {
                            return false;
                        }

                        @Override
                        public boolean isCreative() {
                            return false;
                        }
                    }
                ),
                100
            );
        }
        List<ItemStack> dropsCopy = new ArrayList<>(drops);
        drops.clear();
        return dropsCopy;
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (editable) {
            captureBlockSnapshots = true;
            if (pos.getY() < 0 || pos.getY() >= 256) {
                return false;
            } else {
                getChunkFromBlockCoords(pos).setBlockState(pos, newState);
                return true;
            }
        } else {
            return true;
        }
    }

    @Override
    public void updateBlockTick(BlockPos pos, Block block, int delay, int priority) {
        if (editable) {
            super.updateBlockTick(pos, block, delay, priority);
        }
    }

    @Override
    public void scheduleBlockUpdate(BlockPos pos, Block block, int delay, int priority) {
        if (editable) {
            super.scheduleBlockUpdate(pos, block, delay, priority);
        }
    }

    @Override
    protected void updateBlocks() {
        if (editable) {
            super.updateBlocks();
        }
    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        if (editable) {
            super.sendBlockBreakProgress(breakerId, pos, progress);
        }
    }

    @Override
    public boolean addTileEntity(TileEntity tile) {
        return !editable || super.addTileEntity(tile);
    }

    @Override
    public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
        if (editable) {
            super.addTileEntities(tileEntityCollection);
        }
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntity) {
        if (editable) {
            super.setTileEntity(pos, tileEntity);
        }
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        if (editable) {
            super.removeTileEntity(pos);
        }
    }

    @Override
    public void markTileEntityForRemoval(TileEntity tileEntity) {
        if (editable) {
            super.markTileEntityForRemoval(tileEntity);
        }
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        if (editable) {
            return super.spawnEntity(entity);
        } else {
            if (entity instanceof EntityItem) {
                drops.add(((EntityItem) entity).getItem());
            }
            return true;
        }
    }

    @Override
    public void removeEntity(Entity entity) {
        if (editable) {
            super.removeEntity(entity);
        }
    }

    @Override
    public void setEntityState(Entity entity, byte state) {
        if (editable) {
            super.setEntityState(entity, state);
        }
    }

    @Override
    public void removeEntityDangerously(Entity entity) {
        if (editable) {
            super.removeEntityDangerously(entity);
        }
    }

    @Override
    public void updateEntity(Entity entity) {
        if (editable) {
            super.updateEntity(entity);
        }
    }

    @Override
    public void updateEntityWithOptionalForce(Entity entity, boolean forceUpdate) {
        if (editable) {
            super.updateEntityWithOptionalForce(entity, forceUpdate);
        }
    }

    @Override
    public void loadEntities(Collection<Entity> entityCollection) {
        if (editable) {
            super.loadEntities(entityCollection);
        }
    }

    @Override
    public void unloadEntities(Collection<Entity> entityCollection) {
        if (editable) {
            super.unloadEntities(entityCollection);
        }
    }

    @Override
    public void joinEntityInSurroundings(Entity entity) {
        if (editable) {
            super.joinEntityInSurroundings(entity);
        }
    }

    @Override
    public BlockPos getSpawnPoint() {
        return BLUEPRINT_OFFSET;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return chunkProvider;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return BIOME;
    }

    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        return BIOME;
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return new BiomeProvider(worldInfo) {
            @Override
            public List<Biome> getBiomesToSpawnIn() {
                return Collections.emptyList();
            }

            @Override
            public Biome getBiome(BlockPos pos) {
                return BIOME;
            }

            @Override
            public Biome getBiome(BlockPos pos, Biome defaultBiome) {
                return BIOME;
            }

            @Override
            public Biome[] getBiomesForGeneration(Biome[] biomes, int x, int z, int width, int height) {
                return biomes;
            }

            @Override
            public Biome[] getBiomes(@Nullable Biome[] oldBiomeList, int x, int z, int width, int depth) {
                return oldBiomeList;
            }

            @Override
            public Biome[] getBiomes(@Nullable Biome[] listToReuse, int x, int z, int width, int length, boolean cacheFlag) {
                return listToReuse;
            }

            @Override
            public boolean areBiomesViable(int x, int z, int radius, List<Biome> allowed) {
                return false;
            }

            @Nullable
            @Override
            public BlockPos findBiomePosition(int x, int z, int range, List<Biome> biomes, Random random) {
                return BlockPos.ORIGIN;
            }

            @Override
            public void cleanupCache() {
            }

            @Override
            public GenLayer[] getModdedBiomeGenerators(WorldType worldType, long seed, GenLayer[] original) {
                return original;
            }

            @Override
            public boolean isFixedBiome() {
                return true;
            }

            @Override
            public Biome getFixedBiome() {
                return BIOME;
            }
        };
    }
}
