/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.api.transport.pipe.PipeApi.RedstoneFluxTransferInfo;
import buildcraft.lib.misc.VecUtil;
import buildcraft.lib.misc.data.AverageInt;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.network.NetworkDirection;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.ToIntFunction;

public class PipeFlowRedstoneFlux extends PipeFlow implements IFlowRedstoneFlux, IDebuggable {
    private static final int DEFAULT_MAX_POWER = 100;
    public static final int NET_POWER_AMOUNTS = 2;

    public Vec3 clientDisplayFlowCentre = VecUtil.VEC_HALF;
    public Vec3 clientDisplayFlowCentreLast = VecUtil.VEC_HALF;
    public long clientLastDisplayTime = 0;

    private int maxPower = -1;
    private boolean disabled = false;

    private long currentWorldTime;

    private boolean isReceiver = false;
    private final EnumMap<Direction, Section> sections;

    public PipeFlowRedstoneFlux(IPipe pipe) {
        super(pipe);
        sections = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            sections.put(face, new Section(face));
        }
    }

    public PipeFlowRedstoneFlux(IPipe pipe, CompoundTag nbt) {
        super(pipe, nbt);
        isReceiver = nbt.getBoolean("isReceiver");
        sections = new EnumMap<>(Direction.class);
        for (Direction face : Direction.values()) {
            sections.put(face, new Section(face));
        }
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.putBoolean("isReceiver", isReceiver);
        return nbt;
    }

    @Override
    public void writePayload(int id, FriendlyByteBuf buffer, Dist side) {
        super.writePayload(id, buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
                for (Direction face : Direction.values()) {
                    Section s = sections.get(face);
                    buffer.writeInt(s.displayPower);
                    buffer.writeEnum(s.displayFlow);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, FriendlyByteBuf buffer, NetworkDirection side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
                for (Direction face : Direction.values()) {
                    Section s = sections.get(face);
                    s.displayPower = buffer.readInt();
                    s.displayFlow = buffer.readEnum(EnumFlow.class);
                }
            }
        }
    }

    @Override
    public boolean canConnect(Direction face, PipeFlow other) {
        return other instanceof PipeFlowRedstoneFlux;
    }

    @Override
    public boolean canConnect(Direction face, BlockEntity oTile) {
        return oTile.getCapability(CapabilityEnergy.ENERGY, face.getOpposite()).isPresent();
    }

    @Override
    public void reconfigure() {
        PipeEventRedstoneFlux.Configure configure = new PipeEventRedstoneFlux.Configure(pipe.getHolder(), this);
        RedstoneFluxTransferInfo pti = PipeApi.getRfTransferInfo(pipe.getDefinition());
        configure.setReceiver(pti.isReceiver);
        configure.setMaxPower(pti.transferPerTick);
        pipe.getHolder().fireEvent(configure);
        isReceiver = configure.isReceiver();
        maxPower = configure.getMaxPower();
        disabled = configure.isTransferDisabled();
        if (maxPower <= 0) {
            maxPower = DEFAULT_MAX_POWER;
        }
    }

    @Override
    public int tryExtractPower(int maxExtracted, Direction from) {
        if (!isReceiver || disabled) {
            return 0;
        }
        BlockEntity tile = pipe.getConnectedTile(from);
        if (tile == null) {
            return 0;
        }
        IEnergyStorage storage = tile.getCapability(CapabilityEnergy.ENERGY, from.getOpposite()).orElse(null);
        if (storage == null) {
            return 0;
        }

        // TODO!
        return 0;
    }

    @Override
    public boolean onFlowActivate(Player player, HitResult trace, float hitX, float hitY, float hitZ,
            EnumPipePart part) {
        return super.onFlowActivate(player, trace, hitX, hitY, hitZ, part);
    }

    public Section getSection(Direction side) {
        return sections.get(side);
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
        if (facing == null) {
            return LazyOptional.empty();
        } else if (capability == CapabilityEnergy.ENERGY) {
            return isReceiver ? LazyOptional.of(() -> sections.get(facing)).cast() : LazyOptional.empty();
        } else {
            return LazyOptional.empty();
        }
    }

    @Override
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
        left.add(new TextComponent("maxPower = " + maxPower));
        left.add(new TextComponent("isReceiver = " + isReceiver));
        left.add(new TextComponent(
                "internalPower = " + arrayToString(s -> s.internalPower) + " <- " + arrayToString(s -> s.internalNextPower)
        ));
        left.add(new TextComponent("- powerQuery: " + arrayToString(s -> s.powerQuery) + " <- " + arrayToString(s -> s.nextPowerQuery)));
        left.add(new TextComponent(
                "- power: IN " + arrayToString(s -> s.debugPowerInput) + ", OUT " + arrayToString(s -> s.debugPowerOutput)
        ));
        left.add(new TextComponent("- power: OFFERED " + arrayToString(s -> s.debugPowerOffered)));
    }

    private String arrayToString(ToIntFunction<Section> getter) {
        long[] arr = new long[6];
        for (Direction face : Direction.values()) {
            arr[face.ordinal()] = getter.applyAsInt(sections.get(face));
        }
        return Arrays.toString(arr);
    }

    @Override
    public void onTick() {
        if (maxPower == -1) {
            reconfigure();
        }
        if (pipe.getHolder().getPipeWorld().isClientSide) {
            clientDisplayFlowCentreLast = clientDisplayFlowCentre;
            for (Direction face : Direction.values()) {
                Section s = sections.get(face);
                s.clientDisplayFlowLast = s.clientDisplayFlow;
                // double diff = s.displayFlow.value * 2.4 * face.getAxisDirection().getOffset();
                double diff = s.displayFlow.value * 2.4 * face.getAxisDirection().getStep();
                s.clientDisplayFlow += 16 + diff;
                s.clientDisplayFlow %= 16;

                double cVal = VecUtil.getValue(clientDisplayFlowCentre, face.getAxis());
                cVal += 16 + diff / 2;
                cVal %= 16;
                clientDisplayFlowCentre = VecUtil.replaceValue(clientDisplayFlowCentre, face.getAxis(), cVal);
            }
            return;
        }

        EnumFlow[] lastFlows = new EnumFlow[6];
        int[] lastDisplayPower = new int[6];

        for (Direction face : Direction.values()) {
            Section s = sections.get(face);
            int i = face.ordinal();
            lastFlows[i] = s.displayFlow;
            lastDisplayPower[i] = s.displayPower;
        }

        step();

        init();

        for (Direction face : Direction.values()) {
            Section s = sections.get(face);
            if (s.internalPower > 0) {
                int totalPowerQuery = 0;
                for (Direction face2 : Direction.values()) {
                    if (face != face2) {
                        totalPowerQuery += sections.get(face2).powerQuery;
                    }
                }

                boolean returnPower = false;
                if (totalPowerQuery <= 0 && s.powerQuery > 0) {
                    totalPowerQuery = s.powerQuery;
                    returnPower = true;
                }

                if (totalPowerQuery > 0) {
                    int unusedPowerQuery = totalPowerQuery;
                    for (Direction face2 : Direction.values()) {
                        if (face == face2 && !returnPower) {
                            continue;
                        }
                        Section s2 = sections.get(face2);
                        if (s2.powerQuery > 0) {
                            int watts = (int) Math.min(s.internalPower * (long) s2.powerQuery / unusedPowerQuery, s.internalPower);
                            unusedPowerQuery -= s2.powerQuery;
                            IPipe neighbour = pipe.getConnectedPipe(face2);
                            int leftover = watts;
                            if (
                                    neighbour != null && neighbour.getFlow() instanceof PipeFlowRedstoneFlux && neighbour
                                            .isConnected(face2.getOpposite())
                            ) {
                                PipeFlowRedstoneFlux oFlow = (PipeFlowRedstoneFlux) neighbour.getFlow();
                                leftover = oFlow.sections.get(face2.getOpposite()).receivePowerInternal(watts);
                            } else {
                                IEnergyStorage receiver = pipe.getHolder().getCapabilityFromPipe(
                                        face2, CapabilityEnergy.ENERGY
                                );
                                if (receiver != null && receiver.canReceive()) {
                                    int accepted = receiver.receiveEnergy(watts, false);
                                    leftover = watts - accepted;
                                }
                            }
                            int used = watts - leftover;
                            s.internalPower -= used;
                            s2.debugPowerOutput += used;

                            s.powerAverage.push(used);
                            s2.powerAverage.push(used);

                            s.displayFlow = EnumFlow.OUT;
                            s2.displayFlow = EnumFlow.IN;
                        }
                    }
                }
            }
        }
        // Render compute goes here
        for (Section s : sections.values()) {
            s.powerAverage.tick();
            double value = s.powerAverage.getAverage() / maxPower;
            value = Math.sqrt(value);
            s.displayPower = (int) (value * MjAPI.MJ);
        }

        // Compute the tiles requesting power that are not power pipes
        for (Direction face : Direction.values()) {
            if (pipe.getConnectedType(face) != ConnectedType.TILE) {
                continue;
            }
            IEnergyStorage recv = pipe.getHolder().getCapabilityFromPipe(face, CapabilityEnergy.ENERGY);
            if (recv != null && recv.canReceive()) {
                int requested = recv.getMaxEnergyStored() - recv.getEnergyStored();
                if (requested > 0) {
                    requestPower(face, requested);
                }
            }
        }

        // Sum the amount of power requested on each side
        int[] transferQueryTemp = new int[6];
        for (Direction face : Direction.values()) {
            if (!pipe.isConnected(face)) {
                continue;
            }
            int query = 0;
            for (Direction face2 : Direction.values()) {
                if (face != face2) {
                    query += sections.get(face2).powerQuery;
                }
            }
            transferQueryTemp[face.ordinal()] = query;
        }

        // Transfer requested power to neighbouring pipes
        for (Direction face : Direction.values()) {
            if (disabled) {
                continue;
            }
            if (transferQueryTemp[face.ordinal()] <= 0 || !pipe.isConnected(face)) {
                continue;
            }
            IPipe oPipe = pipe.getHolder().getNeighbourPipe(face);
            if (oPipe == null || !(oPipe.getFlow() instanceof PipeFlowRedstoneFlux)) {
                continue;
            }
            PipeFlowRedstoneFlux oFlow = (PipeFlowRedstoneFlux) oPipe.getFlow();
            oFlow.requestPower(face.getOpposite(), transferQueryTemp[face.ordinal()]);
        }
        // Networking
        boolean didChange = false;
        for (Direction face : Direction.values()) {
            Section s = sections.get(face);
            int i = face.ordinal();
            if (lastFlows[i] != s.displayFlow || lastDisplayPower[i] != s.displayPower) {
                didChange = true;
                break;
            }
        }

        // if (tracker.markTimeIfDelay(pipe.getHolder().getPipeWorld())) {
        if (didChange) {
            sendPayload(NET_POWER_AMOUNTS);
        }
        // }
    }

    private void step() {
        // long now = pipe.getHolder().getPipeWorld().getTotalWorldTime();
        long now = pipe.getHolder().getPipeWorld().getGameTime();
        if (currentWorldTime != now) {
            currentWorldTime = now;
            sections.values().forEach(Section::step);
        }
    }

    private void init() {
        // TODO: use this for initialising the tile cache
    }

    private void requestPower(Direction from, int amount) {
        step();

        Section s = sections.get(from);
        if (pipe.getBehaviour() instanceof IPipeTransportRfHook) {
            s.nextPowerQuery += ((IPipeTransportRfHook) pipe.getBehaviour()).requestPower(from, amount);
        } else {
            s.nextPowerQuery += amount;
        }
        s.nextPowerQuery = Math.min(s.nextPowerQuery, maxPower);
    }

    public int getPowerRequested(@Nullable Direction side) {
        int req = 0;
        for (Direction face : Direction.values()) {
            if (side == null || face != side) {
                req += sections.get(face).powerQuery;
            }
        }
        return req;
    }

    public double getMaxTransferForRender(float partialTicks) {
        if (true) return maxPower / (double) MjAPI.MJ;
        double max = 0;
        for (Section s : sections.values()) {
            double value = s.displayPower / (double) MjAPI.MJ;
            // value = MathUtil.interp(partialTicks, value, value);
            max = Math.max(max, value);
        }
        return max;
    }

    public class Section implements IEnergyStorage {
        public final Direction side;

        public final AverageInt clientDisplayAverage = new AverageInt(10);
        public double clientDisplayFlow, clientDisplayFlowLast;

        /** Range: 0 to {@link MjAPI#MJ} */
        public int displayPower;
        public EnumFlow displayFlow = EnumFlow.STATIONARY;
        public int nextPowerQuery;
        public int internalNextPower;
        public final AverageInt powerAverage = new AverageInt(1);

        int powerQuery;
        int internalPower;

        /** Debugging fields */
        int debugPowerInput, debugPowerOutput, debugPowerOffered;

        public Section(Direction side) {
            this.side = side;
            clientDisplayFlow = (side.getAxisDirection() == AxisDirection.POSITIVE ? 7 : 1) / 8.0;
        }

        void step() {
            powerQuery = nextPowerQuery;
            nextPowerQuery = 0;

            internalPower += internalNextPower;
            internalNextPower = 0;
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return isReceiver;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (isReceiver) {
                if (!simulate) {
                    return maxReceive - this.receivePowerInternal(maxReceive);
                }
                return maxReceive;
            }
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }

        @Override
        public int getEnergyStored() {
            return internalPower + internalNextPower;
        }

        @Override
        public int getMaxEnergyStored() {
            return maxPower;
        }

        int receivePowerInternal(int sent) {
            if (sent > 0) {
                PipeFlowRedstoneFlux.this.step();
                int max = maxPower - internalNextPower;
                if (max < 0) {
                    return sent;
                }
                int accepted = Math.min(max, sent);
                debugPowerOffered += accepted;
                internalNextPower += accepted;
                return sent - accepted;
            }
            return sent;
        }
    }

    public enum EnumFlow {
        IN(-1),
        OUT(1),
        STATIONARY(0);

        public final int value;

        private EnumFlow(int value) {
            this.value = value;
        }
    }
}
