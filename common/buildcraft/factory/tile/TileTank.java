package buildcraft.factory.tile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.core.BCCoreConfig;
import buildcraft.lib.fluid.Tank;
import buildcraft.lib.misc.CapUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.tile.TileBC_Neptune;

public class TileTank extends TileBC_Neptune implements ITickable, IDebuggable, IFluidHandlerAdv {
    public static final int NET_FLUID_DELTA = 10;

    public Tank tank = new Tank("tank", 16000, this);

    private int lastSentAmount = -1;
    private boolean lastSentFluid = false;
    private final SafeTimeTracker tracker = new SafeTimeTracker(BCCoreConfig.networkUpdateRate, 4);

    // client side

    private int target;
    private int amount, amountLast;
    private long lastMessage, lastMessageMinus1;

    public TileTank() {
        caps.addCapability(CapUtil.CAP_FLUIDS, this, EnumPipePart.VALUES);
    }

    // ITickable

    @Override
    public void update() {
        if (world.isRemote) {
            amountLast = amount;
            if (amount != target) {
                int delta = target - amount;
                long msgDelta = lastMessage - lastMessageMinus1;
                msgDelta = MathUtil.clamp((int) msgDelta, 1, 60);
                if (Math.abs(delta) < msgDelta) {
                    amount += delta;
                } else {
                    amount += delta / (int) msgDelta;
                }
            }
            return;
        }

        if (lastSentFluid != (tank.getFluid() != null)) {
            if (tracker.markTimeIfDelay(world)) {
                lastSentFluid = tank.getFluid() != null;
                lastSentAmount = tank.getFluidAmount();
                sendNetworkUpdate(NET_RENDER_DATA);
            }
        } else if (lastSentAmount != tank.getFluidAmount()) {
            if (tracker.markTimeIfDelay(world)) {
                lastSentAmount = tank.getFluidAmount();
                sendNetworkUpdate(NET_FLUID_DELTA);
            }
        }
    }

    @Override
    public void onPlacedBy(EntityLivingBase placer, ItemStack stack) {
        super.onPlacedBy(placer, stack);
        if (!placer.world.isRemote) {
            BlockPos p = pos.up();
            TileTank moveTo = this;
            while (true) {
                TileEntity tileUp = world.getTileEntity(p);
                if (tileUp instanceof TileTank) {
                    TileTank tankUp = (TileTank) tileUp;

                    int used = moveTo.tank.fill(tankUp.tank.getFluid(), true);
                    if (used > 0) {
                        tankUp.drain(used, true);
                    }

                    moveTo = tankUp;
                    p = p.up();
                } else {
                    break;
                }
            }
        }
    }

    // NBT

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
                tank.writeToBuffer(buffer);
            } else if (id == NET_FLUID_DELTA) {
                buffer.writeInt(tank.getFluidAmount());
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, Side side, MessageContext ctx) throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == Side.CLIENT) {
            if (id == NET_RENDER_DATA) {
                tank.readFromBuffer(buffer);
                target = tank.getClientAmount();
                lastMessageMinus1 = lastMessage = world.getTotalWorldTime();
            } else if (id == NET_FLUID_DELTA) {
                target = buffer.readInt();
                lastMessageMinus1 = lastMessage;
                lastMessage = world.getTotalWorldTime();
            }
        }
    }

    // IDebuggable

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("fluid = " + tank.getDebugString());
        if (world.isRemote) {
            left.add("shown = " + amount + ", target = " + target);
            left.add("lastMsg = " + lastMessage + ", lastMsg-1 = " + lastMessageMinus1 + ", diff = " + (lastMessage - lastMessageMinus1));
        } else {
            left.add("current = " + tank.getFluidAmount() + " of " + ((tank.getFluid() != null) ? "Something" : "Nothing"));
            left.add("lastSent = " + lastSentAmount + " of " + (lastSentFluid ? "Something" : "Nothing"));
        }
    }

    // Rendering

    @SideOnly(Side.CLIENT)
    public double getFluidAmountForRender(float partialTicks) {
        float amount = amountLast * (1 - partialTicks) + this.amount * partialTicks;
        Tank other = getTank(pos.up());
        if (other != null && !other.isEmpty()) {
            amount = tank.getCapacity();
        }
        return amount;
    }

    // Tank helper methods

    private Tank getTank(BlockPos at) {
        TileEntity tile = world.getTileEntity(at);
        if (tile instanceof TileTank) {
            TileTank tileTank = (TileTank) tile;
            return tileTank.tank;
        }
        return null;
    }

    private List<Tank> getTanks() {
        List<Tank> tanks = new ArrayList<>();
        BlockPos currentPos = pos;
        while (true) {
            Tank tankUp = getTank(currentPos);
            if (tankUp != null) {
                tanks.add(tankUp);
            } else {
                break;
            }
            currentPos = currentPos.up();
        }
        currentPos = pos.down();
        while (true) {
            Tank tankBelow = getTank(currentPos);
            if (tankBelow != null) {
                tanks.add(0, tankBelow);
            } else {
                break;
            }
            currentPos = currentPos.down();
        }
        return tanks;
    }

    // IFluidHandler

    @Override
    public IFluidTankProperties[] getTankProperties() {
        List<Tank> tanks = getTanks();
        Tank bottom = tanks.get(0);
        FluidStack total = bottom.getFluid();
        int capacity = 0;
        if (total == null) {
            for (Tank t : tanks) {
                capacity += t.getCapacity();
            }
        } else {
            total = total.copy();
            total.amount = 0;
            for (Tank t : tanks) {
                FluidStack other = t.getFluid();
                if (other != null) {
                    total.amount += other.amount;
                }
                capacity += t.getCapacity();
            }
        }
        return new IFluidTankProperties[] { new FluidTankProperties(total, capacity) };
    }

    @Override
    public int fill(FluidStack resource, boolean doFill) {
        if (resource == null || resource.amount <= 0) {
            return 0;
        }
        int filled = 0;
        List<Tank> tanks = getTanks();
        for (Tank t : tanks) {
            FluidStack current = t.getFluid();
            if (current != null && !current.isFluidEqual(resource)) {
                return 0;
            }
        }
        for (Tank t : tanks) {
            int tankFilled = t.fill(resource, doFill);
            if (tankFilled > 0) {
                resource.amount -= tankFilled;
                filled += tankFilled;
                if (resource.amount == 0) {
                    break;
                }
            }
        }
        return filled;
    }

    @Override
    public FluidStack drain(int maxDrain, boolean doDrain) {
        return drain((fluid) -> true, maxDrain, doDrain);
    }

    @Override
    public FluidStack drain(FluidStack resource, boolean doDrain) {
        if (resource == null) {
            return null;
        }
        return drain(resource::isFluidEqual, resource.amount, doDrain);
    }

    // IFluidHandlerAdv

    @Override
    public FluidStack drain(IFluidFilter filter, int maxDrain, boolean doDrain) {
        if (maxDrain <= 0) {
            return null;
        }
        List<Tank> tanks = getTanks();
        // The returned list is ordered bottom -> top, but we want top -> bottom
        Collections.reverse(tanks);
        FluidStack total = null;
        for (Tank t : tanks) {
            int realMax = maxDrain - (total == null ? 0 : total.amount);
            if (realMax <= 0) {
                break;
            }
            FluidStack drained = t.drain(filter, realMax, doDrain);
            if (drained == null) continue;
            if (total == null) {
                total = drained.copy();
                total.amount = 0;
            }
            total.amount += drained.amount;
        }
        return total;
    }
}
