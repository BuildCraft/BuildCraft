package buildcraft.transport.pipe.flow;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

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
import buildcraft.api.transport.pipe.*;
import buildcraft.api.transport.pipe.IPipe.ConnectedType;
import buildcraft.api.transport.pipe.PipeApi.PowerTransferInfo;

import buildcraft.core.BCCoreConfig;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.data.AverageInt;

public class PipeFlowPower extends PipeFlow implements IFlowPower, IDebuggable {

    private static final long DEFAULT_MAX_POWER = MjAPI.MJ * 10;

    public Vec3d clientFlowCenter;

    long maxPower = -1;
    long powerLoss = -1;
    long powerResistance = -1;

    private long currentWorldTime;

    boolean isReceiver = false;
    final EnumMap<EnumFacing, Section> sections = createSections();

    final SafeTimeTracker tracker = new SafeTimeTracker(2 * BCCoreConfig.networkUpdateRate);

    private EnumMap<EnumFacing, Section> createSections() {
        EnumMap<EnumFacing, Section> map = new EnumMap<>(EnumFacing.class);
        for (EnumFacing face : EnumFacing.VALUES) {
            map.put(face, new Section(face));
        }
        return map;
    }

    public PipeFlowPower(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowPower(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();

        return nbt;
    }

    @Override
    public void readPayload(int id, PacketBuffer buffer, Side side) throws IOException {
        super.readPayload(id, buffer, side);
    }

    @Override
    public void writePayload(int id, PacketBuffer buffer, Side side) {
        super.writePayload(id, buffer, side);
    }

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowPower;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        if (isReceiver) {
            IMjPassiveProvider provider = oTile.getCapability(MjAPI.CAP_PASSIVE_PROVIDER, face.getOpposite());
            if (provider != null) return true;
        }
        IMjConnector reciever = oTile.getCapability(MjAPI.CAP_CONNECTOR, face.getOpposite());
        if (reciever == null) return false;
        return reciever.canConnect(sections.get(face));
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
        if (!isReceiver) return 0;
        TileEntity tile = pipe.getConnectedTile(from);
        if (tile == null) return 0;
        IMjPassiveProvider reciever = tile.getCapability(MjAPI.CAP_PASSIVE_PROVIDER, from.getOpposite());
        if (reciever == null) return 0;

        // TODO!
        return 0;
    }

    @Override
    public boolean onFlowActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        return super.onFlowActivate(player, trace, hitX, hitY, hitZ, part);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (facing == null) {
            return null;
        } else if (capability == MjAPI.CAP_RECEIVER) {
            return isReceiver ? (T) sections.get(facing) : null;
        } else if (capability == MjAPI.CAP_CONNECTOR) {
            return (T) sections.get(facing);
        } else {
            return null;
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("maxPower = " + LocaleUtil.localizeMj(maxPower));
        left.add("isReceiver = " + isReceiver);
        left.add("internalPower = " + arrayToString(s -> s.internalPower) + " <- " + arrayToString(s -> s.internalNextPower));
        left.add("- powerQuery: " + arrayToString(s -> s.powerQuery) + " <- " + arrayToString(s -> s.nextPowerQuery));
        left.add("- power: IN " + arrayToString(s -> s.debugPowerInput) + ", OUT " + arrayToString(s -> s.debugPowerOutput));
        left.add("- power: OFFERED " + arrayToString(s -> s.debugPowerOffered));
    }

    interface ISectionPropertyGetter {
        long get(Section s);
    }

    private String arrayToString(ISectionPropertyGetter getter) {
        long[] arr = new long[6];
        for (EnumFacing face : EnumFacing.VALUES) {
            arr[face.ordinal()] = getter.get(sections.get(face)) / MjAPI.MJ;
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
                            if (neighbour != null && neighbour.getFlow() instanceof PipeFlowPower && neighbour.isConnected(face2.getOpposite())) {
                                PipeFlowPower oFlow = (PipeFlowPower) neighbour.getFlow();
                                leftover = oFlow.sections.get(face2.getOpposite()).receivePowerInternal(watts);
                            } else {
                                IMjReceiver reciever = pipe.getHolder().getCapabilityFromPipe(face2, MjAPI.CAP_RECEIVER);
                                if (reciever != null && reciever.canReceive()) {
                                    leftover = reciever.receivePower(watts, false);
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
        long[] transferQuery = new long[6];
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
            transferQuery[face.ordinal()] = query;
        }

        // Transfer requested power to neighbouring pipes
        for (EnumFacing face : EnumFacing.VALUES) {
            if (transferQuery[face.ordinal()] <= 0 || !pipe.isConnected(face)) {
                continue;
            }
            IPipe oPipe = pipe.getHolder().getNeighbourPipe(face);
            if (oPipe == null || !(oPipe.getFlow() instanceof PipeFlowPower)) {
                continue;
            }
            PipeFlowPower oFlow = (PipeFlowPower) oPipe.getFlow();
            oFlow.requestPower(face.getOpposite(), transferQuery[face.ordinal()]);
        }
        // Networking
    }

    private void step() {
        long now = pipe.getHolder().getPipeWorld().getTotalWorldTime();
        if (currentWorldTime != now) {
            currentWorldTime = now;
            for (EnumFacing face : EnumFacing.VALUES) {
                sections.get(face).step();
            }
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

    public class Section implements IMjReceiver {
        public final EnumFacing side;

        public final AverageInt clientDisplayAverage = new AverageInt(10);
        public double clientDisplayFlow;

        public long displayPower;
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
        public boolean canConnect(IMjConnector other) {
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
