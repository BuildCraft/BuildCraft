package buildcraft.builders.entity;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.NBTUtils;
import buildcraft.lib.misc.VecUtil;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityQuarry extends Entity {
    private static final DataParameter<BlockPos> TILE_POS = EntityDataManager.createKey(EntityQuarry.class, DataSerializers.BLOCK_POS);
    private static final DataParameter<Integer> TYPE = EntityDataManager.createKey(EntityQuarry.class, DataSerializers.VARINT);

    public EntityQuarry(World world) {
        super(world);
        dataManager.register(TILE_POS, BlockPos.ORIGIN);
        dataManager.register(TYPE, 0);
    }

    public EntityQuarry(World world, BlockPos tilePos, BlockPos entityPos, Type type) {
        super(world);
        dataManager.register(TILE_POS, tilePos);
        setPosition(entityPos.getX(), entityPos.getY(), entityPos.getZ());
        dataManager.register(TYPE, type.ordinal());
    }

    public EntityQuarry(World world, BlockPos tilePos, Type type) {
        this(world, tilePos, tilePos, type);
    }

    @Override
    protected void entityInit() {
    }

    public BlockPos getTilePos() {
        return dataManager.get(TILE_POS);
    }

    public Type getType() {
        return Type.values()[dataManager.get(TYPE)];
    }

    public TileQuarry getTile() {
        return worldObj.getTileEntity(getTilePos()) instanceof TileQuarry ? (TileQuarry) worldObj.getTileEntity(getTilePos()) : null;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();

        if(getTile() == null) {
            setDead();
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound compound) {
        compound.setTag("tile_pos", NBTUtils.writeBlockPos(getTilePos()));
        compound.setInteger("type", getType().ordinal());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        dataManager.set(TILE_POS, NBTUtils.readBlockPos(compound.getCompoundTag("tile_pos")));
        dataManager.set(TYPE, compound.getInteger("type"));
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        if(getTile() != null && getTile().min != null && getTile().max != null && getTile().drillPos != null) {
            switch(getType()) {
                case X:
                    return BoundingBoxUtil.makeFrom(new Vec3d(getTile().drillPos.xCoord + 0.5, getTile().min.getY() + 0.5, getTile().min.getZ() + 1), new Vec3d(getTile().drillPos.xCoord + 0.5, getTile().min.getY() + 0.5, getTile().max.getZ()), 4 / 16D);
                case Y:
                    return BoundingBoxUtil.makeFrom(new Vec3d(getTile().drillPos.xCoord + 0.5, getTile().min.getY() + 0.5, getTile().drillPos.zCoord + 0.5), new Vec3d(getTile().drillPos.xCoord + 0.5, getTile().drillPos.yCoord + 1 + 0.5, getTile().drillPos.zCoord + 0.5), 4 / 16D);
                case Z:
                    return BoundingBoxUtil.makeFrom(new Vec3d(getTile().min.getX() + 1, getTile().min.getY() + 0.5, getTile().drillPos.zCoord + 0.5), new Vec3d(getTile().max.getX(), getTile().min.getY() + 0.5, getTile().drillPos.zCoord + 0.5), 4 / 16D);
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

    public enum Type {
        X,
        Y,
        Z
    }
}
