package buildcraft.transport.pipes.bc8;

import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Vec3;

import buildcraft.api.core.INBTLoadable_BC8;
import buildcraft.api.core.INetworkLoadable_BC8;
import buildcraft.api.transport.pipe_bc8.BCPipeEventHandler;
import buildcraft.api.transport.pipe_bc8.EnumPipeDirection;
import buildcraft.api.transport.pipe_bc8.IPipeContents.IPipeContentsItem;
import buildcraft.api.transport.pipe_bc8.IPipeContentsEditable.IPipeContentsEditableItem;
import buildcraft.api.transport.pipe_bc8.IPipePropertyProvider.IPipeProperty;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeAPI_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEventContents_BC8.Enter;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEvent_BC8;
import buildcraft.api.transport.pipe_bc8.event_bc8.IPipeEvent_BC8.PropertyQuery;
import buildcraft.core.lib.inventory.StackHelper;
import buildcraft.transport.PipeTransportItems;

import io.netty.buffer.ByteBuf;

public class TravellingItem_BC8 implements INetworkLoadable_BC8<TravellingItem_BC8>, INBTLoadable_BC8<TravellingItem_BC8> {
    private final IPipeContentsEditableItem item;
    /** Indicates the in-world tick of when it will reach its destination (Generally the other side of the pipe) */
    private long tickStarted, tickFinished;

    public TravellingItem_BC8(IPipeContentsEditableItem item, long now, long reachDest) {
        this.item = item;
        this.tickStarted = now;
        this.tickFinished = reachDest;
    }

    public TravellingItem_BC8(TravellingItem_BC8 item, long now, long finished) {
        this.item = item.item;
        this.tickStarted = now;
        this.tickFinished = finished;
    }

    public double getWayThrough(long now) {
        long diff = tickFinished - tickStarted;
        long nowDiff = now - tickStarted;
        return nowDiff / (double) diff;
    }

    public Vec3 interpolatePosition(Vec3 start, Vec3 end, long tick, float partialTicks) {
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
        return new Vec3(x, y, z);
    }

    public boolean canBeGroupedWith(IPipeContentsItem other) {
        if (item.getPart() != other.getPart()) return false;
        if (item.getDirection() != other.getDirection()) return false;
        if (!StackHelper.canStacksMerge(item.cloneItemStack(), other.cloneItemStack())) return false;

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
        int merged = StackHelper.mergeStacks(otherStack, thisStack, true);
        if (merged == otherStack.stackSize) {
            otherStack.stackSize = 0;
            item.setStack(thisStack);
            other.setStack(otherStack);
            return true;
        }
        return false;
    }

    @BCPipeEventHandler
    public void itemInsertion(Enter enter) {
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
            /* If an item has been added to a pipe, try and add it to this item rather than creating a new item */
            tryEncompass(item);
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

        if (tick.getCurrentTick() < tickFinished) return;
        IPipe_BC8 pipe = tick.getPipe();

        // We have reached the end of where we are, try to do something
        if (item.getDirection() == EnumPipeDirection.TO_CENTER) {
            // We need to find out where we are going, and the new speed of ourselves

            pipe.fireEvent(tick);

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
        return new TravellingItem_BC8(item, started, finished);
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
        long started = buf.readLong();
        long finished = buf.readLong();
        return new TravellingItem_BC8(item, started, finished);
    }

    @Override
    public void writeToByteBuf(ByteBuf buf) {
        item.writeToByteBuf(buf);
        buf.writeLong(tickStarted);
        buf.writeLong(tickFinished);
    }
}
