package ct.buildcraft.builders.tile;

import ct.buildcraft.builders.BCBuildersBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileQuarryDrillCollision extends BlockEntity {
    private static final String NBT_OWNER = "owner";

    private BlockPos owner;

    public TileQuarryDrillCollision(BlockPos pos, BlockState state) {
        super(BCBuildersBlocks.QUARRY_DRILL_COLLISION_TILE_BC8.get(), pos, state);
    }

    public void setOwner(BlockPos owner) {
        this.owner = owner.immutable();
        setChanged();
    }

    public BlockPos getOwner() {
        return owner;
    }

    public void update() {
        if (level == null || level.isClientSide) {
            return;
        }
        if (owner == null) {
            removeSelf();
            return;
        }
        BlockEntity ownerTile = level.getBlockEntity(owner);
        if (!(ownerTile instanceof TileQuarry quarry) || !quarry.shouldKeepCollisionBlock(worldPosition)) {
            removeSelf();
        }
    }

    private void removeSelf() {
        if (level != null && level.getBlockState(worldPosition).getBlock() == BCBuildersBlocks.QUARRY_DRILL_COLLISION.get()) {
            level.setBlock(worldPosition, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    @Override
    public void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);
        if (owner != null) {
            nbt.put(NBT_OWNER, NbtUtils.writeBlockPos(owner));
        }
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        owner = nbt.contains(NBT_OWNER) ? NbtUtils.readBlockPos(nbt.getCompound(NBT_OWNER)) : null;
    }
}
