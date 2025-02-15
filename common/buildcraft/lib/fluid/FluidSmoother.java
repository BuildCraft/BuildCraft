package buildcraft.lib.fluid;

import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.core.BCCoreConfig;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.net.IPayloadWriter;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.net.cache.NetworkedFluidStackCache;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;

public class FluidSmoother implements IDebuggable {
    final IFluidDataSender sender;
    final Tank tank;
    _Side data;

    public FluidSmoother(IFluidDataSender sender, Tank tank) {
        this.sender = sender;
        this.tank = tank;
    }

    public void tick(Level world) {
        if (data == null) {
            if (world == null) {
                return;
            }
            data = world.isClientSide ? new _Client() : new _Server();
        }
        data.tick(world);
    }

    public void handleMessage(Level world, PacketBufferBC buffer) {
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
            throw new IllegalStateException("You can only call this on the server!");
        }
    }

    public void resetSmoothing(Level world) {
        if (data == null && world.isClientSide) {
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
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
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
        abstract void tick(Level world);
    }

    final class _Server extends _Side {
        private int sentAmount = -1;
        private boolean sentHasFluid = false;
        private final SafeTimeTracker tracker = new SafeTimeTracker(BCCoreConfig.networkUpdateRate, 4);

        @Override
        void tick(Level world) {
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
//        public void getDebugInfo(List<String> left, List<String> right, Direction side)
        public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//            String contents = (tank.getFluid() != null) ? "Something" : "Nothing";
//            left.add("current = " + tank.getFluidAmount() + " of " + contents);
//            left.add("lastSent = " + sentAmount + " of " + (sentHasFluid ? "Something" : "Nothing"));
            String contents = (tank.getFluid() != null) ? "Something" : "Nothing";
            left.add(Component.literal("current = " + tank.getFluidAmount() + " of " + contents));
            left.add(Component.literal("lastSent = " + sentAmount + " of " + (sentHasFluid ? "Something" : "Nothing")));
        }
    }

    final class _Client extends _Side {
        private int target;
        int amount, amountLast;
        long lastMessage, lastMessageMinus1;
        NetworkedFluidStackCache.Link link;

        @Override
        void tick(Level world) {
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

        void handleMessage(Level world, PacketBufferBC buffer) {
            target = buffer.readInt();
            if (buffer.readBoolean()) {
                link = BuildCraftObjectCaches.CACHE_FLUIDS.client().retrieve(buffer.readInt());
            }
            lastMessageMinus1 = lastMessage;
            lastMessage = world.getGameTime();
        }

        void resetSmoothing(Level world) {
            lastMessageMinus1 = lastMessage = world.getGameTime();
            lastMessageMinus1 -= 1;
        }

        @Override
//        public void getDebugInfo(List<String> left, List<String> right, Direction side)
        public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
//            left.add("shown = " + amount + ", target = " + target);
//            left.add("lastMsg = " + lastMessage + ", lastMsg-1 = " + lastMessageMinus1 + ", diff = " + (lastMessage - lastMessageMinus1));
            left.add(Component.literal("shown = " + amount + ", target = " + target));
            left.add(Component.literal("lastMsg = " + lastMessage + ", lastMsg-1 = " + lastMessageMinus1 + ", diff = " + (lastMessage - lastMessageMinus1)));
        }
    }
}
