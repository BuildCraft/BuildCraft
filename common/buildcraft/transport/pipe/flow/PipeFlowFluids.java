package buildcraft.transport.pipe.flow;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.BCLog;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IFluidFilter;
import buildcraft.api.core.IFluidHandlerAdv;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.PipeEventFluid;
import buildcraft.api.transport.neptune.IFlowFluid;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.PipeAPI;
import buildcraft.api.transport.neptune.PipeAPI.FluidTransferInfo;
import buildcraft.api.transport.neptune.PipeFlow;

import buildcraft.lib.misc.StringUtilBC;
import buildcraft.lib.misc.VecUtil;

public class PipeFlowFluids extends PipeFlow implements IFlowFluid, IDebuggable {

    private static final int DIRECTION_COOLDOWN = 60;
    private static final int COOLDOWN_INPUT = -DIRECTION_COOLDOWN;
    private static final int COOLDOWN_OUTPUT = DIRECTION_COOLDOWN;

    /** The number of pixels the fluid moves by per millisecond */
    public static final double FLOW_MULTIPLIER = 0.016;

    private final FluidTransferInfo fluidTransferInfo = PipeAPI.getFluidTransferInfo(pipe.getDefinition());

    /* Default to an additional second of fluid inserting and removal. This means that (for a normal pipe like cobble)
     * it will be 20 * (10 + 12) = 20 * 22 = 440 - oh that's not good is it */
    public final int capacity = fluidTransferInfo.transferPerTick * (40);// TEMP!

    private final Map<EnumPipePart, Section> sections = new EnumMap<>(EnumPipePart.class);
    private FluidStack currentFluid;
    private int currentDelay;

    public PipeFlowFluids(IPipe pipe) {
        super(pipe);
        for (EnumPipePart part : EnumPipePart.VALUES) {
            sections.put(part, new Section(part));
        }
    }

    public PipeFlowFluids(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        for (EnumPipePart part : EnumPipePart.VALUES) {
            sections.put(part, new Section(part));
        }
        if (nbt.hasKey("fluid")) {
            setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("fluid")));
        } else {
            setFluid(null);
        }

        for (EnumPipePart part : EnumPipePart.VALUES) {
            int direction = part.getIndex();
            if (nbt.hasKey("tank[" + direction + "]")) {
                NBTTagCompound compound = nbt.getCompoundTag("tank[" + direction + "]");
                if (compound.hasKey("FluidType")) {
                    FluidStack stack = FluidStack.loadFluidStackFromNBT(compound);
                    if (currentFluid == null) {
                        setFluid(stack);
                    }
                    if (stack.isFluidEqual(currentFluid)) {
                        sections.get(part).readFromNbt(compound);
                    }
                } else {
                    sections.get(part).readFromNbt(compound);
                }
            }
        }
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();

        if (currentFluid != null) {
            NBTTagCompound fluidTag = new NBTTagCompound();
            currentFluid.writeToNBT(fluidTag);
            nbt.setTag("fluid", fluidTag);

            for (EnumPipePart part : EnumPipePart.VALUES) {
                int direction = part.getIndex();
                NBTTagCompound subTag = new NBTTagCompound();
                sections.get(part).writeToNbt(subTag);
                nbt.setTag("tank[" + direction + "]", subTag);
            }
        }

        return nbt;
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowFluids;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return oTile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face.getOpposite());
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return (T) sections.get(EnumPipePart.fromFacing(facing));
        }
        return super.getCapability(capability, facing);
    }

    // IFlowFluid

    @Override
    public FluidStack tryExtractFluid(int millibuckets, EnumFacing from, FluidStack filter) {
        // NOTE: all changes to this method probably also need to be applied to the advanced version below!
        if (from == null) {
            return null;
        }
        TileEntity connected = pipe.getConnectedTile(from);
        if (connected == null) {
            return null;
        }
        IFluidHandler fluidHandler = connected.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, from.getOpposite());
        if (fluidHandler == null) {
            return null;
        }
        if (filter == null) {
            filter = this.currentFluid;
        } else if (currentFluid != null && !filter.isFluidEqual(currentFluid)) {
            return null;
        }
        Section section = sections.get(EnumPipePart.fromFacing(from));
        millibuckets = Math.min(millibuckets, section.getMaxFilled());
        if (millibuckets <= 0) {
            return null;
        }
        FluidStack toAdd;
        if (filter == null) {
            toAdd = fluidHandler.drain(millibuckets, true);
        } else {
            filter = filter.copy();
            filter.amount = millibuckets;
            toAdd = fluidHandler.drain(filter, true);
        }
        if (toAdd == null || toAdd.amount <= 0) {
            return null;
        }
        int extracted = toAdd.amount;
        if (currentFluid == null) {
            setFluid(toAdd);
        }
        int reallyFilled = section.fill(extracted, true);
        section.ticksInDirection = COOLDOWN_INPUT;
        if (reallyFilled != extracted) {
            BCLog.logger.warn("[tryExtractFluid] Filled " + reallyFilled + " != extracted " + extracted //
                + " (maxExtract = " + millibuckets + ")" //
                + " (handler = " + fluidHandler.getClass() + ") @" + pipe.getHolder().getPipePos());
        }
        return toAdd;
    }

    @Override
    public FluidStack tryExtractFluidAdv(int millibuckets, EnumFacing from, IFluidFilter filter) {
        // Mostly a copy of the above method
        if (from == null || filter == null || millibuckets <= 0) {
            return null;
        }
        TileEntity connected = pipe.getConnectedTile(from);
        if (connected == null) {
            return null;
        }
        IFluidHandler fluidHandler = connected.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, from.getOpposite());
        if (!(fluidHandler instanceof IFluidHandlerAdv)) {
            return null;
        }
        IFluidHandlerAdv handlerAdv = (IFluidHandlerAdv) fluidHandler;
        if (currentFluid != null) {
            if (!filter.matches(currentFluid)) {
                return null;
            }
            final IFluidFilter existing = filter;
            filter = (fluid) -> currentFluid.isFluidEqual(fluid) && existing.matches(fluid);
        }
        Section section = sections.get(EnumPipePart.fromFacing(from));
        millibuckets = Math.min(millibuckets, section.getMaxFilled());
        if (millibuckets <= 0) {
            return null;
        }
        FluidStack toAdd = handlerAdv.drain(filter, millibuckets, true);
        if (toAdd == null || toAdd.amount <= 0) {
            return null;
        }
        millibuckets = toAdd.amount;
        if (currentFluid == null) {
            setFluid(toAdd);
        }
        int reallyFilled = section.fill(millibuckets, true);
        section.ticksInDirection = COOLDOWN_INPUT;
        if (reallyFilled != millibuckets) {
            BCLog.logger.warn("[tryExtractFluidAdv] Filled " + reallyFilled + " != extracted " + millibuckets //
                + " (handler = " + fluidHandler.getClass() + ") @" + pipe.getHolder().getPipePos());
        }
        return toAdd;
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
            line += " " + section.getCurrentDirection() + " (" + section.ticksInDirection + ")";

            line += " [";
            int last = -1;
            int skipped = 0;

            for (int i : section.incoming) {
                if (i != last) {
                    if (skipped > 0) {
                        line += "..." + skipped + "... ";
                        skipped = 0;
                    }
                    last = i;
                    line += i + ", ";
                } else {
                    skipped++;
                }
            }
            if (skipped > 0) {
                line += "..." + skipped + "... ";
                skipped = 0;
            }
            line += "0]";

            left.add(line);
        }
    }

    // Rendering

    @SideOnly(Side.CLIENT)
    public FluidStack getFluidStackForRender() {
        return currentFluid;
    }

    @SideOnly(Side.CLIENT)
    public double[] getAmountsForRender(float partialTicks) {
        double[] arr = new double[7];
        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section s = sections.get(part);
            arr[part.getIndex()] = s.clientAmountLast * (partialTicks) + s.clientAmountThis * (1 - partialTicks);
        }
        return arr;
    }

    @SideOnly(Side.CLIENT)
    public Vec3d[] getOffsetsForRender(float partialTicks) {
        Vec3d[] arr = new Vec3d[7];
        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section s = sections.get(part);
            if (s.offsetLast != null & s.offsetThis != null) {
                arr[part.getIndex()] = s.offsetLast.scale(partialTicks).add(s.offsetThis.scale(1 - partialTicks));
            }
        }
        return arr;
    }

    // Internal logic

    private void setFluid(FluidStack fluid) {
        currentFluid = fluid;
        if (fluid != null) {
            currentDelay = (int) PipeAPI.getFluidTransferInfo(pipe.getDefinition()).transferDelayMultiplier;
            // (int) (fluidTransferInfo.transferDelayMultiplier * fluid.getFluid().getViscosity(fluid) / 100);
        } else {
            currentDelay = (int) PipeAPI.getFluidTransferInfo(pipe.getDefinition()).transferDelayMultiplier;
        }
        for (Section section : sections.values()) {
            section.incoming = new int[currentDelay];
            section.currentTime = 0;
            section.ticksInDirection = 0;
        }
    }

    @Override
    public void onTick() {
        if (currentFluid == null) {
            return;
        }
        World world = pipe.getHolder().getPipeWorld();
        for (EnumPipePart part : EnumPipePart.VALUES) {
            sections.get(part).tickClient();
        }
        if (world.isRemote) {
            return;
        }

        // int timeSlot = (int) (world.getTotalWorldTime() % currentDelay);
        int totalFluid = 0;
        boolean canOutput = false;

        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section section = sections.get(part);
            section.currentTime = (section.currentTime + 1) % currentDelay;
            section.advanceForMovement();
            totalFluid += section.amount;
            if (section.getCurrentDirection().canOutput()) {
                canOutput = true;
            }
        }
        if (totalFluid == 0) {
            setFluid(null);
        } else {
            // Fluid movement is split into 3 parts
            // - move from pipe (to other tiles)
            // - move from center (to sides)
            // - move into center (from sides)

            if (canOutput) {
                moveFromPipe();
            }
            moveFromCenter();
            moveToCenter();
        }

        // tick cooldowns
        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section section = sections.get(part);
            if (section.ticksInDirection > 0) {
                section.ticksInDirection--;
            } else if (section.ticksInDirection < 0) {
                section.ticksInDirection++;
            }
        }
    }

    private void moveFromPipe() {
        for (EnumPipePart part : EnumPipePart.FACES) {
            Section section = sections.get(part);
            if (section.getCurrentDirection().canOutput()) {
                PipeEventFluid.SideCheck sideCheck = new PipeEventFluid.SideCheck(pipe.getHolder(), this, currentFluid);
                sideCheck.disallowAllExcept(part.face);
                pipe.getHolder().fireEvent(sideCheck);
                if (sideCheck.getOrder().size() == 1) {
                    TileEntity target = pipe.getConnectedTile(part.face);
                    if (target == null) continue;
                    EnumFacing opposite = part.face.getOpposite();
                    IFluidHandler cap = target.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, opposite);
                    if (cap == null) continue;

                    FluidStack fluidToPush = new FluidStack(currentFluid, section.drainInternal(fluidTransferInfo.transferPerTick, false));

                    if (fluidToPush.amount > 0) {
                        int filled = cap.fill(fluidToPush, true);
                        if (filled > 0) {
                            section.drainInternal(filled, true);
                            section.ticksInDirection = COOLDOWN_OUTPUT;
                        }
                    }
                }
            }
        }
    }

    private void moveFromCenter() {
        Section center = sections.get(EnumPipePart.CENTER);
        // Split liquids moving to output equally based on flowrate, how much each side can accept and available liquid
        int totalAvailable = center.getMaxDrained();
        if (totalAvailable < 1) {
            return;
        }

        int flowRate = fluidTransferInfo.transferPerTick;
        Set<EnumFacing> realDirections = EnumSet.noneOf(EnumFacing.class);

        // Move liquid from the center to the output sides
        for (EnumFacing direction : EnumFacing.VALUES) {
            if (sections.get(EnumPipePart.fromFacing(direction)).getCurrentDirection().canOutput() && pipe.isConnected(direction)) {
                realDirections.add(direction);
            }
        }

        if (realDirections.size() > 0) {
            PipeEventFluid.SideCheck sideCheck = new PipeEventFluid.SideCheck(pipe.getHolder(), this, currentFluid);
            sideCheck.disallowAllExcept(realDirections);
            pipe.getHolder().fireEvent(sideCheck);

            EnumSet<EnumFacing> set = sideCheck.getOrder();

            List<EnumFacing> random = new ArrayList<>(set);
            Collections.shuffle(random);

            float min = Math.min(flowRate * realDirections.size(), totalAvailable) / (float) flowRate / realDirections.size();

            for (EnumFacing direction : random) {
                Section section = sections.get(EnumPipePart.fromFacing(direction));
                int available = section.fill(flowRate, false);
                int amountToPush = (int) (available * min);
                if (amountToPush < 1) {
                    amountToPush++;
                }

                amountToPush = center.drainInternal(amountToPush, false);
                if (amountToPush > 0) {
                    int filled = section.fill(amountToPush, true);
                    if (filled > 0) {
                        center.drainInternal(filled, true);
                        section.ticksInDirection = COOLDOWN_OUTPUT;
                    }
                    // FIXME: This is the animated flow variable
                    // flow[direction.ordinal()] = 1;
                }
            }
        }
    }

    private void moveToCenter() {
        int transferInCount = 0;
        Section center = sections.get(EnumPipePart.CENTER);
        int spaceAvailable = capacity - center.amount;
        int flowRate = fluidTransferInfo.transferPerTick;

        int[] inputPerTick = new int[6];
        for (EnumPipePart part : EnumPipePart.FACES) {
            Section section = sections.get(part);
            inputPerTick[part.getIndex()] = 0;
            if (section.getCurrentDirection().canInput()) {
                inputPerTick[part.getIndex()] = section.drainInternal(flowRate, false);
                if (inputPerTick[part.getIndex()] > 0) {
                    transferInCount++;
                }
            }
        }

        float min = Math.min(flowRate * transferInCount, spaceAvailable) / (float) flowRate / transferInCount;
        for (EnumPipePart part : EnumPipePart.FACES) {
            Section section = sections.get(part);
            // Move liquid from input sides to the center
            if (inputPerTick[part.getIndex()] > 0) {
                int amountToDrain = (int) (inputPerTick[part.getIndex()] * min);
                if (amountToDrain < 1) {
                    amountToDrain++;
                }

                int amountToPush = section.drainInternal(amountToDrain, false);
                if (amountToPush > 0) {
                    int filled = center.fill(amountToPush, true);
                    section.drainInternal(filled, true);
                    if (filled > 0) {
                        section.ticksInDirection = COOLDOWN_INPUT;
                    }
                    // FIXME: This is the animated flow variable
                    // flow[dir.ordinal()] = -1;
                }
            }
        }
    }

    /** Holds data about a single section of this pipe. */
    class Section implements IFluidHandler {
        final EnumPipePart part;

        int amount;

        int currentTime;

        /** Map of [time] -> [amount inserted].
         * 
         * Used to implement the delayed fluid travelling. */
        int[] incoming = new int[1];

        /** If 0 then fluids can move from this in either direction.
         * 
         * If less than 0 then fluids can only move into this section from other tiles, and outputs to other sections.
         * 
         * If greater than 0 then fluids can only move out of this section into other tiles. */
        int ticksInDirection = 0;

        // Client side fields

        /** Used to interpolate between {@link #clientAmountThis} and {@link #clientAmountLast} for rendering. */
        double clientAmountThis, clientAmountLast;

        /** Holds the amount of fluid was last sent to us from the sever */
        int target;

        /** The world-times of when the last message was received. */
        long targetTime = -1;

        Vec3d offsetLast, offsetThis;

        Section(EnumPipePart part) {
            this.part = part;
        }

        void writeToNbt(NBTTagCompound nbt) {
            nbt.setShort("capacity", (short) amount);

            for (int i = 0; i < incoming.length; ++i) {
                nbt.setShort("in[" + i + "]", (short) incoming[i]);
            }
        }

        void readFromNbt(NBTTagCompound nbt) {
            this.amount = nbt.getShort("capacity");

            for (int i = 0; i < incoming.length; ++i) {
                incoming[i] = nbt.getShort("in[" + i + "]");
            }
        }

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

        /** @return The fluid filled */
        int fill(int maxFill, boolean doFill) {
            int amountToFill = Math.min(getMaxFilled(), maxFill);
            if (amountToFill <= 0) {
                return 0;
            }
            if (doFill) {
                incoming[currentTime] += amountToFill;
                amount += amountToFill;
            }
            return amountToFill;
        }

        /** @param maxDrain
         * @param doDrain
         * @return The amount drained */
        int drainInternal(int maxDrain, boolean doDrain) {
            maxDrain = Math.min(maxDrain, getMaxDrained());
            if (maxDrain <= 0) {
                return 0;
            } else {
                if (doDrain) {
                    amount -= maxDrain;
                }
                return maxDrain;
            }
        }

        void advanceForMovement() {
            incoming[currentTime] = 0;
        }

        void setTime(int current) {
            currentTime = current;
        }

        Dir getCurrentDirection() {
            Dir dir = ticksInDirection == 0 ? Dir.NONE : ticksInDirection < 0 ? Dir.IN : Dir.OUT;
            return dir;
        }

        void writeClientMessage(PacketBuffer buffer) {
            buffer.writeShort(amount);
        }

        void handleClientMessage(PacketBuffer buffer) {
            targetTime = pipe.getHolder().getPipeWorld().getTotalWorldTime();
            target = buffer.readShort();
        }

        /** @return True if this still contains fluid, false if not. */
        boolean tickClient() {
            clientAmountLast = clientAmountThis;

            target = amount; // temp until networking is added

            if (target != clientAmountThis) {
                double diff = target - clientAmountThis;
                diff = Math.min(diff, fluidTransferInfo.transferPerTick);
                clientAmountThis += diff;
            }

            if (offsetThis == null) {
                offsetThis = Vec3d.ZERO;
            }
            offsetLast = offsetThis;

            if (part.face == null) {
                Vec3d dir = Vec3d.ZERO;
                // Firstly find all the outgoing faces
                for (EnumPipePart p : EnumPipePart.FACES) {
                    Section s = sections.get(p);
                    if (s.ticksInDirection > 0) {
                        dir = dir.add(new Vec3d(p.face.getDirectionVec()));
                    }
                }
                // If that failed then find all of the incoming faces
                for (EnumPipePart p : EnumPipePart.FACES) {
                    Section s = sections.get(p);
                    if (s.ticksInDirection < 0) {
                        dir = dir.add(new Vec3d(p.face.getDirectionVec()).scale(-1));
                    }
                }
                dir = new Vec3d(Math.signum(dir.xCoord), Math.signum(dir.yCoord), Math.signum(dir.zCoord));
                offsetThis = offsetThis.add(dir.scale(-FLOW_MULTIPLIER));
            } else {
                double mult = Math.signum(ticksInDirection);
                offsetThis = VecUtil.offset(offsetLast, part.face, -FLOW_MULTIPLIER * (mult));
            }

            double dx = offsetThis.xCoord >= 0.5 ? -1 : offsetThis.xCoord <= 0.5 ? 1 : 0;
            double dy = offsetThis.yCoord >= 0.5 ? -1 : offsetThis.yCoord <= 0.5 ? 1 : 0;
            double dz = offsetThis.zCoord >= 0.5 ? -1 : offsetThis.zCoord <= 0.5 ? 1 : 0;
            if (dx != 0 || dy != 0 || dz != 0) {
                offsetThis = offsetThis.addVector(dx, dy, dz);
                offsetLast = offsetLast.addVector(dx, dy, dz);
            }
            return clientAmountThis > 0 | clientAmountLast > 0;
        }

        // IFluidHandler

        @Override
        @Deprecated
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        /** @deprecated USE {@link #drainInternal(int, boolean)} rather than this! */
        @Override
        @Deprecated
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            return new IFluidTankProperties[0];
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            if (!getCurrentDirection().canInput() || !pipe.isConnected(part.face)) {
                return 0;
            }
            if (currentFluid == null || currentFluid.isFluidEqual(resource)) {
                if (doFill) {
                    if (currentFluid == null) {
                        setFluid(resource);
                    }
                }
                int filled = fill(resource.amount, doFill);
                if (filled > 0 && doFill) {
                    ticksInDirection = COOLDOWN_INPUT;
                }
                return filled;
            }
            return 0;
        }
    }

    /** Enum used for the current direction that a fluid is flowing. */
    enum Dir {
        IN,
        NONE,
        OUT;

        public boolean isInput() {
            return this == IN;
        }

        public boolean canInput() {
            return this != OUT;
        }

        public boolean isOutput() {
            return this == OUT;
        }

        public boolean canOutput() {
            return this != IN;
        }
    }
}
