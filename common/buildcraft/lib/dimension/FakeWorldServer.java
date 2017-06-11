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
import java.util.concurrent.CopyOnWriteArrayList;
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
import net.minecraft.profiler.Profiler;
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
import buildcraft.lib.misc.data.Box;

public class FakeWorldServer extends WorldServerMulti implements IFakeWorld {
    private List<ItemStack> drops = new CopyOnWriteArrayList<>();
    public static FakeWorldServer INSTANCE;
    private FakeChunkProvider provider;
    private List<Box> locks = new CopyOnWriteArrayList<>();

    public FakeWorldServer(MinecraftServer server) {
        super(server, DimensionManager.getWorld(0).getSaveHandler(), BCLib.DIMENSION_ID, DimensionManager.getWorld(0), new Profiler());
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
        return equals;
    }

    private boolean isLocked(BlockPos pos) {
        return locks.stream().anyMatch(lock-> lock.contains(pos));
    }

    public void lock(Box box) {
        locks.add(box);
    }

    public void unlock(Box box) {
        locks.remove(box);
    }

    @Override
    public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
        if (!isLocked(pos)) {
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
        if (!isLocked(pos)) {
            super.updateBlockTick(pos, block, delay, priority);
        }
    }

    @Override
    public void scheduleBlockUpdate(BlockPos pos, Block block, int delay, int priority) {
        if (!isLocked(pos)) {
            super.scheduleBlockUpdate(pos, block, delay, priority);
        }
    }

    @Override
    protected void updateBlocks() {

    }

    @Override
    public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
        if (!isLocked(pos)) {
            super.sendBlockBreakProgress(breakerId, pos, progress);
        }
    }

    @Override
    public boolean addTileEntity(TileEntity tile) {
        return isLocked(tile.getPos()) || super.addTileEntity(tile);
    }

    @Override
    public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
        tileEntityCollection.forEach(this::addTileEntity);
    }

    @Override
    public void setTileEntity(BlockPos pos, @Nullable TileEntity tileEntity) {
        if (!isLocked(pos)) {
            super.setTileEntity(pos, tileEntity);
        }
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        if (!isLocked(pos)) {
            super.removeTileEntity(pos);
        }
    }

    @Override
    public void markTileEntityForRemoval(TileEntity tileEntity) {
        if (!isLocked(tileEntity.getPos())) {
            super.markTileEntityForRemoval(tileEntity);
        }
    }

    @Override
    public boolean spawnEntity(Entity entity) {
        if (!isLocked(entity.getPosition())) {
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
        if (!isLocked(entity.getPosition())) {
            super.removeEntity(entity);
        }
    }

    @Override
    public void setEntityState(Entity entity, byte state) {
        if (!isLocked(entity.getPosition())) {
            super.setEntityState(entity, state);
        }
    }

    @Override
    public void removeEntityDangerously(Entity entity) {
        if (isLocked(entity.getPosition())) {
            super.removeEntityDangerously(entity);
        }
    }

    @Override
    public void updateEntity(Entity entity) {
        if (!isLocked(entity.getPosition())) {
            super.updateEntity(entity);
        }
    }

    @Override
    public void updateEntityWithOptionalForce(Entity entity, boolean forceUpdate) {
        if (!isLocked(entity.getPosition())) {
            super.updateEntityWithOptionalForce(entity, forceUpdate);
        }
    }

    @Override
    public void loadEntities(Collection<Entity> entityCollection) {
        entityCollection.forEach(this::spawnEntity);
    }

    @Override
    public void unloadEntities(Collection<Entity> entityCollection) {
       entityCollection.forEach(this::removeEntity);
    }

    @Override
    public BlockPos getSpawnPoint() {
        return new BlockPos(-100, 100, -100);
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
        return new File("BC_DIM_BP");
    }

    @Override
    public FakeChunkProvider getFakeChunkProvider() {
        return provider;
    }

    @Override
    public int countEntities(Class<?> entityType) {
        return 0;
    }

    @Override
    public void tick() {

    }
}
