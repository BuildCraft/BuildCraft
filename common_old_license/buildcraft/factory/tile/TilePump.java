package buildcraft.factory.tile;

import buildcraft.api.mj.IMjReceiver;
import buildcraft.lib.fluids.SingleUseTank;
import buildcraft.lib.fluids.TankUtils;
import buildcraft.lib.misc.BlockUtil;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.mj.MjRedstoneBatteryReceiver;
import buildcraft.lib.net.PacketBufferBC;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.io.IOException;
import java.util.*;

public class TilePump extends TileMiner {
    private SingleUseTank tank = new SingleUseTank("tank", 16 * Fluid.BUCKET_VOLUME, this);
    public boolean queueBuilt = false;
    public Queue<BlockPos> queue = new PriorityQueue<>(
            Comparator.comparing(
                    blockPos -> 100_000 - (Math.pow(blockPos.getX() - pos.getX(), 2) + Math.pow(blockPos.getZ() - pos.getZ(), 2)) +
                            Math.abs(blockPos.getY() - pos.getY()) * 100_000
            )
    );

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjRedstoneBatteryReceiver(battery);
    }

    public void buildQueue() {
        queue.clear();
        List<BlockPos> nextPosesToCheck = new ArrayList<>();
        int y = pos.getY() - 1;
        while (true) {
            if (nextPosesToCheck.isEmpty()) {
                for (; y >= 0; y--) {
                    BlockPos posToCheck = new BlockPos(pos.getX(), y, pos.getZ());
                    if (BlockUtil.getFluid(world, posToCheck) != null) {
                        if (!queue.contains(posToCheck)) {
                            nextPosesToCheck.add(posToCheck);
                        }
                        break;
                    } else if(!world.isAirBlock(posToCheck)) {
                        break;
                    }
                }
                if (nextPosesToCheck.isEmpty()) {
                    break;
                }
            }
            List<BlockPos> nextPosesToCheckCopy = new ArrayList<>(nextPosesToCheck);
            nextPosesToCheck.clear();
            for (BlockPos posToCheck : nextPosesToCheckCopy) {
                if (!queue.contains(posToCheck)) {
                    queue.add(posToCheck);
                }
                for (EnumFacing side : EnumFacing.values()) {
                    BlockPos offsetPos = posToCheck.offset(side);
                    if(Math.pow(offsetPos.getX() - pos.getX(), 2) + Math.pow(offsetPos.getZ() - pos.getZ(), 2) > Math.pow(64, 2)) {
                        continue;
                    }
                    if (BlockUtil.getFluid(world, posToCheck) != null && BlockUtil.getFluid(world, offsetPos) == BlockUtil.getFluid(world, posToCheck) && !queue.contains(offsetPos) && !nextPosesToCheck.contains(offsetPos) && !nextPosesToCheckCopy.contains(offsetPos)) {
                        nextPosesToCheck.add(offsetPos);
                    }
                }
            }
        }
    }

    public boolean canDrain(BlockPos blockPos) {
        Fluid fluid = BlockUtil.getFluid(world, blockPos);
        return tank.isEmpty() ? fluid != null : fluid == tank.getAcceptedFluid();
    }

    public void nextPos() {
        while (!queue.isEmpty()) {
            currentPos = queue.poll();
            if (canDrain(currentPos)) {
                return;
            }
        }
        currentPos = null;
    }

    public void updateYLevel() {
        if (currentPos != null) {
            goToYLevel(Math.min(currentPos.getY(), pos.getY()));
        } else {
            goToYLevel(pos.getY());
        }
    }

    @Override
    protected void initCurrentPos() {
        if (currentPos == null) {
            nextPos();
            updateYLevel();
        }
    }

    @Override
    public void update() {
        if (!queueBuilt && !world.isRemote) {
            buildQueue();
            queueBuilt = true;
        }

        super.update();

        TankUtils.pushFluidAround(world, pos);
    }

    @Override
    public void mine() {
        if (tank.isFull()) {
            return;
        }

        long target = 10000000;

        if (currentPos != null) {
            progress += battery.extractPower(0, target - progress);
            if (progress >= target) {
                FluidStack drain = BlockUtil.drainBlock(world, currentPos, false);
                if (drain != null && canDrain(currentPos)) {
                    world.setBlockToAir(currentPos);
                    tank.fill(drain, true);
                    nextPos();
                    updateYLevel();
                    progress = 0;
                } else {
                    buildQueue();
                    nextPos();
                    updateYLevel();
                }
            }
        } else {
            buildQueue();
            nextPos();
            updateYLevel();
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        tank.deserializeNBT(nbt.getCompoundTag("tank"));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setTag("tank", tank.serializeNBT());
        return nbt;
    }

    // Networking

    @Override
    public void writePayload(int id, PacketBufferBC buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_RENDER_DATA) {
                writePayload(NET_LED_STATUS, buffer, side);
            } else if (id == NET_LED_STATUS) {
                tank.writeToBuffer(buffer);
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                readPayload(NET_LED_STATUS, buffer, side, ctx);
            } else if (id == NET_LED_STATUS) {
                tank.readFromBuffer(buffer);
            }
        }
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        super.getDebugInfo(left, right, side);
        left.add("fluid = " + tank.getDebugString());
        left.add("queue size = " + queue.size());
    }

    @SideOnly(Side.CLIENT)
    public float getFluidPercentFilledForRender() {
        float val = tank.getFluidAmount() / (float) tank.getCapacity();
        return val < 0 ? 0 : val > 1 ? 1 : val;
    }

    // Capabilities

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_FLUIDS) {
            return (T) tank;
        }
        return super.getCapability(capability, facing);
    }
}
