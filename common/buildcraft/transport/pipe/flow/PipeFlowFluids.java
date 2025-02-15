/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.core.*;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.PipeApi.FluidTransferInfo;
import buildcraft.api.transport.pipe.PipeEventFluid.OnMoveToCentre;
import buildcraft.api.transport.pipe.PipeEventFluid.PreMoveToCentre;
import buildcraft.core.BCCoreConfig;
import buildcraft.core.BCCoreItems;
import buildcraft.lib.misc.*;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;
import buildcraft.lib.net.cache.NetworkedObjectCache;
import buildcraft.transport.BCTransportStatements;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.network.NetworkDirection;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class PipeFlowFluids extends PipeFlow implements IFlowFluid, IDebuggable {

    private static final int DIRECTION_COOLDOWN = 60;
    private static final int COOLDOWN_INPUT = -DIRECTION_COOLDOWN;
    private static final int COOLDOWN_OUTPUT = DIRECTION_COOLDOWN;

    private static final InteractionResultHolder<FluidStack> FAILED_EXTRACT = new InteractionResultHolder<>(InteractionResult.FAIL, null);
    private static final InteractionResultHolder<FluidStack> PASSED_EXTRACT = new InteractionResultHolder<>(InteractionResult.PASS, null);

    public static final int NET_FLUID_AMOUNTS = 2;

    /** The number of pixels the fluid moves by per millisecond */
    public static final double FLOW_MULTIPLIER = 0.016;

    private final FluidTransferInfo fluidTransferInfo = PipeApi.getFluidTransferInfo(pipe.getDefinition());

    /* Default to an additional second of fluid inserting and removal. This means that (for a normal pipe like cobble)
     * it will be 20 * (10 + 12) = 20 * 22 = 440 - oh that's not good is it */
    public final int capacity = Math.max(FluidType.BUCKET_VOLUME, fluidTransferInfo.transferPerTick * (10));// TEMP!

    private final Map<EnumPipePart, Section> sections = new EnumMap<>(EnumPipePart.class);
    private FluidStack currentFluid;
    private int currentDelay;
    private final SafeTimeTracker tracker = new SafeTimeTracker(BCCoreConfig.networkUpdateRate, 4);

    // Client fields for interpolating amounts
    private long lastMessage, lastMessageMinus1;
    private NetworkedObjectCache<FluidStack>.Link clientFluid = null;

    public PipeFlowFluids(IPipe pipe) {
        super(pipe);
        for (EnumPipePart part : EnumPipePart.VALUES) {
            sections.put(part, new Section(part));
        }
    }

    public PipeFlowFluids(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        for (EnumPipePart part : EnumPipePart.VALUES) {
            sections.put(part, new Section(part));
        }
        if (nbt.contains("fluid")) {
            setFluid(FluidStack.loadFluidStackFromNBT(nbt.getCompound("fluid")));
        } else {
            setFluid(null);
        }

        for (EnumPipePart part : EnumPipePart.VALUES) {
            int direction = part.getIndex();
            if (nbt.contains("tank[" + direction + "]")) {
                CompoundTag compound = nbt.getCompound("tank[" + direction + "]");
                if (compound.contains("FluidType")) {
                    FluidStack stack = FluidStack.loadFluidStackFromNBT(compound);
                    if (currentFluid == null) {
                        setFluid(stack);
                    }
                    if (stack != null && stack.isFluidEqual(currentFluid)) {
                        sections.get(part).readFromNbt(compound);
                    }
                } else {
                    sections.get(part).readFromNbt(compound);
                }
            }
        }
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();

        if (currentFluid != null) {
            CompoundTag fluidTag = new CompoundTag();
            currentFluid.writeToNBT(fluidTag);
            nbt.put("fluid", fluidTag);

            for (EnumPipePart part : EnumPipePart.VALUES) {
                int direction = part.getIndex();
                CompoundTag subTag = new CompoundTag();
                sections.get(part).writeToNbt(subTag);
                nbt.put("tank[" + direction + "]", subTag);
            }
        }

        return nbt;
    }

    @Override
    public boolean canConnect(Direction face, PipeFlow other) {
        return other instanceof IFlowFluid;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {
        return oTile.getCapability(CapUtil.CAP_FLUIDS, face.getOpposite()).isPresent();
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (capability == CapUtil.CAP_FLUIDS) {
//            return CapUtil.CAP_FLUIDS.cast(sections.get(EnumPipePart.fromFacing(facing)));
            return LazyOptional.of(() -> sections.get(EnumPipePart.fromFacing(facing))).cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        super.addDrops(toDrop, fortune);
        if (currentFluid != null && BCCoreItems.fragileFluidShard != null) {
            int totalAmount = 0;
            for (EnumPipePart part : EnumPipePart.VALUES) {
                totalAmount += sections.get(part).amount;
            }
            if (totalAmount > 0) {
                BCCoreItems.fragileFluidShard.get().addFluidDrops(toDrop, new FluidStack(currentFluid, totalAmount));
            }
        }
    }

    public boolean doesContainFluid() {
        for (EnumPipePart part : EnumPipePart.VALUES) {
            if (sections.get(part).amount > 0) {
                return true;
            }
        }
        return false;
    }

    @PipeEventHandler
    public static void addTriggers(PipeEventStatement.AddTriggerInternal event) {
        event.triggers.add(BCTransportStatements.TRIGGER_FLUIDS_TRAVERSING);
    }

    // IFlowFluid

    @Override
//    public FluidStack tryExtractFluid(int millibuckets, Direction from, FluidStack filter, boolean simulate)
    public FluidStack tryExtractFluid(int millibuckets, Direction from, FluidStack filter, IFluidHandler.FluidAction action) {
        FluidExtractor extractor = (mb, c, handler) ->
        {
            FluidStack f = filter == null ? c : filter;
//            return extractSimple(mb, f, handler, simulate);
            return extractSimple(mb, f, handler, action);
        };
//        return tryExtractFluidInternal(millibuckets, from, extractor, simulate).getObject();
        return tryExtractFluidInternal(millibuckets, from, extractor, action).getObject();
    }

    @Override
//    public InteractionResultHolder<FluidStack> tryExtractFluidAdv(int millibuckets, Direction from, IFluidFilter filter, boolean simulate)
    public InteractionResultHolder<FluidStack> tryExtractFluidAdv(int millibuckets, Direction from, IFluidFilter filter, IFluidHandler.FluidAction action) {
        FluidExtractor extractor = (mb, c, handler) ->
        {
            if (c != null) {
                if (!filter.matches(c)) {
                    return null;
                }
//                return extractSimple(mb, c, handler, simulate);
                return extractSimple(mb, c, handler, action);
            }
            if (handler instanceof IFluidHandlerAdv) {
                // This will likely be cheaper
                IFluidHandlerAdv handlerAdv = (IFluidHandlerAdv) handler;
//                return handlerAdv.drain(filter, mb, !simulate);
                return handlerAdv.drain(filter, mb, action);
            }

            // Search for the first valid fluid

//            IFluidTankProperties[] tanks = handler.getTankProperties();
            int tanks = handler.getTanks();
//            if (tanks == null)
            if (tanks == 0) {
                return null;
            }
//            for (IFluidTankProperties tank : tanks)
            for (int i = 0; i < tanks; i++) {
//                FluidStack contents = tank.getContents();
                FluidStack contents = handler.getFluidInTank(i);
//                if (contents != null && filter.matches(contents))
                if (!contents.isEmpty() && filter.matches(contents)) {
//                    FluidStack extracted = extractSimple(mb, contents, handler, simulate);
                    FluidStack extracted = extractSimple(mb, contents, handler, action);
//                    if (extracted != null)
                    if (!extracted.isEmpty()) {
                        return extracted;
                    }
                }
            }
            return null;
        };
//        return tryExtractFluidInternal(millibuckets, from, extractor, simulate);
        return tryExtractFluidInternal(millibuckets, from, extractor, action);
    }

    @FunctionalInterface
    private interface FluidExtractor {
        FluidStack extract(int millibuckets, FluidStack current, IFluidHandler handler);
    }

    // private InteractionResultHolder<FluidStack> tryExtractFluidInternal(int millibuckets, Direction from, FluidExtractor extractor, boolean simulate)
    private InteractionResultHolder<FluidStack> tryExtractFluidInternal(int millibuckets, Direction from, FluidExtractor extractor, IFluidHandler.FluidAction action) {
        if (from == null || millibuckets <= 0) {
            return FAILED_EXTRACT;
        }
        IFluidHandler fluidHandler = pipe.getHolder().getCapabilityFromPipe(from, CapUtil.CAP_FLUIDS);
        if (fluidHandler == null) {
            // FIXME: WRONG PLACE!!!
            return PASSED_EXTRACT;
        }
        Section section = sections.get(EnumPipePart.fromFacing(from));
        Section middle = sections.get(EnumPipePart.CENTER);
        millibuckets = Math.min(millibuckets, capacity * 2 - section.amount - middle.amount);
        if (millibuckets <= 0) {
            return FAILED_EXTRACT;
        }
        FluidStack toAdd = extractor.extract(millibuckets, currentFluid, fluidHandler);
        if (toAdd == null || toAdd.getAmount() <= 0) {
            return FAILED_EXTRACT;
        }
        millibuckets = toAdd.getAmount();
//        if (currentFluid == null && !simulate)
        if (currentFluid == null && action.execute()) {
            setFluid(toAdd);
        }
//        int reallyFilled = section.fillInternal(millibuckets, !simulate);
        int reallyFilled = section.fillInternal(millibuckets, action.execute());
        int leftOver = millibuckets - reallyFilled;
//        reallyFilled += middle.fillInternal(leftOver, !simulate);
        reallyFilled += middle.fillInternal(leftOver, action.execute());
//        if (!simulate)
        if (action.execute()) {
            section.ticksInDirection = COOLDOWN_INPUT;
        }
        if (reallyFilled != millibuckets) {
            BCLog.logger.warn(
                    "[tryExtractFluidAdv] Filled "
                            + reallyFilled + " != extracted " + millibuckets //
                            + " (handler = " + fluidHandler.getClass() + ") @" + pipe.getHolder().getPipePos()
            );
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, toAdd);
    }

    // private static FluidStack extractSimple(int millibuckets, FluidStack filter, IFluidHandler handler, boolean simulate)
    private static FluidStack extractSimple(int millibuckets, FluidStack filter, IFluidHandler handler, IFluidHandler.FluidAction action) {
        if (filter == null) {
//            return handler.drain(millibuckets, !simulate);
            return handler.drain(millibuckets, action);
        }
        filter = filter.copy();
        filter.setAmount(millibuckets);
//        FluidStack drained = handler.drain(filter, !simulate);
        FluidStack drained = handler.drain(filter, action);
//        if (drained != null)
        if (!drained.isEmpty()) {
            if (!filter.isFluidEqual(filter)) {
                String detail = "(Filter = " + StringUtilBC.fluidToString(filter);
                detail += ",\nactually drained = " + StringUtilBC.fluidToString(drained) + ")";
                detail += ",\nIFluidHandler = " + handler.getClass() + "(" + handler + ")";
                throw new IllegalStateException("Drained fluid did not equal filter fluid!\n" + detail);
            }
        }
        return drained;
    }

    @Override
//    public int insertFluidsForce(FluidStack fluid, @Nullable EnumFacing from, boolean simulate)
    public int insertFluidsForce(FluidStack fluid, @Nullable Direction from, FluidAction action) {
        Section s = sections.get(EnumPipePart.CENTER);
        if (fluid == null || fluid.getAmount() == 0) {
            return 0;
        }
        if (currentFluid != null && !currentFluid.isFluidEqual(fluid)) {
            return 0;
        }
//        if (currentFluid == null && !simulate)
        if (currentFluid == null && action.execute()) {
            setFluid(fluid.copy());
        }
//        int filled = s.fill(fluid.getAmount(), !simulate);
        int filled = s.fill(fluid.getAmount(), action.execute());
        if (filled == 0) {
            return 0;
        }
//        if (simulate)
        if (action.simulate()) {
            return filled;
        }
        if (from != null) {
            sections.get(EnumPipePart.fromFacing(from)).ticksInDirection = COOLDOWN_INPUT;
        }
        return filled;
    }

    @Override
    @Nullable
//    public FluidStack extractFluidsForce(int min, int max, @Nullable EnumFacing section, boolean simulate)
    public FluidStack extractFluidsForce(int min, int max, @Nullable Direction section, FluidAction action) {
        if (min > max) {
            throw new IllegalArgumentException("Minimum (" + min + ") > maximum (" + max + ")");
        }
        if (max < 0) {
            return null;
        }
        Section s = sections.get(EnumPipePart.fromFacing(section));
        if (s.amount < min) {
            return null;
        }
        int amount = MathUtil.clamp(s.amount, min, max);
        FluidStack fluid = new FluidStack(currentFluid, amount);
//        if (!simulate)
        if (action.execute()) {
            s.amount -= amount;
            s.drainInternal(amount, false);
            if (s.amount == 0) {
                boolean isEmpty = true;
                for (Section s2 : sections.values()) {
                    isEmpty &= s2.amount == 0;
                }
                if (isEmpty) {
                    setFluid(null);
                }
            }
        }
        return fluid;
    }

    // IDebuggable

    @Override
//    public void getDebugInfo(List<String> left, List<String> right, Direction side)
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
        boolean isRemote = pipe.getHolder().getPipeWorld().isClientSide;

        FluidStack fluid = isRemote ? getFluidStackForRender() : currentFluid;
//        left.add(" - FluidType = " + (fluid == null ? "empty" : fluid.getDisplayName()));
        left.add(Component.literal(" - FluidType = ").append(fluid == null ? Component.literal("empty") : fluid.getDisplayName()));

        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section section = sections.get(part);
            if (section == null) {
                continue;
            }
//            StringBuilder line = new StringBuilder(" - " + LocaleUtil.localizeFacing(part.face) + " = ");
            MutableComponent firstPart = Component.literal(" - ").append(LocaleUtil.localizeFacingComponent(part.face)).append(Component.literal(" = "));
            StringBuilder line = new StringBuilder();
            int amount = isRemote ? section.target : section.amount;
            line.append(amount > 0 ? ChatFormatting.GREEN : "");
            line.append(amount).append("").append(ChatFormatting.RESET).append("mB");
            line.append(" ").append(section.getCurrentDirection()).append(" (").append(section.ticksInDirection).append(
                    ")"
            );

            line.append(" [");
            int last = -1;
            int skipped = 0;

            for (int i : section.incoming) {
                if (i != last) {
                    if (skipped > 0) {
                        line.append("...").append(skipped).append("... ");
                        skipped = 0;
                    }
                    last = i;
                    line.append(i).append(", ");
                } else {
                    skipped++;
                }
            }
            if (skipped > 0) {
                line.append("...").append(skipped).append("... ");
                skipped = 0;
            }
            line.append("0]");

//            left.add(line.toString());
            left.add(firstPart.append(Component.literal(line.toString())));
        }
    }

    // Rendering

    @OnlyIn(Dist.CLIENT)
    public FluidStack getFluidStackForRender() {
        return clientFluid == null ? null : clientFluid.get();
    }

    @OnlyIn(Dist.CLIENT)
    public double[] getAmountsForRender(float partialTicks) {
        double[] arr = new double[7];
        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section s = sections.get(part);
            arr[part.getIndex()] = s.clientAmountLast * (1 - partialTicks) + s.clientAmountThis * (partialTicks);
        }
        return arr;
    }

    @OnlyIn(Dist.CLIENT)
    public Vec3[] getOffsetsForRender(float partialTicks) {
        Vec3[] arr = new Vec3[7];
        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section s = sections.get(part);
            if (s.offsetLast != null & s.offsetThis != null) {
                arr[part.getIndex()] = s.offsetLast.scale(1 - partialTicks).add(s.offsetThis.scale(partialTicks));
            }
        }
        return arr;
    }

    // Internal logic

    private void setFluid(FluidStack fluid) {
        currentFluid = fluid;
        if (fluid != null) {
            currentDelay = (int) PipeApi.getFluidTransferInfo(pipe.getDefinition()).transferDelayMultiplier;
            // (int) (fluidTransferInfo.transferDelayMultiplier * fluid.getFluid().getViscosity(fluid) / 100);
        } else {
            currentDelay = (int) PipeApi.getFluidTransferInfo(pipe.getDefinition()).transferDelayMultiplier;
        }
        for (Section section : sections.values()) {
            section.incoming = new int[currentDelay];
            section.currentTime = 0;
            section.ticksInDirection = 0;
        }
    }

    @Override
    public void onTick() {
        Level world = pipe.getHolder().getPipeWorld();
        if (world.isClientSide) {
            for (EnumPipePart part : EnumPipePart.VALUES) {
                sections.get(part).tickClient();
            }
            return;
        }

        if (currentFluid != null) {
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

        boolean send = false;

        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section section = sections.get(part);
            if (section.amount != section.lastSentAmount) {
                send = true;
                break;
            } else {
                Dir should = Dir.get(section.ticksInDirection);
                if (section.lastSentDirection != should) {
                    send = true;
                    break;
                }
            }
        }

        if (send && tracker.markTimeIfDelay(world)) {
            // send a net update
            sendPayload(NET_FLUID_AMOUNTS);
        }
    }

    private void moveFromPipe() {
        for (EnumPipePart part : EnumPipePart.FACES) {
            Section section = sections.get(part);
            if (section.getCurrentDirection().canOutput()) {
                int maxDrain = section.drainInternal(fluidTransferInfo.transferPerTick, false);
                if (maxDrain <= 0) {
                    continue;
                }
                PipeEventFluid.SideCheck sideCheck = new PipeEventFluid.SideCheck(pipe.getHolder(), this, currentFluid);
                sideCheck.disallowAllExcept(part.face);
                pipe.getHolder().fireEvent(sideCheck);
                if (sideCheck.getOrder().size() == 1) {
                    IFluidHandler fluidHandler = pipe.getHolder().getCapabilityFromPipe(part.face, CapUtil.CAP_FLUIDS);
                    if (fluidHandler == null) continue;

                    FluidStack fluidToPush = new FluidStack(currentFluid, maxDrain);

                    if (fluidToPush.getAmount() > 0) {
                        int filled = fluidHandler.fill(fluidToPush, IFluidHandler.FluidAction.EXECUTE);
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
        Set<Direction> realDirections = EnumSet.noneOf(Direction.class);

        // Move liquid from the center to the output sides
        for (Direction direction : Direction.VALUES) {
            Section section = sections.get(EnumPipePart.fromFacing(direction));
            if (!section.getCurrentDirection().canOutput()) {
                continue;
            }
            if (
                    section.getMaxFilled() > 0
                            && pipe.getHolder().getCapabilityFromPipe(direction, CapUtil.CAP_FLUIDS) != null
            ) {
                realDirections.add(direction);
            }
        }

        if (realDirections.size() > 0) {
            PipeEventFluid.SideCheck sideCheck = new PipeEventFluid.SideCheck(pipe.getHolder(), this, currentFluid);
            sideCheck.disallowAllExcept(realDirections);
            pipe.getHolder().fireEvent(sideCheck);

            EnumSet<Direction> set = sideCheck.getOrder();

            List<Direction> random = new ArrayList<>(set);
            Collections.shuffle(random);

            float min = Math.min(flowRate * realDirections.size(), totalAvailable)
                    / (float) flowRate / realDirections.size();

            for (Direction direction : random) {
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
        if (spaceAvailable <= 0 || center.getMaxFilled() <= 0) {
            return;
        }
        int flowRate = fluidTransferInfo.transferPerTick;

        List<EnumPipePart> faces = new ArrayList<>();
        Collections.addAll(faces, EnumPipePart.FACES);
        Collections.shuffle(faces);

        int[] inputPerTick = new int[6];
        for (EnumPipePart part : faces) {
            Section section = sections.get(part);
            inputPerTick[part.getIndex()] = 0;
            if (section.getCurrentDirection().canInput()) {
                inputPerTick[part.getIndex()] = section.drainInternal(flowRate, false);
                if (inputPerTick[part.getIndex()] > 0) {
                    transferInCount++;
                }
            }
        }

        int[] totalOffered = Arrays.copyOf(inputPerTick, 6);
        PreMoveToCentre preMove = new PreMoveToCentre(
                pipe.getHolder(), this, currentFluid, Math.min(flowRate, spaceAvailable), totalOffered, inputPerTick
        );
        // Event handlers edit the array in-place
        pipe.getHolder().fireEvent(preMove);

        int[] fluidLeavingSide = new int[6];

        // Work out how much fluid should leave
        int left = Math.min(flowRate, spaceAvailable);
        float min = Math.min(flowRate * transferInCount, spaceAvailable) / (float) flowRate / transferInCount;
        for (EnumPipePart part : EnumPipePart.FACES) {
            Section section = sections.get(part);
            // Move liquid from input sides to the centre
            int i = part.getIndex();
            if (inputPerTick[i] > 0) {
                int amountToDrain = (int) (inputPerTick[i] * min);
                if (amountToDrain < 1) {
                    amountToDrain++;
                }
                if (amountToDrain > left) {
                    amountToDrain = left;
                }
                int amountToPush = section.drainInternal(amountToDrain, false);
                if (amountToPush > 0) {
                    fluidLeavingSide[i] = amountToPush;
                    left -= amountToPush;
                }
            }
        }

        int[] fluidEnteringCentre = Arrays.copyOf(fluidLeavingSide, 6);
        OnMoveToCentre move = new OnMoveToCentre(
                pipe.getHolder(), this, currentFluid, fluidLeavingSide, fluidEnteringCentre
        );
        pipe.getHolder().fireEvent(move);

        for (EnumPipePart part : EnumPipePart.FACES) {
            Section section = sections.get(part);
            int i = part.getIndex();
            int leaving = fluidLeavingSide[i];
            if (leaving > 0) {
                int actuallyDrained = section.drainInternal(leaving, true);
                if (actuallyDrained != leaving) {
                    throw new IllegalStateException(
                            "Couldn't drain " + leaving + " from " + part + ", only drained " + actuallyDrained
                    );
                }
                if (actuallyDrained > 0) {
                    section.ticksInDirection = COOLDOWN_INPUT;
                }
                int entering = fluidEnteringCentre[i];
                if (entering > 0) {
                    int actuallyFilled = center.fill(entering, true);
                    if (actuallyFilled != entering) {
                        throw new IllegalStateException(
                                "Couldn't fill " + entering + " from " + part + ", only filled " + actuallyFilled
                        );
                    }
                }
            }
        }
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buf, Dist side) {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(buf);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_FLUID_AMOUNTS || id == NET_ID_FULL_STATE) {
                boolean full = id == NET_ID_FULL_STATE;
                if (currentFluid == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writeInt(BuildCraftObjectCaches.CACHE_FLUIDS.server().store(currentFluid));
                }
                for (EnumPipePart part : EnumPipePart.VALUES) {
                    Section section = sections.get(part);
                    if (full) {
                        buffer.writeShort(section.amount);
                    } else if (section.amount == section.lastSentAmount) {
                        buffer.writeBoolean(false);
                    } else {
                        buffer.writeBoolean(true);
                        buffer.writeShort(section.amount);
                        section.lastSentAmount = section.amount;
                    }
                    Dir should = Dir.get(section.ticksInDirection);
                    buffer.writeEnum(should); // This writes out 2 bits so don't bother with a boolean flag
                    section.lastSentDirection = should;
                }
            }
        }
    }

    @Override
//    public void readPayload(int id, PacketBuffer buf, Dist side) throws IOException
    public void readPayload(int id, FriendlyByteBuf buf, NetworkDirection side) throws IOException {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(buf);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_FLUID_AMOUNTS || id == NET_ID_FULL_STATE) {
                boolean full = id == NET_ID_FULL_STATE;
                if (buffer.readBoolean()) {
                    int fluidId = buffer.readInt();
                    clientFluid = BuildCraftObjectCaches.CACHE_FLUIDS.client().retrieve(fluidId);
                }
                for (EnumPipePart part : EnumPipePart.VALUES) {
                    Section section = sections.get(part);
                    if (full || buffer.readBoolean()) {
                        section.target = buffer.readShort();
                        if (full) {
                            section.clientAmountLast = section.clientAmountThis = section.target;
                        }
                    }

                    Dir dir = buffer.readEnum(Dir.class);
                    section.ticksInDirection = dir == Dir.NONE ? 0 : dir == Dir.IN ? COOLDOWN_INPUT : COOLDOWN_OUTPUT;
                }
                lastMessageMinus1 = lastMessage;
                lastMessage = pipe.getHolder().getPipeWorld().getGameTime();
            }
        }
    }

    /** Holds data about a single section of this pipe. */
    class Section implements IFluidHandler {
        final EnumPipePart part;

        int amount = 0;

        int lastSentAmount = 0;

        Dir lastSentDirection = Dir.NONE;

        int currentTime = 0;

        /** Map of [time] -> [amount inserted]. Used to implement the delayed fluid travelling. */
        int[] incoming = new int[1];

        int incomingTotalCache = 0;

        /** If 0 then fluids can move from this in either direction. If less than 0 then fluids can only move into this
         * section from other tiles, and outputs to other sections. If greater than 0 then fluids can only move out of
         * this section into other tiles. */
        int ticksInDirection = 0;

        // Client side fields

        /** Used to interpolate between {@link #clientAmountThis} and {@link #clientAmountLast} for rendering. */
        int clientAmountThis, clientAmountLast;

        /** Holds the amount of fluid was last sent to us from the sever */
        int target = 0;

        Vec3 offsetLast, offsetThis;

        Section(EnumPipePart part) {
            this.part = part;
        }

        void writeToNbt(CompoundTag nbt) {
            nbt.putShort("capacity", (short) amount);
            nbt.putShort("lastSentAmount", (short) lastSentAmount);
            nbt.putShort("ticksInDirection", (short) ticksInDirection);

            for (int i = 0; i < incoming.length; ++i) {
                nbt.putShort("in[" + i + "]", (short) incoming[i]);
            }
        }

        void readFromNbt(CompoundTag nbt) {
            this.amount = nbt.getShort("capacity");
            this.lastSentAmount = nbt.getShort("lastSentAmount");
            this.ticksInDirection = nbt.getShort("ticksInDirection");

            incomingTotalCache = 0;
            for (int i = 0; i < incoming.length; ++i) {
                incomingTotalCache += incoming[i] = nbt.getShort("in[" + i + "]");
            }
        }

        /** @return The maximum amount of fluid that can be inserted into this pipe on this tick. */
        int getMaxFilled() {
            int availableTotal = capacity - amount;
            int availableThisTick = fluidTransferInfo.transferPerTick - incoming[currentTime];
            return Math.min(availableTotal, availableThisTick);
        }

        /** @return The maximum amount of fluid that can be extracted out of this pipe this tick. */
        int getMaxDrained() {
            return Math.min(amount - incomingTotalCache, fluidTransferInfo.transferPerTick);
        }

        /** @return The fluid filled */
        int fill(int maxFill, boolean doFill) {
            int amountToFill = Math.min(getMaxFilled(), maxFill);
            if (amountToFill <= 0) {
                return 0;
            }
            if (doFill) {
                incoming[currentTime] += amountToFill;
                incomingTotalCache += amountToFill;
                amount += amountToFill;
            }
            return amountToFill;
        }

        public int fillInternal(int maxFill, boolean doFill) {
            int amountToFill = Math.min(capacity - amount, maxFill);
            if (amountToFill <= 0) {
                return 0;
            }
            if (doFill) {
                incoming[currentTime] += amountToFill;
                incomingTotalCache += amountToFill;
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
            incomingTotalCache -= incoming[currentTime];
            incoming[currentTime] = 0;
        }

        void setTime(int current) {
            currentTime = current;
        }

        Dir getCurrentDirection() {
            Dir dir = ticksInDirection == 0 ? Dir.NONE : ticksInDirection < 0 ? Dir.IN : Dir.OUT;
            return dir;
        }

        /** @return True if this still contains fluid, false if not. */
        boolean tickClient() {
            clientAmountLast = clientAmountThis;

            if (target != clientAmountThis) {
                int delta = target - clientAmountThis;
                long msgDelta = lastMessage - lastMessageMinus1;
                msgDelta = MathUtil.clamp((int) msgDelta, 1, 60);
                if (Math.abs(delta) < msgDelta) {
                    clientAmountThis += delta;
                } else {
                    clientAmountThis += delta / (int) msgDelta;
                }
            }

            if (offsetThis == null || (clientAmountThis == 0 && clientAmountLast == 0)) {
                offsetThis = Vec3.ZERO;
            }
            offsetLast = offsetThis;

            if (part.face == null) {
                Vec3 dir = Vec3.ZERO;
                // Firstly find all the outgoing faces
                for (EnumPipePart p : EnumPipePart.FACES) {
                    Section s = sections.get(p);
                    if (s.ticksInDirection > 0) {
                        dir = dir.add(Vec3.atLowerCornerOf(p.face.getNormal()));
                    }
                }
                // If that failed then find all of the incoming faces
                for (EnumPipePart p : EnumPipePart.FACES) {
                    Section s = sections.get(p);
                    if (s.ticksInDirection < 0) {
                        dir = dir.add(Vec3.atLowerCornerOf(p.face.getNormal()).scale(-1));
                    }
                }
                dir = new Vec3(Math.signum(dir.x), Math.signum(dir.y), Math.signum(dir.z));
                offsetThis = offsetThis.add(dir.scale(-FLOW_MULTIPLIER));
            } else {
                double mult = Math.signum(ticksInDirection);
                offsetThis = VecUtil.offset(offsetLast, part.face, -FLOW_MULTIPLIER * (mult));
            }

            double dx = offsetThis.x >= 0.5 ? -1 : offsetThis.x <= 0.5 ? 1 : 0;
            double dy = offsetThis.y >= 0.5 ? -1 : offsetThis.y <= 0.5 ? 1 : 0;
            double dz = offsetThis.z >= 0.5 ? -1 : offsetThis.z <= 0.5 ? 1 : 0;
            if (dx != 0 || dy != 0 || dz != 0) {
                offsetThis = offsetThis.add(dx, dy, dz);
                offsetLast = offsetLast.add(dx, dy, dz);
            }
            return clientAmountThis > 0 | clientAmountLast > 0;
        }

        // IFluidHandler

        @NotNull
        @Override
        @Deprecated
        public FluidStack drain(FluidStack resource, FluidAction doDrain) {
            return StackUtil.EMPTY_FLUID;
        }

        /** @deprecated USE {@link #drainInternal(int, boolean)} rather than this! */
        @NotNull
        @Override
        @Deprecated
        public FluidStack drain(int maxDrain, FluidAction doDrain) {
            return StackUtil.EMPTY_FLUID;
        }

        // 1.18.2: divided into 3 methods
//        @Override
//        public IFluidTankProperties[] getTankProperties() {
//            return new IFluidTankProperties[0];
//        }

        @Override
        public int getTanks() {
            return 0;
        }

        @NotNull
        @Override
        public FluidStack getFluidInTank(int tank) {
            return StackUtil.EMPTY_FLUID;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return false;
        }

        @Override
        public int fill(FluidStack resource, FluidAction doFill) {
            if (!getCurrentDirection().canInput() || !pipe.isConnected(part.face) || resource == null) {
                return 0;
            }
            resource = resource.copy();
            PipeEventFluid.TryInsert tryInsert = new PipeEventFluid.TryInsert(
                    pipe.getHolder(), PipeFlowFluids.this, part.face, resource
            );
            pipe.getHolder().fireEvent(tryInsert);
            if (tryInsert.isCanceled()) {
                return 0;
            }

            if (currentFluid == null || currentFluid.isFluidEqual(resource)) {
                if (doFill.execute()) {
                    if (currentFluid == null) {
                        setFluid(resource.copy());
                    }
                }
                int filled = fill(resource.getAmount(), doFill.execute());
                if (filled > 0 && doFill.execute()) {
                    ticksInDirection = COOLDOWN_INPUT;
                }
                return filled;
            }
            return 0;
        }
    }

    /** Enum used for the current direction that a fluid is flowing. */
    enum Dir {
        IN(-1),
        NONE(0),
        OUT(1);

        final byte nbtValue;

        private Dir(int nbtValue) {
            this.nbtValue = (byte) nbtValue;
        }

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

        public static Dir get(int dir) {
            if (dir == 0) {
                return Dir.NONE;
            } else if (dir < 0) {
                return IN;
            } else {
                return OUT;
            }
        }
    }
}
