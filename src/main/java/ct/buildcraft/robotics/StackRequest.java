package ct.buildcraft.robotics;

import javax.annotation.Nullable;

import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.IRequestProvider;
import ct.buildcraft.api.robots.IRobotRegistry;
import ct.buildcraft.api.robots.ResourceId;
import ct.buildcraft.api.robots.ResourceIdRequest;
import ct.buildcraft.api.robots.RobotManager;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Classic BuildCraft robotics item request descriptor used by Delivery robots. */
public class StackRequest {
    @Nullable
    private IRequestProvider requester;
    private final int slot;
    private final ItemStack stack;

    @Nullable
    private DockingStation station;
    @Nullable
    private BlockIndex stationIndex;
    @Nullable
    private Direction stationSide;

    public StackRequest(IRequestProvider requester, int slot, ItemStack stack) {
        this.requester = requester;
        this.slot = slot;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
    }

    private StackRequest(int slot, ItemStack stack, BlockIndex stationIndex, @Nullable Direction stationSide) {
        this.slot = slot;
        this.stack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.stationIndex = stationIndex;
        this.stationSide = stationSide;
    }

    @Nullable
    public IRequestProvider getRequester(Level level) {
        if (requester == null) {
            DockingStation dockingStation = getStation(level);
            if (dockingStation != null) {
                requester = dockingStation.getRequestProvider();
            }
        }
        return requester;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Nullable
    public DockingStation getStation(Level level) {
        if (station == null && stationIndex != null && RobotManager.registryProvider != null && level != null) {
            IRobotRegistry registry = RobotManager.registryProvider.getRegistry(level);
            station = registry.getStation(stationIndex.toBlockPos(), stationSide);
        }
        return station;
    }

    public void setStation(DockingStation station) {
        this.station = station;
        this.stationIndex = station.index();
        this.stationSide = station.side();
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.putInt("slot", slot);

        CompoundTag stackTag = new CompoundTag();
        stack.save(stackTag);
        nbt.put("stack", stackTag);

        if (station != null) {
            CompoundTag stationIndexTag = new CompoundTag();
            station.index().writeTo(stationIndexTag);
            nbt.put("stationIndex", stationIndexTag);
            nbt.putByte("stationSide", (byte) (station.side() == null ? -1 : station.side().ordinal()));
        } else if (stationIndex != null) {
            CompoundTag stationIndexTag = new CompoundTag();
            stationIndex.writeTo(stationIndexTag);
            nbt.put("stationIndex", stationIndexTag);
            nbt.putByte("stationSide", (byte) (stationSide == null ? -1 : stationSide.ordinal()));
        }
    }

    @Nullable
    public static StackRequest loadFromNBT(CompoundTag nbt) {
        if (!nbt.contains("stationIndex")) {
            return null;
        }

        int slot = nbt.getInt("slot");
        ItemStack stack = ItemStack.of(nbt.getCompound("stack"));
        BlockIndex stationIndex = new BlockIndex(nbt.getCompound("stationIndex"));
        int sideId = nbt.getByte("stationSide");
        Direction stationSide = sideId >= 0 && sideId < Direction.values().length ? Direction.values()[sideId] : null;
        return new StackRequest(slot, stack, stationIndex, stationSide);
    }

    @Nullable
    public ResourceId getResourceId(Level level) {
        DockingStation requestStation = getStation(level);
        return requestStation != null ? new ResourceIdRequest(requestStation, slot) : null;
    }
}
