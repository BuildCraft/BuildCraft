package buildcraft.transport.pipe.flow;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.neptune.IFlowFluid;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.PipeAPI;
import buildcraft.api.transport.neptune.PipeAPI.FluidTransferInfo;
import buildcraft.api.transport.neptune.PipeFlow;

import buildcraft.lib.misc.StringUtilBC;

public class PipeFlowFluids extends PipeFlow implements IFlowFluid, IDebuggable {

    private static final int DIRECTION_COOLDOWN = 30;

    private final FluidTransferInfo fluidTransferInfo = PipeAPI.getFluidTransferInfo(pipe.getDefinition());

    /* Default to an additional second of fluid inserting and removal. This means that (for a normal pipe like cobble)
     * it will be 20 * (10 + 12) = 20 * 22 = 440 - oh that's not good is it */
    private final int capacity = fluidTransferInfo.transferPerTick * (10 + fluidTransferInfo.transferDelay);

    private final Map<EnumPipePart, Section> sections = new EnumMap<>(EnumPipePart.class);
    private FluidStack currentFluid;
    private int currentDelay = fluidTransferInfo.transferDelay;

    public PipeFlowFluids(IPipe pipe) {
        super(pipe);
        for (EnumPipePart part : EnumPipePart.VALUES) {
            sections.put(part, new Section());
        }
    }

    public PipeFlowFluids(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        for (EnumPipePart part : EnumPipePart.VALUES) {
            sections.put(part, new Section());
        }
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowFluids;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return oTile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
    }

    // IFlowFluid

    @Override
    public int tryExtractFluid(int millibuckets, EnumFacing from, FluidStack filter) {
        if (from == null) {
            return 0;
        }
        TileEntity connected = pipe.getConnectedTile(from);
        if (connected == null) {
            return 0;
        }
        IFluidHandler fluidHandler = connected.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, from.getOpposite());
        if (fluidHandler == null) {
            return 0;
        }
        if (filter == null) {
            filter = this.currentFluid;
        } else if (currentFluid != null && !filter.isFluidEqual(currentFluid)) {
            return 0;
        }
        Section section = sections.get(EnumPipePart.fromFacing(from));
        millibuckets = Math.min(millibuckets, section.getMaxFilled());
        if (millibuckets <= 0) {
            return 0;
        }
        FluidStack toAdd;
        if (filter == null) {
            toAdd = fluidHandler.drain(millibuckets, true);
        } else {
            filter = filter.copy();
            filter.amount = millibuckets;
            toAdd = fluidHandler.drain(filter, true);
        }
        if (toAdd == null) {
            return 0;
        }
        if (currentFluid == null) {
            currentFluid = toAdd;
        }
        section.amount += toAdd.amount;
        return toAdd.amount;
    }

    // IDebuggable

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add(" - FluidType = " + (currentFluid == null ? "empty" : currentFluid.getLocalizedName()));

        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section section = sections.get(part);
            if (section == null) {
                continue;
            }
            String line = " - " + StringUtilBC.getLocalized(part.face) + " = ";
            line += (section.amount > 0 ? TextFormatting.GREEN : "");
            line += section.amount + "" + TextFormatting.RESET + "mB";
            left.add(line);
        }
    }

    // Internal logic

    @Override
    public void onTick() {
        World world = pipe.getHolder().getPipeWorld();
        if (world.isRemote) {
            return;
        }

        int timeSlot = (int) (world.getTotalWorldTime() % currentDelay);

        for (EnumPipePart part : EnumPipePart.VALUES) {

        }

        // Fluid movement is split into 3 parts :
        // - move from pipe (to other tiles)
        // - move from center (to sides)
        // - move into center (from sides)
    }

    /** Holds data about a single section of this pipe. */
    class Section {
        /**
         * 
         */
        int amount;

        /**
         * 
         */
        int currentTime;

        /** Map of [time] -> [amount inserted].
         * 
         * Used to implement the delayed fluid travelling, but only delays the */
        int[] incoming = new int[fluidTransferInfo.transferDelay];

        /** If 0 then fluids can move from this in either direction.
         * 
         * If less than 0 then fluids can only move into this section from other tiles, and outputs to other sections.
         * 
         * If greater than 0 then fluids can only move out of this section into other tiles. */
        int ticksInDirection = 0;

        /** @return The maximum amount of fluid that can be inserted into this pipe on this tick. */
        int getMaxFilled() {
            int avalibleTotal = capacity - amount;
            int avalibleThisTick = fluidTransferInfo.transferPerTick - incoming[currentTime];
            return Math.min(avalibleTotal, avalibleThisTick);
        }

        /** @return The maximum amount of fluid that can be extracted out of this pipe this tick. */
        int getMaxDrained() {
            int max = amount;
            for (int i : incoming) {
                max -= i;
            }
            return Math.min(max, fluidTransferInfo.transferPerTick);
        }

        /** @return The leftover fluid */
        int fill(int maxFill, boolean doFill) {
            int maxFilled = getMaxFilled();
            int actuallyFilled = Math.min(maxFill, maxFilled);
            if (actuallyFilled <= 0) {
                return maxFill;
            }
            if (doFill) {
                amount += actuallyFilled;
                incoming[currentTime] += actuallyFilled;
            }
            return maxFill - actuallyFilled;
        }

        /** @param maxDrain
         * @param doDrain
         * @return */
        int drain(int maxDrain, boolean doDrain) {
            int maxDrained = getMaxDrained();
            int actuallyDrained = Math.min(maxDrained, maxDrain);
            if (actuallyDrained <= 0) {
                return 0;
            }
            if (doDrain) {
                amount -= actuallyDrained;
            }
            return actuallyDrained;
        }

        void advanceForMovement() {
            incoming[currentTime] = 0;
        }

        void setTime(short current) {
            currentTime = current;
        }

        Dir getCurrentDirection() {
            return ticksInDirection == 0 ? Dir.NONE : ticksInDirection < 0 ? Dir.IN : Dir.OUT;
        }
    }

    /** Enum used for the current direction that a fluid is flowing. */
    enum Dir {
        IN,
        NONE,
        OUT;

        public boolean canInput() {
            return this != OUT;
        }

        public boolean canOutput() {
            return this != IN;
        }
    }
}
