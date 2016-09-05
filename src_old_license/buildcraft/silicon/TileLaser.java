/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.silicon;

import java.util.LinkedList;
import java.util.List;
import io.netty.buffer.ByteBuf;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.power.ILaserTarget;
import buildcraft.api.power.ILaserTargetBlock;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.EntityLaser;
import buildcraft.core.LaserData;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.data.Box;

public class TileLaser extends TileBuildCraft implements IHasWork, IControllable {

    private static final float LASER_OFFSET = 2.0F / 16.0F;
    private static final short POWER_AVERAGING = 100;

    public LaserData laser = new LaserData();

    private final SafeTimeTracker laserTickTracker = new SafeTimeTracker(10);
    private final SafeTimeTracker searchTracker = new SafeTimeTracker(100, 100);
    private final SafeTimeTracker networkTracker = new SafeTimeTracker(20, 3);
    private ILaserTarget laserTarget;
    private int powerIndex = 0;

    private short powerAverage = 0;
    private final short[] power = new short[POWER_AVERAGING];

    public TileLaser() {
        super();
        this.setBattery(new RFBattery(10000, 250, 0));
    }

    @Override
    public void initialize() {
        super.initialize();

        if (laser == null) {
            laser = new LaserData();
        }

        laser.isVisible = false;
        laser.head = Utils.convert(getPos());
        laser.tail = Utils.convert(getPos());
        laser.isGlowing = true;
    }

    @Override
    public void update() {
        super.update();

        laser.iterateTexture();

        if (worldObj.isRemote) {
            return;
        }

        // If a gate disabled us, remove laser and do nothing.
        if (mode == IControllable.Mode.Off) {
            removeLaser();
            return;
        }

        // Check for any available tables at a regular basis
        if (canFindTable()) {
            findTable();
        }

        // If we still don't have a valid table or the existing has
        // become invalid, we disable the laser and do nothing.
        if (!isValidTable()) {
            removeLaser();
            return;
        }

        // Disable the laser and do nothing if no energy is available.
        if (getBattery().getEnergyStored() == 0) {
            removeLaser();
            return;
        }

        // We have a laser
        if (laser != null) {
            // We have a table and can work, so we create a laser if
            // necessary.
            laser.isVisible = true;

            // We may update laser
            if (canUpdateLaser()) {
                updateLaser();
            }
        }

        // Consume power and transfer it to the table.
        int localPower = getBattery().useEnergy(0, getMaxPowerSent(), false);
        laserTarget.receiveLaserEnergy(localPower);

        if (laser != null) {
            pushPower(localPower);
        }

        onPowerSent(localPower);

        sendNetworkUpdate();
    }

    protected int getMaxPowerSent() {
        return 40;
    }

    protected void onPowerSent(int power) {}

    protected boolean canFindTable() {
        return searchTracker.markTimeIfDelay(worldObj);
    }

    protected boolean canUpdateLaser() {
        return laserTickTracker.markTimeIfDelay(worldObj);
    }

    protected boolean isValidTable() {
        if (laserTarget == null || laserTarget.isInvalidTarget() || !laserTarget.requiresLaserEnergy()) {
            return false;
        }

        return true;
    }

    protected void findTable() {
        int meta = getBlockMetadata();

        BlockPos min = getPos().add(Utils.vec3i(-5));
        BlockPos max = getPos().add(Utils.vec3i(5));

        EnumFacing face = EnumFacing.getFront(meta);
        if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
            max = max.offset(face, 5);
        } else {
            min = min.offset(face, -5);
        }

        List<ILaserTarget> targets = new LinkedList<>();
        for (BlockPos pos : BlockPos.getAllInBox(min, max)) {
            if (BlockUtils.getBlockState(worldObj, pos).getBlock() instanceof ILaserTargetBlock) {
                TileEntity tile = worldObj.getTileEntity(pos);
                if (tile instanceof ILaserTarget) {
                    ILaserTarget table = (ILaserTarget) tile;

                    if (table.requiresLaserEnergy()) {
                        targets.add(table);
                    }
                }
            }
        }

        if (targets.isEmpty()) {
            return;
        }

        laserTarget = targets.get(worldObj.rand.nextInt(targets.size()));
    }

    protected void updateLaser() {

        int meta = getBlockMetadata();
        double px = 0, py = 0, pz = 0;

        switch (EnumFacing.getFront(meta)) {

            case WEST:
                px = -LASER_OFFSET;
                break;
            case EAST:
                px = LASER_OFFSET;
                break;
            case DOWN:
                py = -LASER_OFFSET;
                break;
            case UP:
                py = LASER_OFFSET;
                break;
            case NORTH:
                pz = -LASER_OFFSET;
                break;
            case SOUTH:
            default:
                pz = LASER_OFFSET;
                break;
        }

        Vec3d head = Utils.convertMiddle(getPos()).addVector(px, py, pz);
        Vec3d tail = Utils.convert(((TileEntity) laserTarget).getPos());
        tail = tail.addVector(0.475 + (worldObj.rand.nextDouble() - 0.5) / 5d, 9 / 16d, 0.475 + (worldObj.rand.nextDouble() - 0.5) / 5d);

        laser.head = head;
        laser.tail = tail;

        if (!laser.isVisible) {
            laser.isVisible = true;
        }
    }

    protected void removeLaser() {
        if (powerAverage > 0) {
            pushPower(0);
        }
        if (laser.isVisible) {
            laser.isVisible = false;
            // force sending the network update even if the network tracker
            // refuses.
            super.sendNetworkUpdate();
        }
    }

    @Override
    public void sendNetworkUpdate() {
        if (networkTracker.markTimeIfDelay(worldObj)) {
            super.sendNetworkUpdate();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbttagcompound) {
        super.readFromNBT(nbttagcompound);
    }

    @Override
    public void writeToNBT(NBTTagCompound nbttagcompound) {
        super.writeToNBT(nbttagcompound);
    }

    @Override
    public void readData(ByteBuf stream) {
        laser = new LaserData();
        laser.readData(stream);
        powerAverage = stream.readShort();
    }

    @Override
    public void writeData(ByteBuf stream) {
        laser.writeData(stream);
        stream.writeShort(powerAverage);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        removeLaser();
    }

    @Override
    public boolean hasWork() {
        return isValidTable();
    }

    private void pushPower(int received) {
        powerAverage -= power[powerIndex];
        powerAverage += received;
        power[powerIndex] = (short) received;
        powerIndex++;

        if (powerIndex == power.length) {
            powerIndex = 0;
        }
    }

    public ResourceLocation getTexture() {
        double avg = powerAverage / POWER_AVERAGING;

        if (avg <= 10.0) {
            return EntityLaser.LASER_RED;
        } else if (avg <= 20.0) {
            return EntityLaser.LASER_YELLOW;
        } else if (avg <= 30.0) {
            return EntityLaser.LASER_GREEN;
        } else {
            return EntityLaser.LASER_BLUE;
        }
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new Box(this).extendToEncompass(laser.tail).getBoundingBox();
    }

    @Override
    public boolean acceptsControlMode(Mode mode) {
        return mode == IControllable.Mode.On || mode == IControllable.Mode.Off;
    }
}
