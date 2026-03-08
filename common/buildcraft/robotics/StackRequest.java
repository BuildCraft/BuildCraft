/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.robotics;

import buildcraft.api.robots.*;
import buildcraft.lib.misc.NBTUtilBC;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;

public class StackRequest {
    private IRequestProvider requester;

    private int slot;

    @Nonnull
    private ItemStack stack;

    private DockingStation station;
    private BlockPos stationIndex;
    private Direction stationSide;

    public StackRequest(IRequestProvider requester, int slot, @Nonnull ItemStack stack) {
        this.requester = requester;
        this.slot = slot;
        this.stack = stack;
        this.station = null;
    }

    private StackRequest(int slot, @Nonnull ItemStack stack, BlockPos stationIndex, Direction stationSide) {
        requester = null;
        this.slot = slot;
        this.stack = stack;
        station = null;
        this.stationIndex = stationIndex;
        this.stationSide = stationSide;
    }

    public IRequestProvider getRequester(Level world) {
        if (requester == null) {
            DockingStation dockingStation = getStation(world);
            if (dockingStation != null) {
                requester = dockingStation.getRequestProvider();
            }
        }
        return requester;
    }

    public int getSlot() {
        return slot;
    }

    @Nonnull
    public ItemStack getStack() {
        return stack;
    }

    public DockingStation getStation(Level world) {
        if (station == null) {
            IRobotRegistry robotRegistry = RobotManager.registryProvider.getRegistry(world);
            station = robotRegistry.getStation(stationIndex, stationSide);
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

        CompoundTag stackNBT = new CompoundTag();
        stack.save(stackNBT);
        nbt.put("stack", stackNBT);

        if (station != null) {
            nbt.put("stationIndex", NBTUtilBC.writeBlockPos(stationIndex));
            nbt.putByte("stationSide", (byte) station.side().ordinal());
        }
    }

    public static StackRequest loadFromNBT(CompoundTag nbt) {
        if (nbt.contains("stationIndex")) {
            int slot = nbt.getInt("slot");

            ItemStack stack = ItemStack.of(nbt.getCompound("stack"));

            BlockPos stationIndex = NBTUtilBC.readBlockPos(nbt.get("stationIndex"));
            Direction stationSide = Direction.values()[nbt.getByte("stationSide")];

            return new StackRequest(slot, stack, stationIndex, stationSide);
        } else {
            return null;
        }
    }

    public ResourceId getResourceId(Level world) {
        return getStation(world) != null ? new ResourceIdRequest(getStation(world), slot) : null;
    }
}
