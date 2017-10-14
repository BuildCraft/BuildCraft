package buildcraft.lib.fluid;

import java.util.List;

import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.tiles.IDebuggable;

import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.net.cache.NetworkedFluidStackCache;

import buildcraft.core.BCCoreConfig;

public class FluidSmoother implements IDebuggable {
    final IFluidDataSender sender;
    final Tank tank;
    _Side data;

    public FluidSmoother(IFluidDataSender sender, Tank tank) {
        this.sender = sender;
        this.tank = tank;
    }

    public void tick(World world) {
        if (data == null) {
            if (world == null) {
                return;
            }
            data = world.isRemote ? new _Client() : new _Server();
        }
        data.tick(world);
    }

    public void handleMessage(World world, PacketBufferBC buffer) {
        if (data == null) {
            data = new _Client();
        }
        if (data instanceof _Client) {
            ((_Client) data).handleMessage(world, buffer);
        } else {
            throw new IllegalStateException("You can only call this on the client!");
        }
    }

    public void writeInit(PacketBufferBC buffer) {
        if (data == null) {
            data = new _Server();
        }
        if (data instanceof _Server) {
            ((_Server) data).writeMessage(buffer);
        } else {
            throw new IllegalStateException("You can only call this on the client!");
        }
    }

    public void resetSmoothing(World world) {
        if (data == null && world.isRemote) {
            data = new _Client();
        }
        if (data instanceof _Client) {
            _Client client = (_Client) data;
            client.resetSmoothing(world);
        } else {
            throw new IllegalStateException("You can only call this on the client!");
        }
    }

    public FluidStack getFluidForRender() {
        if (data instanceof _Client) {
            _Client client = (_Client) data;
            if (client.link == null) {
                return null;
            }
            FluidStack fluid = client.link.get();
            if (fluid == null) {
                return null;
            }
            return new FluidStack(fluid, client.amount);
        }
        return null;
    }

    public FluidStackInterp getFluidForRender(double partialTicks) {
        if (data instanceof _Client) {
            _Client client = (_Client) data;
            if (client.link == null) {
                return null;
            }
            FluidStack fluid = client.link.get();
            if (fluid == null) {
                return null;
            }
            double amount = client.amountLast * (1 - partialTicks) + client.amount * partialTicks;
            return new FluidStackInterp(fluid, amount);
        }
        return null;
    }

    /** Delegate for {@link Tank#getCapacity()} - useful if this is the only object exposed for rendering. */
    public int getCapacity() {
        return tank.getCapacity();
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        if (data != null) {
            data.getDebugInfo(left, right, side);
        }
    }

    @FunctionalInterface
    public interface IFluidDataSender {
        void writePacket(IPayloadWriter writer);
    }

    public static class FluidStackInterp {
        public final FluidStack fluid;
        public final double amount;

        public FluidStackInterp(FluidStack fluid, double amount) {
            this.fluid = fluid;
            this.amount = amount;
        }
    }

    abstract class _Side implements IDebuggable {
        abstract void tick(World world);
    }

    final class _Server extends _Side {
        private int sentAmount = -1;
        private boolean sentHasFluid = false;
        private final SafeTimeTracker tracker = new SafeTimeTracker(BCCoreConfig.networkUpdateRate, 4);

        @Override
        void tick(World world) {
            FluidStack fluid = tank.getFluid();
            boolean hasFluid = fluid != null;
            if ((tank.getFluidAmount() != sentAmount || hasFluid != sentHasFluid)) {
                if (tracker.markTimeIfDelay(world)) {
                    sender.writePacket(this::writeMessage);
                }
            }
        }

        void writeMessage(PacketBufferBC buffer) {
            FluidStack fluid = tank.getFluid();
            boolean hasFluid = fluid != null;

            sentAmount = tank.getFluidAmount();
            sentHasFluid = hasFluid;

            final int amount = sentAmount;
            final int flId = hasFluid ? BuildCraftObjectCaches.CACHE_FLUIDS.server().store(fluid) : -1;

            buffer.writeInt(amount);
            if (hasFluid) {
                buffer.writeBoolean(true);
                buffer.writeInt(flId);
            } else {
                buffer.writeBoolean(false);
            }
        }

        @Override
        public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
            String contents = (tank.getFluid() != null) ? "Something" : "Nothing";
            left.add("current = " + tank.getFluidAmount() + " of " + contents);
            left.add("lastSent = " + sentAmount + " of " + (sentHasFluid ? "Something" : "Nothing"));
        }
    }

    final class _Client extends _Side {
        private int target;
        int amount, amountLast;
        long lastMessage, lastMessageMinus1;
        NetworkedFluidStackCache.Link link;

        @Override
        void tick(World world) {
            amountLast = amount;
            if (amount != target) {
                int delta = target - amount;
                long msgDelta = lastMessage - lastMessageMinus1;
                msgDelta = MathUtil.clamp((int) msgDelta, 1, 10);
                if (Math.abs(delta) < msgDelta) {
                    amount += delta;
                } else {
                    amount += delta / (int) msgDelta;
                }
            }
        }

        void handleMessage(World world, PacketBufferBC buffer) {
            target = buffer.readInt();
            if (buffer.readBoolean()) {
                link = BuildCraftObjectCaches.CACHE_FLUIDS.client().retrieve(buffer.readInt());
            }
            lastMessageMinus1 = lastMessage;
            lastMessage = world.getTotalWorldTime();
        }

        void resetSmoothing(World world) {
            lastMessageMinus1 = lastMessage = world.getTotalWorldTime();
            lastMessageMinus1 -= 1;
        }

        @Override
        public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
            left.add("shown = " + amount + ", target = " + target);
            left.add("lastMsg = " + lastMessage + ", lastMsg-1 = " + lastMessageMinus1 + ", diff = "
                + (lastMessage - lastMessageMinus1));
        }
    }
}
