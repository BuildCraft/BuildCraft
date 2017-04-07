package buildcraft.builders.snapshot;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.storage.SaveHandlerMP;
import net.minecraft.world.storage.WorldInfo;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class FakeWorld extends World {
    public static FakeWorld INSTANCE = new FakeWorld();

    public static final BlockPos BLUEPRINT_OFFSET = new BlockPos(0, 127, 0);
    private final List<ItemStack> drops = new ArrayList<>();
    public boolean editable = true;

    public FakeWorld() {
        super(
                new SaveHandlerMP(),
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
                Minecraft.getMinecraft().mcProfiler,
                false
        );
        chunkProvider = new FakeChunkProvider(this);
    }

    public void clear() {
        ((FakeChunkProvider) chunkProvider).chunks.clear();
    }

    public void uploadBlueprint(Blueprint blueprint) {
        for (int z = -1; z <= blueprint.size.getZ(); z++) {
            for (int y = -1; y <= blueprint.size.getY(); y++) {
                for (int x = -1; x <= blueprint.size.getX(); x++) {
                    BlockPos pos = new BlockPos(x, y, z).add(BLUEPRINT_OFFSET);
                    if (x == -1 || y == -1 || z == -1 ||
                            x == blueprint.size.getX() ||
                            y == blueprint.size.getY() ||
                            z == blueprint.size.getZ()) {
                        setBlockState(pos, Blocks.STONE.getDefaultState());
                    } else {
                        SchematicBlock schematicBlock = blueprint.data[x][y][z];
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
        if (editable) {
            return super.addTileEntity(tile);
        } else {
            return true;
        }
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
}
