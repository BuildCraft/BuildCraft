package buildcraft.builders.entity;

import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.NBTUtilBC;

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
        return Type.VALUES[dataManager.get(TYPE)];
    }

    public TileQuarry getTile() {
        TileEntity tile = world.getTileEntity(getTilePos());
        return tile instanceof TileQuarry ? (TileQuarry) tile : null;
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
        compound.setTag("tile_pos", NBTUtilBC.writeBlockPos(getTilePos()));
        compound.setInteger("type", getType().ordinal());
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound compound) {
        dataManager.set(TILE_POS, NBTUtilBC.readBlockPos(compound.getTag("tile_pos")));
        dataManager.set(TYPE, compound.getInteger("type"));
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        TileQuarry tile = getTile();
        if(tile != null && tile.min != null && tile.max != null && tile.drillPos != null) {
            switch(getType()) {
                case X:
                    return BoundingBoxUtil.makeFrom(new Vec3d(tile.drillPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.min.getZ() + 1), new Vec3d(tile.drillPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.max.getZ()), 4 / 16D);
                case Y:
                    return BoundingBoxUtil.makeFrom(new Vec3d(tile.drillPos.xCoord + 0.5, tile.min.getY() + 0.5, tile.drillPos.zCoord + 0.5), new Vec3d(tile.drillPos.xCoord + 0.5, tile.drillPos.yCoord + 1 + 0.5, tile.drillPos.zCoord + 0.5), 4 / 16D);
                case Z:
                    return BoundingBoxUtil.makeFrom(new Vec3d(tile.min.getX() + 1, tile.min.getY() + 0.5, tile.drillPos.zCoord + 0.5), new Vec3d(tile.max.getX(), tile.min.getY() + 0.5, tile.drillPos.zCoord + 0.5), 4 / 16D);
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
        Z;

        public static final Type[] VALUES = values();
    }
}
