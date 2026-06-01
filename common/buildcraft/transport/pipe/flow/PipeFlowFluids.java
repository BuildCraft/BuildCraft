/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 *
 * Ported to Fabric 1.20.1 by R.Chen (https://github.com/MantraChen).
 */

// FluidStack → FluidVariant (identity) + int millibuckets (per-section internal amount). The pipe's
// internal fluid mechanics (sectioning, delayed travel, centre/side movement, networking) are kept
// intact and run entirely on the migrated lib types. Fluid amounts crossing the IFlowFluid public
// boundary are droplets (1 bucket = 81000) and are converted to/from internal mB via BCFluidStorage.
//
// STUB(R.Chen): external-tile fluid I/O is deferred to Phase 4F (FluidStorage.SIDED / BlockApiLookup).
// Every edge that used to obtain a neighbouring tile's IFluidHandler through
// getCapabilityFromPipe(side, CapUtil.CAP_FLUIDS) — extracting fluid into the pipe (tryExtractFluid*),
// pushing fluid out (moveFromPipe) and routing fluid from the centre to externally-fed output sides
// (moveFromCentre) — is stubbed to a no-op until the Fabric neighbour lookup is wired. Pipe-internal
// flow (insertFluidsForce into the centre, side→centre movement, extractFluidsForce) is fully live.

package buildcraft.transport.pipe.flow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowFluid;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.FluidTransferInfo;
import buildcraft.api.transport.pipe.PipeEventFluid.OnMoveToCentre;
import buildcraft.api.transport.pipe.PipeEventFluid.PreMoveToCentre;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.compat.BCFluidStorage;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.net.PacketBufferBC;
import buildcraft.lib.net.cache.BuildCraftObjectCaches;

import buildcraft.transport.BCTransportStatements;
import buildcraft.lib.tile.TileBC_Neptune.NetSide;

public class PipeFlowFluids extends PipeFlow implements IFlowFluid, IDebuggable {

    private static final int DIRECTION_COOLDOWN = 60;
    private static final int COOLDOWN_INPUT = -DIRECTION_COOLDOWN;
    private static final int COOLDOWN_OUTPUT = DIRECTION_COOLDOWN;

    /** STUB(R.Chen): BCCoreConfig.networkUpdateRate inlined; wired back in Phase 6 (core config). */
    private static final int NETWORK_UPDATE_RATE = 10;

    /** One bucket, in millibuckets — replaces Forge's {@code Fluid.BUCKET_VOLUME}. */
    private static final int BUCKET_VOLUME = 1000;

    public static final int NET_FLUID_AMOUNTS = 2;

    /** The number of pixels the fluid moves by per millisecond */
    public static final double FLOW_MULTIPLIER = 0.016;

    private final FluidTransferInfo fluidTransferInfo = PipeApi.getFluidTransferInfo(pipe.getDefinition());

    /* Default to an additional second of fluid inserting and removal. This means that (for a normal pipe like cobble)
     * it will be 20 * (10 + 12) = 20 * 22 = 440 - oh that's not good is it */
    public final int capacity = Math.max(BUCKET_VOLUME, fluidTransferInfo.transferPerTick * (10));// TEMP!

    private final Map<EnumPipePart, Section> sections = new EnumMap<>(EnumPipePart.class);
    /** The fluid currently held by this pipe, or null when empty. Amounts live per-section in mB. */
    private FluidVariant currentFluid;
    private int currentDelay;
    private final SafeTimeTracker tracker = new SafeTimeTracker(NETWORK_UPDATE_RATE, 4);

    // Client fields for interpolating amounts
    private long lastMessage, lastMessageMinus1;
    private Supplier<FluidVariant> clientFluid = null;

    public PipeFlowFluids(IPipe pipe) {
        super(pipe);
        for (EnumPipePart part : EnumPipePart.VALUES) {
            sections.put(part, new Section(part));
        }
    }

    public PipeFlowFluids(IPipe pipe, NbtCompound nbt) {
        super(pipe, nbt);
        for (EnumPipePart part : EnumPipePart.VALUES) {
            sections.put(part, new Section(part));
        }
        if (nbt.contains("fluid")) {
            FluidVariant fluid = FluidVariant.fromNbt(nbt.getCompound("fluid"));
            setFluid(fluid.isBlank() ? null : fluid);
        } else {
            setFluid(null);
        }

        for (EnumPipePart part : EnumPipePart.VALUES) {
            int direction = part.getIndex();
            String key = "tank[" + direction + "]";
            if (nbt.contains(key)) {
                sections.get(part).readFromNbt(nbt.getCompound(key));
            }
        }
    }

    @Override
    public NbtCompound writeToNbt() {
        NbtCompound nbt = super.writeToNbt();

        if (currentFluid != null) {
            nbt.put("fluid", currentFluid.toNbt());

            for (EnumPipePart part : EnumPipePart.VALUES) {
                int direction = part.getIndex();
                NbtCompound subTag = new NbtCompound();
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
        // STUB(R.Chen): neighbour fluid-storage lookup deferred to Phase 4F (FluidStorage.SIDED).
        // The original queried CAP_FLUIDS on the neighbour tile; with no Fabric equivalent wired yet,
        // external-tile connections are disabled. Pipe→pipe connections still work above.
        return false;
    }

    @Override
    public <T> T getCapability(@Nonnull Object capability, Direction facing) {
        // STUB(R.Chen): FluidStorage.SIDED exposure deferred to Phase 4F.
        // The original routed CAP_FLUIDS → the Section for the queried face via Capability#cast, which
        // depends on the Forge capability system. Same deferral as PipeFlow/Pipe/PipeFlowItems.
        return super.getCapability(capability, facing);
    }

    @Override
    public void addDrops(List<ItemStack> toDrop, int fortune) {
        super.addDrops(toDrop, fortune);
        // STUB(R.Chen): BCCoreItems.fragileFluidShard drop deferred to Phase 6 (core item migration).
        // The original dropped a fragile-fluid-shard item carrying this pipe's remaining fluid contents.
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
    public long tryExtractFluid(long droplets, Direction from, Object filter, boolean simulate) {
        return tryExtractFluidInternal(droplets, from, simulate);
    }

    @Override
    public long tryExtractFluidAdv(long droplets, Direction from, Object filter, boolean simulate) {
        // STUB(R.Chen): the advanced (IFluidFilter / IFluidHandlerAdv) extraction path additionally
        // inspects the neighbour's per-tank contents and filter matches; restore the filter handling
        // once the FluidStorage.SIDED neighbour lookup is wired in Phase 4F.
        return tryExtractFluidInternal(droplets, from, simulate);
    }

    private long tryExtractFluidInternal(long maxDroplets, Direction from, boolean simulate) {
        if (from == null || maxDroplets <= 0) {
            return 0;
        }
        // STUB(R.Chen): neighbour fluid handler lookup deferred to Phase 4F (FluidStorage.SIDED).
        // The original obtained the neighbour's IFluidHandler via getCapabilityFromPipe(from, CAP_FLUIDS)
        // and drained it into this pipe's sections (filling the side section then the centre). Until the
        // Fabric lookup replaces the Forge capability, no external fluid can be pulled in.
        return 0;
    }

    @Override
    public long insertFluidsForce(Object fluid, long amount, @Nullable Direction from, boolean simulate) {
        if (!(fluid instanceof FluidVariant)) {
            return 0;
        }
        FluidVariant variant = (FluidVariant) fluid;
        if (variant.isBlank() || amount <= 0) {
            return 0;
        }
        if (currentFluid != null && !currentFluid.equals(variant)) {
            return 0;
        }
        Section s = sections.get(EnumPipePart.CENTER);
        if (currentFluid == null && !simulate) {
            setFluid(variant);
        }
        int maxMb = (int) Math.min(Integer.MAX_VALUE, BCFluidStorage.dropletsToMB(amount));
        int filled = s.fill(maxMb, !simulate);
        if (filled == 0) {
            return 0;
        }
        if (!simulate && from != null) {
            sections.get(EnumPipePart.fromFacing(from)).ticksInDirection = COOLDOWN_INPUT;
        }
        return BCFluidStorage.mBToDroplets(filled);
    }

    @Override
    @Nullable
    public Object extractFluidsForce(long min, long max, @Nullable Direction section, boolean simulate) {
        long minMb = BCFluidStorage.dropletsToMB(min);
        long maxMb = BCFluidStorage.dropletsToMB(max);
        if (minMb > maxMb) {
            throw new IllegalArgumentException("Minimum (" + minMb + ") > maximum (" + maxMb + ")");
        }
        if (maxMb < 0 || currentFluid == null) {
            return null;
        }
        Section s = sections.get(EnumPipePart.fromFacing(section));
        if (s.amount < minMb) {
            return null;
        }
        // STUB(R.Chen): callers will receive a (FluidVariant, amount) pair in Phase 4E; for now only the
        // fluid identity is returned and the drained amount is applied to the section directly.
        int amount = MathUtil.clamp(s.amount, (int) minMb, (int) maxMb);
        FluidVariant extracted = currentFluid;
        if (!simulate) {
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
        return extracted;
    }

    // IDebuggable

    @Override
    public void getDebugInfo(List<String> left, List<String> right, Direction side) {
        boolean isRemote = pipe.getHolder().getPipeWorld().isClient;

        FluidVariant fluid = isRemote ? getFluidStackForRender() : currentFluid;
        left.add(" - FluidType = " + (fluid == null || fluid.isBlank() ? "empty" : fluid.toString()));

        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section section = sections.get(part);
            if (section == null) {
                continue;
            }
            String faceName = part.face == null ? "center" : part.face.asString();
            StringBuilder line = new StringBuilder(" - " + faceName + " = ");
            int amount = isRemote ? section.target : section.amount;
            line.append(amount > 0 ? Formatting.GREEN : "");
            line.append(amount).append("").append(Formatting.RESET).append("mB");
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

            left.add(line.toString());
        }
    }

    // Rendering

    @Environment(EnvType.CLIENT)
    public FluidVariant getFluidStackForRender() {
        return clientFluid == null ? null : clientFluid.get();
    }

    @Environment(EnvType.CLIENT)
    public double[] getAmountsForRender(float partialTicks) {
        double[] arr = new double[7];
        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section s = sections.get(part);
            arr[part.getIndex()] = s.clientAmountLast * (1 - partialTicks) + s.clientAmountThis * (partialTicks);
        }
        return arr;
    }

    @Environment(EnvType.CLIENT)
    public Vec3d[] getOffsetsForRender(float partialTicks) {
        Vec3d[] arr = new Vec3d[7];
        for (EnumPipePart part : EnumPipePart.VALUES) {
            Section s = sections.get(part);
            if (s.offsetLast != null & s.offsetThis != null) {
                arr[part.getIndex()] = s.offsetLast.multiply(1 - partialTicks).add(s.offsetThis.multiply(partialTicks));
            }
        }
        return arr;
    }

    // Internal logic

    private void setFluid(FluidVariant fluid) {
        currentFluid = fluid;
        currentDelay = (int) PipeApi.getFluidTransferInfo(pipe.getDefinition()).transferDelayMultiplier;
        for (Section section : sections.values()) {
            section.incoming = new int[currentDelay];
            section.currentTime = 0;
            section.ticksInDirection = 0;
        }
    }

    @Override
    public void onTick() {
        World world = pipe.getHolder().getPipeWorld();
        if (world.isClient) {
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
        // STUB(R.Chen): pushing fluid out to neighbouring tiles is deferred to Phase 4F (FluidStorage.SIDED).
        // The original, for each output-facing side whose SideCheck resolved to a single allowed face,
        // obtained the neighbour's IFluidHandler and filled it from this section, draining the section by
        // the accepted amount and setting an OUTPUT cooldown. Restore once the neighbour lookup is wired.
    }

    private void moveFromCenter() {
        // STUB(R.Chen): moving fluid from the centre to output sides is gated on a neighbouring tile that
        // accepts fluid on that side; that lookup (getCapabilityFromPipe / FluidStorage.SIDED) is deferred
        // to Phase 4F. With no external sink reachable, no centre→side movement occurs yet.
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
    public void writePayload(int id, PacketByteBuf buf, EnvType side) {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(buf);
        if (side == EnvType.SERVER) {
            if (id == NET_FLUID_AMOUNTS || id == NET_ID_FULL_STATE) {
                boolean full = id == NET_ID_FULL_STATE;
                if (currentFluid == null) {
                    buffer.writeBoolean(false);
                } else {
                    buffer.writeBoolean(true);
                    buffer.writeInt(BuildCraftObjectCaches.storeFluid(currentFluid));
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
                    buffer.writeEnumValue(should); // This writes out 2 bits so don't bother with a boolean flag
                    section.lastSentDirection = should;
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketByteBuf buf, EnvType side) throws IOException {
        PacketBufferBC buffer = PacketBufferBC.asPacketBufferBc(buf);
        if (side == EnvType.CLIENT) {
            if (id == NET_FLUID_AMOUNTS || id == NET_ID_FULL_STATE) {
                boolean full = id == NET_ID_FULL_STATE;
                if (buffer.readBoolean()) {
                    int fluidId = buffer.readInt();
                    clientFluid = BuildCraftObjectCaches.retrieveFluid(fluidId);
                }
                for (EnumPipePart part : EnumPipePart.VALUES) {
                    Section section = sections.get(part);
                    if (full || buffer.readBoolean()) {
                        section.target = buffer.readShort();
                        if (full) {
                            section.clientAmountLast = section.clientAmountThis = section.target;
                        }
                    }

                    Dir dir = buffer.readEnumValue(Dir.class);
                    section.ticksInDirection = dir == Dir.NONE ? 0 : dir == Dir.IN ? COOLDOWN_INPUT : COOLDOWN_OUTPUT;
                }
                lastMessageMinus1 = lastMessage;
                lastMessage = pipe.getHolder().getPipeWorld().getTime();
            }
        }
    }

    /** Holds data about a single section of this pipe. */
    class Section {
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

        Vec3d offsetLast, offsetThis;

        Section(EnumPipePart part) {
            this.part = part;
        }

        void writeToNbt(NbtCompound nbt) {
            nbt.putShort("capacity", (short) amount);
            nbt.putShort("lastSentAmount", (short) lastSentAmount);
            nbt.putShort("ticksInDirection", (short) ticksInDirection);

            for (int i = 0; i < incoming.length; ++i) {
                nbt.putShort("in[" + i + "]", (short) incoming[i]);
            }
        }

        void readFromNbt(NbtCompound nbt) {
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
                offsetThis = Vec3d.ZERO;
            }
            offsetLast = offsetThis;

            if (part.face == null) {
                Vec3d dir = Vec3d.ZERO;
                // Firstly find all the outgoing faces
                for (EnumPipePart p : EnumPipePart.FACES) {
                    Section s = sections.get(p);
                    if (s.ticksInDirection > 0) {
                        dir = dir.add(Vec3d.of(p.face.getVector()));
                    }
                }
                // If that failed then find all of the incoming faces
                for (EnumPipePart p : EnumPipePart.FACES) {
                    Section s = sections.get(p);
                    if (s.ticksInDirection < 0) {
                        dir = dir.add(Vec3d.of(p.face.getVector()).multiply(-1));
                    }
                }
                dir = new Vec3d(Math.signum(dir.x), Math.signum(dir.y), Math.signum(dir.z));
                offsetThis = offsetThis.add(dir.multiply(-FLOW_MULTIPLIER));
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
