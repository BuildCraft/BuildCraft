package buildcraft.builders.entity;

import buildcraft.builders.tile.TileQuarry;
import buildcraft.lib.misc.BoundingBoxUtil;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;

public class EntityQuarryFrame extends Entity {
    public static final int INIT_TIMEOUT = 20;
    public static final DataParameter<BlockPos> QUARRY_POS = EntityDataManager.createKey(EntityQuarryFrame.class, DataSerializers.BLOCK_POS);
    public static final DataParameter<BlockPos> FRAME_POS = EntityDataManager.createKey(EntityQuarryFrame.class, DataSerializers.BLOCK_POS);
    public static final DataParameter<Integer> AXIS = EntityDataManager.createKey(EntityQuarryFrame.class, DataSerializers.VARINT);
    public static final DataParameter<Integer> TIMEOUT = EntityDataManager.createKey(EntityQuarryFrame.class, DataSerializers.VARINT);

    public EntityQuarryFrame(World world) {
        super(world);
        dataManager.register(QUARRY_POS, BlockPos.ORIGIN);
        dataManager.register(FRAME_POS, BlockPos.ORIGIN);
        dataManager.register(AXIS, 0);
        dataManager.register(TIMEOUT, INIT_TIMEOUT);
    }

    public EntityQuarryFrame(World world, BlockPos quarryPos, BlockPos framePos, EnumFacing.Axis axis) {
        super(world);
        dataManager.register(QUARRY_POS, quarryPos);
        dataManager.register(FRAME_POS, framePos);
        setPosition(framePos.getX(), framePos.getY(), framePos.getZ());
        prevPosX = framePos.getX();
        prevPosY = framePos.getY();
        prevPosZ = framePos.getZ();
        dataManager.register(AXIS, axis.ordinal());
        dataManager.register(TIMEOUT, INIT_TIMEOUT);
    }

    @Override
    protected void entityInit() {}

    public TileQuarry getTile() {
        TileEntity tile = world.getTileEntity(dataManager.get(QUARRY_POS));
        return tile instanceof TileQuarry ? (TileQuarry) tile : null;
    }

    @Override
    public void onEntityUpdate() {
        super.onEntityUpdate();
        if (!world.isRemote) {
            dataManager.set(TIMEOUT, dataManager.get(TIMEOUT) - 1);
            if (dataManager.get(TIMEOUT) <= 0) {
                setDead();
            }
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
        nbt.setTag("quarryPos", NBTUtilBC.writeBlockPos(dataManager.get(QUARRY_POS)));
        nbt.setTag("framePos", NBTUtilBC.writeBlockPos(dataManager.get(FRAME_POS)));
        nbt.setInteger("timeout", dataManager.get(TIMEOUT));
        nbt.setInteger("axis", dataManager.get(AXIS));
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
        dataManager.set(QUARRY_POS, NBTUtilBC.readBlockPos(nbt.getTag("quarryPos")));
        dataManager.set(FRAME_POS, NBTUtilBC.readBlockPos(nbt.getTag("framePos")));
        dataManager.set(TIMEOUT, nbt.getInteger("timeout"));
        dataManager.set(AXIS, nbt.getInteger("axis"));
    }

    @Override
    public AxisAlignedBB getEntityBoundingBox() {
        TileQuarry tile = getTile();
        if (tile != null && tile.frameBox.isInitialized() && tile.drillPos != null) {
            BlockPos min = tile.frameBox.min();
            BlockPos max = tile.frameBox.max();
            AxisAlignedBB boundingBox = null;
            switch (EnumFacing.Axis.values()[dataManager.get(AXIS)]) {
                case X:
                    boundingBox = BoundingBoxUtil.makeFrom(
                            new Vec3d(tile.drillPos.xCoord + 0.5, max.getY() + 0.5, min.getZ() + 1),
                            new Vec3d(tile.drillPos.xCoord + 0.5, max.getY() + 0.5, max.getZ()),
                            4 / 16D
                    );
                    break;
                case Y:
                    boundingBox = BoundingBoxUtil.makeFrom(
                            new Vec3d(tile.drillPos.xCoord + 0.5, max.getY() + 0.5, tile.drillPos.zCoord + 0.5),
                            new Vec3d(tile.drillPos.xCoord + 0.5, tile.drillPos.yCoord + 1 + 0.5, tile.drillPos.zCoord + 0.5),
                            4 / 16D
                    );
                    break;
                case Z:
                    boundingBox = BoundingBoxUtil.makeFrom(
                            new Vec3d(min.getX() + 1, max.getY() + 0.5, tile.drillPos.zCoord + 0.5),
                            new Vec3d(max.getX(), max.getY() + 0.5, tile.drillPos.zCoord + 0.5),
                            4 / 16D
                    );
                    break;
            }
            return boundingBox.intersect(new AxisAlignedBB(dataManager.get(FRAME_POS)));
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
