/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.transport.pipe.flow;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.function.ToLongFunction;
import javax.annotation.Nonnull;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.SafeTimeTracker;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IFlowPower;
import buildcraft.api.transport.pipe.IPipe;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.api.transport.pipe.PipeApi;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;
import buildcraft.api.transport.pipe.PipeEventPower;
import buildcraft.api.transport.pipe.PipeFlow;

import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.data.AverageInt;

import buildcraft.core.BCCoreConfig;

public class PipeFlowPower extends PipeFlow implements IFlowPower, IDebuggable {
    private static final long DEFAULT_MAX_POWER = MjAPI.MJ * 10;
    public static final int NET_POWER_AMOUNTS = 2;

    private long maxPower = -1;
    private long powerLoss = -1;
    private long powerResistance = -1;

    private long currentWorldTime;

    private boolean isReceiver = false;
    private final EnumMap<EnumFacing, Section> sections;

    private final SafeTimeTracker tracker = new SafeTimeTracker(BCCoreConfig.networkUpdateRate);
    private long[] transferQuery;

    public PipeFlowPower(IPipe pipe) {
        super(pipe);
        sections = new EnumMap<>(EnumFacing.class);
        for (EnumFacing face : EnumFacing.VALUES) {
            sections.put(face, new Section(face));
        }
    }

    public PipeFlowPower(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        sections = new EnumMap<>(EnumFacing.class);
        for (EnumFacing face : EnumFacing.VALUES) {
            sections.put(face, new Section(face));
        }
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();

        return nbt;
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
        if (side == Side.SERVER) {
            if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    buffer.writeInt(sections.get(face).displayPower);
                }
            }
        }
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
        if (side == Side.CLIENT) {
            if (id == NET_POWER_AMOUNTS || id == NET_ID_FULL_STATE) {
                for (EnumFacing face : EnumFacing.VALUES) {
                    sections.get(face).displayPower = buffer.readInt();
                }
            }
        }
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowPower;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        if (isReceiver) {
            IMjPassiveProvider provider = oTile.getCapability(MjAPI.CAP_PASSIVE_PROVIDER, face.getOpposite());
            if (provider != null) {
                return true;
            }
        }
        IMjConnector receiver = oTile.getCapability(MjAPI.CAP_CONNECTOR, face.getOpposite());
        return receiver != null && receiver.canConnect(sections.get(face));
    }

    @Override
    public void reconfigure() {
        PipeEventPower.Configure configure = new PipeEventPower.Configure(pipe.getHolder(), this);
        PowerTransferInfo pti = PipeApi.getPowerTransferInfo(pipe.getDefinition());
        configure.setReceiver(pti.isReceiver);
        configure.setMaxPower(pti.transferPerTick);
        configure.setPowerLoss(pti.lossPerTick);
        configure.setPowerResistance(pti.resistancePerTick);
        pipe.getHolder().fireEvent(configure);
        isReceiver = configure.isReceiver();
        maxPower = configure.getMaxPower();
        if (maxPower <= 0) {
            maxPower = DEFAULT_MAX_POWER;
        }
        powerLoss = MathUtil.clamp(configure.getPowerLoss(), -1, maxPower);
        powerResistance = MathUtil.clamp(configure.getPowerResistance(), -1, MjAPI.MJ);

        if (powerLoss < 0) {
            if (powerResistance < 0) {
                // 1% resistance
                powerResistance = MjAPI.MJ / 100;
            }
            powerLoss = maxPower * powerResistance / MjAPI.MJ;
        } else if (powerResistance < 0) {
            powerResistance = powerLoss * MjAPI.MJ / maxPower;
        }
    }

    @Override
    public long tryExtractPower(long maxExtracted, EnumFacing from) {
        if (!isReceiver) {
            return 0;
        }
        TileEntity tile = pipe.getConnectedTile(from);
        if (tile == null) {
            return 0;
        }
        IMjPassiveProvider receiver = tile.getCapability(MjAPI.CAP_PASSIVE_PROVIDER, from.getOpposite());
        if (receiver == null) {
            return 0;
        }

        // TODO!
        return 0;
    }

    @Override
    public boolean onFlowActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ,
                                  EnumPipePart part) {
        return super.onFlowActivate(player, trace, hitX, hitY, hitZ, part);
    }

    @Override
    public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
        if (facing == null) {
            return null;
        } else if (capability == MjAPI.CAP_RECEIVER) {
            return isReceiver ? MjAPI.CAP_RECEIVER.cast(sections.get(facing)) : null;
        } else if (capability == MjAPI.CAP_CONNECTOR) {
            return MjAPI.CAP_CONNECTOR.cast(sections.get(facing));
        } else {
            return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("maxPower = " + LocaleUtil.localizeMj(maxPower));
        left.add("isReceiver = " + isReceiver);
        left.add("internalPower = " + arrayToString(s -> s.internalPower) + " <- "
            + arrayToString(s -> s.internalNextPower));
        left.add("- powerQuery: " + arrayToString(s -> s.powerQuery) + " <- " + arrayToString(s -> s.nextPowerQuery));
        left.add(
            "- power: IN " + arrayToString(s -> s.debugPowerInput) + ", OUT " + arrayToString(s -> s.debugPowerOutput));
        left.add("- power: OFFERED " + arrayToString(s -> s.debugPowerOffered));
    }

    private String arrayToString(ToLongFunction<Section> getter) {
        long[] arr = new long[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            arr[face.ordinal()] = getter.applyAsLong(sections.get(face)) / MjAPI.MJ;
        }
        return Arrays.toString(arr);
    }

    @Override
    public void onTick() {
        if (pipe.getHolder().getPipeWorld().isRemote) {
            return;
        }
        if (maxPower == -1) {
            reconfigure();
        }

        step();

        init();

        for (EnumFacing face : EnumFacing.VALUES) {
            Section s = sections.get(face);
            if (s.internalPower > 0) {
                long totalPowerQuery = 0;
                for (EnumFacing face2 : EnumFacing.VALUES) {
                    if (face != face2) {
                        totalPowerQuery += sections.get(face2).powerQuery;
                    }
                }

                if (totalPowerQuery > 0) {
                    long unusedPowerQuery = totalPowerQuery;
                    for (EnumFacing face2 : EnumFacing.VALUES) {
                        if (face == face2) {
                            continue;
                        }
                        Section s2 = sections.get(face2);
                        if (s2.powerQuery > 0) {
                            long watts = Math.min(s.internalPower * s2.powerQuery / unusedPowerQuery, s.internalPower);
                            unusedPowerQuery -= s2.powerQuery;
                            IPipe neighbour = pipe.getConnectedPipe(face2);
                            long leftover = watts;
                            if (neighbour != null && neighbour.getFlow() instanceof PipeFlowPower
                                && neighbour.isConnected(face2.getOpposite())) {
                                PipeFlowPower oFlow = (PipeFlowPower) neighbour.getFlow();
                                leftover = oFlow.sections.get(face2.getOpposite()).receivePowerInternal(watts);
                            } else {
                                IMjReceiver receiver =
                                    pipe.getHolder().getCapabilityFromPipe(face2, MjAPI.CAP_RECEIVER);
                                if (receiver != null && receiver.canReceive()) {
                                    leftover = receiver.receivePower(watts, false);
                                }
                            }
                            long used = watts - leftover;
                            s.internalPower -= used;
                            s2.debugPowerOutput += used;

                            s.powerAverage.push((int) used);
                            s2.powerAverage.push((int) used);
                        }
                    }
                }
            }
        }
        // Render compute goes here
        for (Section s : sections.values()) {
            s.powerAverage.tick();
            long value = (long) s.powerAverage.getAverage();
            long temp = Math.min(value * MjAPI.MJ / maxPower, 1 * MjAPI.MJ);
            s.displayPower = (int) (temp);
        }

        // Compute the tiles requesting power that are not power pipes
        for (EnumFacing face : EnumFacing.VALUES) {
            if (pipe.getConnectedType(face) != ConnectedType.TILE) {
                continue;
            }
            IMjReceiver recv = pipe.getHolder().getCapabilityFromPipe(face, MjAPI.CAP_RECEIVER);
            if (recv != null && recv.canReceive()) {
                long requested = recv.getPowerRequested();
                if (requested > 0) {
                    requestPower(face, requested);
                }
            }
        }

        // Sum the amount of power requested on each side
        long[] transferQueryTemp = new long[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            if (!pipe.isConnected(face)) {
                continue;
            }
            long query = 0;
            for (EnumFacing face2 : EnumFacing.VALUES) {
                if (face != face2) {
                    query += sections.get(face2).powerQuery;
                }
            }
            transferQueryTemp[face.ordinal()] = query;
        }

        // Transfer requested power to neighbouring pipes
        for (EnumFacing face : EnumFacing.VALUES) {
            if (transferQueryTemp[face.ordinal()] <= 0 || !pipe.isConnected(face)) {
                continue;
            }
            IPipe oPipe = pipe.getHolder().getNeighbourPipe(face);
            if (oPipe == null || !(oPipe.getFlow() instanceof PipeFlowPower)) {
                continue;
            }
            PipeFlowPower oFlow = (PipeFlowPower) oPipe.getFlow();
            oFlow.requestPower(face.getOpposite(), transferQueryTemp[face.ordinal()]);
        }
        // Networking

//        if (tracker.markTimeIfDelay(pipe.getHolder().getPipeWorld())) {
        if (!Arrays.equals(transferQuery, transferQueryTemp)) {
            sendPayload(NET_POWER_AMOUNTS);
        }

        transferQuery = transferQueryTemp;
//        }
    }

    private void step() {
        long now = pipe.getHolder().getPipeWorld().getTotalWorldTime();
        if (currentWorldTime != now) {
            currentWorldTime = now;
            sections.values().forEach(Section::step);
        }
    }

    private void init() {
        // TODO: use this for initialising the tile cache
    }

    private void requestPower(EnumFacing from, long amount) {
        step();

        Section s = sections.get(from);
        if (pipe.getBehaviour() instanceof IPipeTransportPowerHook) {
            s.nextPowerQuery += ((IPipeTransportPowerHook) pipe.getBehaviour()).requestPower(from, amount);
        } else {
            s.nextPowerQuery += amount;
        }
    }

    public double getMaxTransferForRender(float partialTicks) {
        double max = 0;
        for (Section s : sections.values()) {
            double value = s.displayPower / (double) MjAPI.MJ;
//            value = MathUtil.interp(partialTicks, value, value);
            max = Math.max(max, value);
        }
        return max;
    }

    public class Section implements IMjReceiver {
        public final EnumFacing side;

        public final AverageInt clientDisplayAverage = new AverageInt(10);
        public double clientDisplayFlow;

        /** Range: 0 to {@link MjAPI#MJ} */
        public int displayPower;
        public EnumFlow displayFlow = EnumFlow.STATIONARY;
        public long nextPowerQuery;
        public long internalNextPower;
        public final AverageInt powerAverage = new AverageInt(10);

        long powerQuery;
        long internalPower;

        /** Debugging fields */
        long debugPowerInput, debugPowerOutput, debugPowerOffered;

        public Section(EnumFacing side) {
            this.side = side;
        }

        void step() {
            powerQuery = nextPowerQuery;
            nextPowerQuery = 0;

            long next = internalPower;
            internalPower = internalNextPower;
            internalNextPower = next;
        }

        @Override
        public boolean canConnect(@Nonnull IMjConnector other) {
            return true;
        }

        @Override
        public long getPowerRequested() {
            long req = 0;
            for (EnumFacing face : EnumFacing.VALUES) {
                if (face != this.side) {
                    req += sections.get(face).powerQuery;
                }
            }
            return req;
        }

        long receivePowerInternal(long sent) {
            if (sent > 0) {
                debugPowerOffered += sent;
                internalNextPower += sent;
                return 0;
            }
            return sent;
        }

        @Override
        public long receivePower(long microJoules, boolean simulate) {
            if (isReceiver) {
                PipeFlowPower.this.step();
                if (!simulate) {
                    return this.receivePowerInternal(microJoules);
                }
                return 0;
            }
            return microJoules;
        }

        @Override
        public boolean canReceive() {
            return isReceiver;
        }
    }

    public enum EnumFlow {
        IN,
        OUT,
        STATIONARY
    }
}
