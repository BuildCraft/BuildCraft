/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.dimension;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServerMulti;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeProvider;
import net.minecraft.world.chunk.Chunk;

import net.minecraftforge.common.DimensionManager;

import buildcraft.api.schematics.SchematicBlockContext;

import buildcraft.lib.BCLib;

public class FakeWorldServer extends WorldServerMulti implements IFakeWorld {
    private List<ItemStack> drops = new ArrayList<>();
    public static FakeWorldServer INSTANCE;
    public boolean editable = true;
    private FakeChunkProvider provider;

    public FakeWorldServer(MinecraftServer server) {
        super(server, DimensionManager.getWorld(0).getSaveHandler(), BCLib.DIMENSION_ID, DimensionManager.getWorld(0), server.profiler);
        provider = new FakeChunkProvider(this);
        INSTANCE = this;
    }

    @Override
    public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
        return getFakeChunkProvider().getLoadedChunk(chunkX, chunkZ);
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

    public boolean isAcceptableForBlueprint(SchematicBlockContext context) {
        BlockPos testPos = new BlockPos(-100, 5, -100);
        boolean oldEditable = editable;
        editable = true;
        setBlockState(testPos, context.blockState);
        TileEntity te = context.block.createTileEntity(this, context.blockState);
        TileEntity original = context.world.getTileEntity(context.pos);
        if (te == null || original == null) {
            return false;
        }
        addTileEntity(te);
        NBTTagCompound compound = te.serializeNBT();
        compound.removeTag("x");
        compound.removeTag("y");
        compound.removeTag("z");
        compound.removeTag("id");

        NBTTagCompound toCompare = original.serializeNBT();
        toCompare.removeTag("x");
        toCompare.removeTag("y");
        toCompare.removeTag("z");
        toCompare.removeTag("id");

        boolean equals = compound.equals(toCompare);
        setBlockToAir(testPos);
        removeTileEntity(testPos);
        editable = oldEditable;
        return equals;
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
                drops.add(((EntityItem) entity).getEntityItem());
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
    public BlockPos getSpawnPoint() {
        return BLUEPRINT_OFFSET;
    }

    @Override
    protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
        return true;
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return FakeBiomeProvider.BIOME;
    }

    @Override
    public Biome getBiomeForCoordsBody(BlockPos pos) {
        return FakeBiomeProvider.BIOME;
    }

    @Override
    public BiomeProvider getBiomeProvider() {
        return new FakeBiomeProvider();
    }

    @Override
    public File getChunkSaveLocation() {
        return new File("Wherever");
    }

    @Override
    public FakeChunkProvider getFakeChunkProvider() {
        return provider;
    }
}
