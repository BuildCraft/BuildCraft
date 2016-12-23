package buildcraft.builders.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.core.BCLog;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.NBTUtilBC;

public class EntityQuarryFrame extends Entity {
    private static final DataParameter<BlockPos> TILE_POS = EntityDataManager.createKey(EntityQuarryFrame.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Byte> AXIS = EntityDataManager.createKey(EntityQuarryFrame.class, DataSerializers.BYTE);
    private static final DataParameter<Integer> LIST_INDEX = EntityDataManager.createKey(EntityQuarryFrame.class, DataSerializers.VARINT);

    private TileQuarry quarry = null;

    public EntityQuarryFrame(World world) {
        super(world);
        dataManager.register(TILE_POS, BlockPos.ORIGIN);
        dataManager.register(AXIS, (byte) 0);
        dataManager.register(LIST_INDEX, 0);
    }

    public EntityQuarryFrame(World world, TileQuarry quarry, BlockPos entityPos, Axis axis, int listIndex) {
        super(world);
        this.quarry = quarry;
        dataManager.register(TILE_POS, quarry.getPos());
        setPosition(entityPos.getX(), entityPos.getY(), entityPos.getZ());
        dataManager.register(AXIS, (byte) axis.ordinal());
        dataManager.register(LIST_INDEX, listIndex);
    }

    @Override
    protected void entityInit() {}

    public BlockPos getTilePos() {
        return dataManager.get(TILE_POS);
    }

    public Axis getAxis() {
        return Axis.values()[dataManager.get(AXIS)];
    }

    public int getListIndex() {
        return dataManager.get(LIST_INDEX);
    }

    public TileQuarry getTile() {
        return quarry;
    }

    public boolean isConnected(TileQuarry tileQuarry) {
        return quarry == tileQuarry;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (world.isRemote) {
            if (quarry == null) {
                // try to pair with a quarry
                TileEntity tile = world.getTileEntity(getTilePos());
                if (tile instanceof TileQuarry) {
                    TileQuarry potentialQuarry = (TileQuarry) tile;
                    if (potentialQuarry.tryPairEntity(this, getAxis(), getListIndex())) {
                        quarry = potentialQuarry;
                    } else {
                        BCLog.logger.info("[quarry.frame] Failed to pair with a quarry!");
                        setDead();
                    }
                } else {
                    setDead();
                }
            } else if (quarry.isInvalid() || !quarry.isPaired(this, getAxis(), getListIndex())) {
                BCLog.logger.info("[quarry.frame] Quarry was invalid (or we were no longer paired) @ " + getPosition());
                setDead();
            }
        } else if (quarry == null || quarry.isInvalid() || !quarry.isPaired(this, getAxis(), getListIndex())) {
            BCLog.logger.info("[quarry.frame] Quarry was invalid (or we were no longer paired) @ " + getPosition());
            // Uh-oh, remove ourselves
            setDead();
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setTag("tile_pos", NBTUtilBC.writeBlockPos(getTilePos()));
        nbt.setByte("type", (byte) getAxis().ordinal());
        nbt.setInteger("list_index", dataManager.get(LIST_INDEX));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        dataManager.set(TILE_POS, NBTUtilBC.readBlockPos(nbt.getTag("tile_pos")));
        dataManager.set(AXIS, nbt.getByte("type"));
        dataManager.set(LIST_INDEX, nbt.getInteger("list_index"));
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        TileQuarry tile = getTile();
        if (tile != null && tile.frameBox.isInitialized() && tile.drillPos != null) {
            final BlockPos min = tile.frameBox.min();
            final BlockPos max = tile.frameBox.max();
            switch (getAxis()) {
                case X:
                    return BoundingBoxUtil.makeFrom(//
                            new Vec3d(tile.drillPos.xCoord + 0.5, max.getY() + 0.5, min.getZ() + 1),//
                            new Vec3d(tile.drillPos.xCoord + 0.5, max.getY() + 0.5, max.getZ()),//
                            4 / 16D//
                    );
                case Y:
                    return BoundingBoxUtil.makeFrom(//
                            new Vec3d(tile.drillPos.xCoord + 0.5, max.getY() + 0.5, tile.drillPos.zCoord + 0.5),//
                            new Vec3d(tile.drillPos.xCoord + 0.5, tile.drillPos.yCoord + 1 + 0.5, tile.drillPos.zCoord + 0.5),//
                            4 / 16D//
                    );
                case Z:
                    return BoundingBoxUtil.makeFrom(//
                            new Vec3d(min.getX() + 1, max.getY() + 0.5, tile.drillPos.zCoord + 0.5),//
                            new Vec3d(max.getX(), max.getY() + 0.5, tile.drillPos.zCoord + 0.5),//
                            4 / 16D//
                    );
            }
        }
        return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBox(Entity entity) {
        return getEntityBoundingBox();
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return getEntityBoundingBox();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(0, 0, 0, 0, 0, 0);
    }
}
