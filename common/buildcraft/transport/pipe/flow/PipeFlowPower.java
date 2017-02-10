package buildcraft.transport.pipe.flow;

import java.io.IOException;
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
import buildcraft.api.transport.PipeEventPower;
import buildcraft.api.transport.neptune.IFlowPower;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.PipeFlow;

import buildcraft.core.BCCoreConfig;
import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.misc.data.AverageInt;

public class PipeFlowPower extends PipeFlow implements IFlowPower, IDebuggable {

    private static final long DEFAULT_MAX_POWER = MjAPI.MJ * 10;

    public Vec3d clientFlowCenter;

    long maxPower = -1;
    long powerLoss = -1;
    long powerResistance = -1;

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
        configure.setReceiver(isReceiver);
        configure.setMaxPower(maxPower);
        configure.setPowerLoss(powerLoss);
        configure.setPowerResistance(powerResistance);
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
    public void onTick() {
        if (maxPower == -1) {
            reconfigure();
        }
    }

    @Override
    public boolean onFlowActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        return super.onFlowActivate(player, trace, hitX, hitY, hitZ, part);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (facing == null || capability != MjAPI.CAP_CONNECTOR || capability != MjAPI.CAP_RECEIVER) {
            return null;
        }
        return (T) sections.get(facing);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {

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

        void onTick() {

        }

        @Override
        public boolean canConnect(IMjConnector other) {
            return true;
        }

        @Override
        public long getPowerRequested() {
            return 0;
        }

        long receivePowerInternal(long sent) {
            return sent;
        }

        @Override
        public long receivePower(long microJoules, boolean simulate) {
            return microJoules;
        }
    }

    public enum EnumFlow {
        IN,
        OUT,
        STATIONARY
    }
}
