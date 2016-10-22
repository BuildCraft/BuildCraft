package buildcraft.transport.pipes.bc8;

import java.util.Set;
import io.netty.buffer.ByteBuf;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;

import buildcraft.api.transport.pipe_bc8.BCPipeEventHandler;
import buildcraft.api.transport.pipe_bc8.EnumContentsJourneyPart;
import buildcraft.api.transport.pipe_bc8.IConnection_BC8;
import buildcraft.api.transport.pipe_bc8.IPipeContents.IPipeContentsItem;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;
import buildcraft.api.transport.pipe_bc8.IPipeListener;
import buildcraft.api.transport.pipe_bc8.IPipeListenerFactory;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipeProperty;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeAPI_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventContents_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventContents_BC8.Enter;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEvent_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEvent_BC8.PropertyQuery;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.lib.misc.StackUtil;
import buildcraft.transport.PipeTransportItems;

public class TravellingItem_BC8 implements IPipeListener {
    private final IPipeContentsEditableItem item;
    private final IPipe_BC8 pipe;
    /** Indicates the in-world tick of when it will reach its destination (Generally the other side of the pipe) */
    private long tickStarted, tickFinished;

    public TravellingItem_BC8(IPipe_BC8 pipe, IPipeContentsEditableItem item, long now, long reachDest) {
        /* If either of these are null it will cause big problems later on- so don't even allow that to be a
         * possibility. */
        if (pipe == null) throw new NullPointerException("pipe");
        if (item == null) throw new NullPointerException("item");
        this.pipe = pipe;
        this.item = item;
        this.tickStarted = now;
        this.tickFinished = reachDest;
    }

    public double getWayThrough(long now) {
        long diff = tickFinished - tickStarted;
        long nowDiff = now - tickStarted;
        return nowDiff / (double) diff;
    }

    public void genTimings(long now, double distance) {
        tickStarted = now;
        double time = distance / item.getSpeed();
        time = Math.ceil(time);
        tickFinished = now + (long) (time);
    }

    public Vec3d interpolatePosition(Vec3d start, Vec3d end, long tick, float partialTicks) {
        long diff = tickFinished - tickStarted;
        long nowDiff = tick - tickStarted;
        double sinceStart = nowDiff + partialTicks;
        double interpMul = sinceStart / (double) diff;
        double oneMinus = 1 - interpMul;
        if (interpMul <= 0) return start;
        if (interpMul >= 1) return end;

        double x = oneMinus * start.xCoord + interpMul * end.xCoord;
        double y = oneMinus * start.yCoord + interpMul * end.yCoord;
        double z = oneMinus * start.zCoord + interpMul * end.zCoord;
        return new Vec3d(x, y, z);
    }

    public boolean canBeGroupedWith(IPipeContentsItem other) {
        if (item.getDirection() != other.getDirection()) return false;
        if (item.getJourneyPart() != other.getJourneyPart()) return false;
        if (!StackUtil.canMerge(item.cloneItemStack(), other.cloneItemStack())) return false;

        Set<IPipeProperty<?>> thisProperties = item.getProperties().getPropertySet();
        Set<IPipeProperty<?>> otherProperties = other.getProperties().getPropertySet();
        if (thisProperties.size() != otherProperties.size()) return false;

        for (IPipeProperty<?> property : thisProperties) {
            if (!otherProperties.contains(property)) return false;
            Object thisValue = item.getProperties().getValue(property);
            Object otherValue = other.getProperties().getValue(property);
            if (!thisValue.equals(otherValue)) return false;
        }
        return true;
    }

    /** Attempts to add the other item to itself. */
    public boolean tryEncompass(IPipeContentsEditableItem other) {
        if (!canBeGroupedWith(other)) return false;

        ItemStack thisStack = item.cloneItemStack();
        ItemStack otherStack = other.cloneItemStack();
        // We can test directly because both of the stacks are cloned.
        int merged = StackHelper.mergeStacks(otherStack, thisStack, true);
        if (merged == otherStack.stackSize) {
            otherStack.stackSize = 0;
            item.setStack(thisStack);
            other.setStack(otherStack);
            return true;
        }
        return false;
    }

    // Event handlers

    @BCPipeEventHandler
    public void itemInsertion(Enter enter) {
        // Don't bother to handle it if someone else already has.
        /* PipeTransportItem will NOT handle it if the number of stacks is greater than or equal to the max, so it falls
         * down to us to handle it. */
        if (enter.hasBeenHandled()) return;

        // Only handle the insertion if its an item
        if (enter.getContents() instanceof IPipeContentsEditableItem) {
            // Check if the pipe already has a lot of items, if it doesn't, then just ignore it.
            int stacks = enter.getPipe().getProperties().getValue(PipeAPI_BC8.STACK_COUNT);
            if (stacks < PipeTransportItems.MAX_PIPE_STACKS) return;

            IPipeContentsEditableItem item = (IPipeContentsEditableItem) enter.getContents();
            long now = enter.getPipe().getWorld().getTotalWorldTime();
            double dist = getWayThrough(now);
            /* Don't add it to ourself if we are far enough away from the entrance. tryEncompass will check for us to
             * see if we are using going in the same direction. */
            if (dist > 0.25) return;
            /* If an item is about to be added to a pipe, try and add it to this item rather than creating a new item */
            if (tryEncompass(item)) {
                // Let everybody know that we have handled this item, so no-one else needs to
                enter.handle();
            }
        }
    }

    @BCPipeEventHandler
    public void tick(IPipeEvent_BC8.Tick tick) {
        if (item.getProperties().getValue(PipeAPI_BC8.ITEM_PAUSED)) {
            // Just so we render them properly, and so we need to stop this from ticking in the near future
            tickStarted++;
            tickFinished++;
            return;
        }

        if (tick instanceof IPipeEvent_BC8.Tick.Client) return;

        EnumContentsJourneyPart direction = item.getJourneyPart();
        if (direction == EnumContentsJourneyPart.JUST_ENTERED) {
            // Setup ourselves NOW, but tick the rest later
            double normalizedSpeed = item.getSpeed() * PipeTransportItem_BC8.SPEED_NORMALIZER;
            IPipeEventContents_BC8.ChangeSpeed changeSpeed = null
                /* new PipeEventContents.ChangeSpeed(item, normalizedSpeed) */;
            pipe.fireEvent(changeSpeed);

            // normalizedSpeed = changeSpeed.getNormalizedSpeed();
            item.setSpeed(normalizedSpeed / PipeTransportItem_BC8.SPEED_NORMALIZER);

            double distance = 0.25;
            IConnection_BC8 connection = pipe.getConnections().get(item.getDirection().getOpposite());
            if (connection != null) distance += connection.getLength();

            // generate our new timings (when we will next tick)
            genTimings(pipe.getWorld().getTotalWorldTime(), distance);

            // Update the client with our new timings
            pipe.sendClientUpdate(this);
            // Tick next tick not this tick
            return;
        }

        if (tick.getCurrentTick() < tickFinished) return;

        if (direction == EnumContentsJourneyPart.TO_CENTER) {
            // We need to find out where we are going, and the new speed of ourselves

        } else {
            /* We must be going to the end of the pipe, so we need to insert ourselves into the next pipe, or into an
             * inventory (if one exists) */
        }
    }

    @BCPipeEventHandler
    public <T> void queryProperty(IPipeEvent_BC8.PropertyQuery<T> property) {
        // If its the item count then
        if (property.getProperty() == PipeAPI_BC8.ITEM_COUNT) {
            PropertyQuery<Integer> event = (PropertyQuery<Integer>) property;
            int current = event.getValue();
            current += item.cloneItemStack().stackSize;
            event.setValue(current);
        } else if (property.getProperty() == PipeAPI_BC8.STACK_COUNT) {
            PropertyQuery<Integer> event = (PropertyQuery<Integer>) property;
            if (item.cloneItemStack().stackSize > 0) event.setValue(event.getValue() + 1);
        }
    }

    @Override
    public TravellingItem_BC8 readFromNBT(NBTBase nbt) {
        NBTTagCompound tag = (NBTTagCompound) nbt;
        IPipeContentsEditableItem item = this.item.readFromNBT(tag.getCompoundTag("item"));
        long started = tag.getLong("tickStarted");
        long finished = tag.getLong("tickFinished");
        return new TravellingItem_BC8(pipe, item, started, finished);
    }

    @Override
    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setTag("item", item.writeToNBT());
        nbt.setLong("tickStarted", tickStarted);
        nbt.setLong("tickFinished", tickFinished);
        return nbt;
    }

    @Override
    public TravellingItem_BC8 readFromByteBuf(ByteBuf buf) {
        IPipeContentsEditableItem item = this.item.readFromByteBuf(buf);
        long now = pipe.getWorld().getTotalWorldTime();
        long started = now + buf.readLong();
        long finished = now + buf.readLong();
        return new TravellingItem_BC8(pipe, item, started, finished);
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {
        item.writeToByteBuf(buf);
        /* Write the delta of ticks, because this way we can counteract all lag between the server and the client. The
         * client will use its own world tick when calculating timings, so the client will display everything
         * properly. */
        long now = pipe.getWorld().getTotalWorldTime();
        buf.writeShort((short) (tickStarted - now));
        buf.writeShort((short) (tickFinished - now));
    }

    public enum Factory implements IPipeListenerFactory {
        INSTANCE;

        @Override
        public IPipeListener createNewListener(IPipe_BC8 pipe) {
            return new TravellingItem_BC8(pipe, new PipeContentsEditableItem(null, null, null), 0, 0);
        }
    }
}
