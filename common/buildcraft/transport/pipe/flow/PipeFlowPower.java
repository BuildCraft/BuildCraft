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

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjPassiveProvider;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.PipeEventPower;
import buildcraft.api.transport.neptune.IFlowPower;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.IPipe.ConnectedType;
import buildcraft.api.transport.neptune.PipeFlow;

import buildcraft.lib.engine.TileEngineBase_BC8;

public class PipeFlowPower extends PipeFlow implements IFlowPower, IDebuggable {

    private static final long DEFAULT_MAX_POWER = MjAPI.MJ * 10;

    long maxPower = DEFAULT_MAX_POWER;
    boolean isReceiver = false;
    final EnumMap<EnumFacing, Section> sections = createSections();

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
        configure.setMaxPower(maxPower);
        configure.setReceiver(isReceiver);
        pipe.getHolder().fireEvent(configure);
        maxPower = configure.getMaxPower();
        if (maxPower <= 0) {
            maxPower = DEFAULT_MAX_POWER;
        }
        isReceiver = configure.isReceiver();
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
        for (EnumFacing side : EnumFacing.VALUES) {
            Section s = sections.get(side);
            if (!pipe.isConnected(side)) {
                s.connection = PowerConnection.DEAD_END;
                continue;
            }
            ConnectedType type = pipe.getConnectedType(side);
            if (type == ConnectedType.PIPE) {
                IPipe oPipe = pipe.getConnectedPipe(side);
                if (oPipe != null && oPipe.getFlow() instanceof PipeFlowPower) {
                    PipeFlowPower oPower = (PipeFlowPower) oPipe.getFlow();
                    s.connection = oPower.getConnectionTypeExcluding(side.getOpposite(), getConnectionTypeExcluding(side, null));
                } else {
                    s.connection = PowerConnection.DEAD_END;
                }
            } else if (type == ConnectedType.TILE) {
                TileEntity tile = pipe.getConnectedTile(side);
                if (tile != null) {
                    s.connection = (tile instanceof TileEngineBase_BC8) ? PowerConnection.SENDER : PowerConnection.REQUEST;// TEMP!
                } else {
                    s.connection = PowerConnection.DEAD_END;
                }
            } else {
                s.connection = PowerConnection.DEAD_END;
            }
        }
    }

    private PowerConnection getConnectionTypeExcluding(EnumFacing ignore, PowerConnection from) {
        PowerConnection current = PowerConnection.DEAD_END;
        for (EnumFacing face : EnumFacing.VALUES) {
            PowerConnection c = sections.get(face).connection;
            if (face == ignore) continue;
            switch (c) {
                case UNKNOWN:
                    return PowerConnection.UNKNOWN;
                case SENDER:
                case REQUEST: {
                    PowerConnection other = c == PowerConnection.SENDER ? PowerConnection.REQUEST : PowerConnection.SENDER;
                    if (current == other) {
                        if (from == PowerConnection.REQUEST) {
                            return PowerConnection.SENDER;
                        } else if (from == PowerConnection.SENDER) {
                            return PowerConnection.REQUEST;
                        } else {
                            return PowerConnection.UNKNOWN;
                        }
                    } else {
                        current = c;
                        break;
                    }
                }
                case DEAD_END:
                    break;
                default:
                    throw new IllegalArgumentException("Unknown PowerConnection " + c);
            }
        }
        return current;
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
        left.add("Connections:");
        for (EnumFacing face : EnumFacing.VALUES) {
            if (!pipe.isConnected(face)) continue;
            Section section = sections.get(face);
            left.add("  " + face + " = " + section.connection);
        }
    }

    @SideOnly(Side.CLIENT)
    public PowerConnection getConnectionType(EnumFacing face) {
        return sections.get(face).connection;
    }

    public class Section implements IMjReceiver {
        public final EnumFacing side;

        long actualMaxPower = 0;
        long stored;
        long maxThisTick = maxPower;
        // long strength;
        // long maxStrength;
        PowerConnection connection = PowerConnection.UNKNOWN;

        public Section(EnumFacing side) {
            this.side = side;
        }

        void onTick() {
            actualMaxPower = pipe.isConnected(side) ? maxPower : 0;

            // maxStrength /= 2;
            // maxStrength = Math.max(maxStrength, strength);
            // strength = 0;

            maxThisTick = actualMaxPower - stored;
        }

        @Override
        public boolean canConnect(IMjConnector other) {
            return true;
        }

        @Override
        public long getPowerRequested() {
            return maxThisTick;
        }

        long receivePowerInternal(long sent) {
            return sent;
        }

        @Override
        public long receivePower(long microJoules, boolean simulate) {
            // if (!isReceiver) {
            return microJoules;
            // }
            // long toAccept = Math.max(microJoules, maxThisTick);
            // toAccept = Math.min(toAccept, actualMaxPower - stored);
            // if (toAccept <= 0) {
            // return microJoules;
            // }

            // if (!simulate) {
            // strength += toAccept;
            // maxThisTick -= toAccept;
            // stored += toAccept;
            // }

            // return microJoules - toAccept;
        }
    }

    public enum PowerConnection {
        UNKNOWN,
        DEAD_END,
        REQUEST,
        SENDER;
    }
}
