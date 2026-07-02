package ct.buildcraft.robotics.plug;

import java.io.IOException;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ct.buildcraft.api.mj.IMjConnector;
import ct.buildcraft.api.mj.IMjReadable;
import ct.buildcraft.api.mj.IMjReceiver;
import ct.buildcraft.api.mj.MjAPI;
import ct.buildcraft.api.mj.MjCapabilityHelper;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.IDockingStationProvider;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.RobotManager;
import ct.buildcraft.api.transport.pipe.IPipeHolder;
import ct.buildcraft.api.transport.pluggable.PipePluggable;
import ct.buildcraft.api.transport.pluggable.PluggableDefinition;
import ct.buildcraft.api.transport.pluggable.PluggableModelKey;
import ct.buildcraft.robotics.BCRoboticsItems;
import ct.buildcraft.robotics.DockingStationPipe;
import ct.buildcraft.robotics.client.model.key.KeyRobotStation;
import ct.buildcraft.robotics.entity.EntityRobot;
import ct.buildcraft.robotics.item.ItemRobot;
import ct.buildcraft.transport.tile.TilePipeHolder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

public class RobotStationPluggable extends PipePluggable implements IDockingStationProvider, IMjReceiver, IMjReadable {
    /** Match the old robot battery receiver: at most 100 robot-energy units per tick = 1 MJ/t in this port. */
    private static final long MAX_CHARGE_PER_TICK = MjAPI.MJ;

    public enum RobotStationState {
        None,
        Available,
        Reserved,
        Linked
    }

    private static final VoxelShape[] BOXES = new VoxelShape[6];

    static {
        double min = 4.0D;
        double max = 12.0D;
        double low = 2.0D;
        double high = 4.016D;
        BOXES[Direction.DOWN.get3DDataValue()] = Block.box(min, low, min, max, high, max);
        BOXES[Direction.UP.get3DDataValue()] = Block.box(min, 16.0D - high, min, max, 16.0D - low, max);
        BOXES[Direction.NORTH.get3DDataValue()] = Block.box(min, min, low, max, max, high);
        BOXES[Direction.SOUTH.get3DDataValue()] = Block.box(min, min, 16.0D - high, max, max, 16.0D - low);
        BOXES[Direction.WEST.get3DDataValue()] = Block.box(low, min, min, high, max, max);
        BOXES[Direction.EAST.get3DDataValue()] = Block.box(16.0D - high, min, min, 16.0D - low, max, max);
    }

    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(this);
    private DockingStationPipe station;
    private RobotStationState renderState = RobotStationState.None;
    private boolean valid;

    public RobotStationPluggable(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    @Override
    public DockingStation getStation() {
        validateStation();
        return station;
    }

    @Nullable
    private EntityRobotBase getDockedRobot() {
        validateStation();
        EntityRobotBase robot = station == null ? null : station.robotTaking();
        return robot != null && robot.getDockingStation() == station ? robot : null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == MjAPI.CAP_CONNECTOR || cap == MjAPI.CAP_RECEIVER || cap == MjAPI.CAP_READABLE) {
            validateStation();
            return mjCaps.getCapability(cap, side);
        }
        return LazyOptional.empty();
    }

    @Override
    public <T> LazyOptional<T> getInternalCapability(@Nonnull Capability<T> cap) {
        if (cap == MjAPI.CAP_CONNECTOR || cap == MjAPI.CAP_RECEIVER || cap == MjAPI.CAP_READABLE) {
            validateStation();
            return mjCaps.getCapability(cap, side);
        }
        return LazyOptional.empty();
    }

    @Override
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public long getPowerRequested() {
        EntityRobotBase robot = getDockedRobot();
        if (robot == null || robot.getBattery() == null) {
            return 0;
        }
        if (robot instanceof EntityRobot entityRobot) {
            return Math.min(entityRobot.getMjPowerRequestedForCharging(), MAX_CHARGE_PER_TICK);
        }
        return Math.min(Math.max(0L, robot.getBattery().getCapacity() - robot.getBattery().getStored()), MAX_CHARGE_PER_TICK);
    }

    @Override
    public long receivePower(long microJoules, FluidAction action) {
        EntityRobotBase robot = getDockedRobot();
        if (microJoules <= 0 || robot == null || robot.getBattery() == null) {
            return microJoules;
        }
        long accepted = Math.min(microJoules, getPowerRequested());
        if (accepted > 0 && action.execute()) {
            if (robot instanceof EntityRobot entityRobot) {
                entityRobot.receivePower(accepted, FluidAction.EXECUTE);
            } else {
                robot.getBattery().addPower(accepted, FluidAction.EXECUTE);
            }
        }
        return microJoules - accepted;
    }

    @Override
    public boolean canReceive() {
        return getPowerRequested() > 0;
    }

    @Override
    public long getStored() {
        EntityRobotBase robot = getDockedRobot();
        return robot == null || robot.getBattery() == null ? 0 : robot.getBattery().getStored();
    }

    @Override
    public long getCapacity() {
        EntityRobotBase robot = getDockedRobot();
        return robot == null || robot.getBattery() == null ? EntityRobotBase.MAX_ENERGY : robot.getBattery().getCapacity();
    }

    private void validateStation() {
        if (valid || holder == null || holder.getPipeWorld() == null || holder.getPipeWorld().isClientSide) {
            return;
        }
        if (!(holder instanceof TilePipeHolder tile)) {
            return;
        }
        if (RobotManager.registryProvider == null) {
            return;
        }
        DockingStation existing = RobotManager.registryProvider.getRegistry(holder.getPipeWorld()).getStation(
                holder.getPipePos(), side);
        if (existing instanceof DockingStationPipe pipeStation) {
            station = pipeStation;
        } else {
            station = new DockingStationPipe(tile, side);
            RobotManager.registryProvider.getRegistry(holder.getPipeWorld()).registerStation(station);
        }
        valid = true;
    }

    @Override
    public void onPlacedBy(Player player) {
        validateStation();
        refreshRenderState();
        scheduleNetworkUpdate();
    }

    @Override
    public void onTick() {
        validateStation();
        RobotStationState old = renderState;
        refreshRenderState();
        if (old != renderState) {
            scheduleNetworkUpdate();
            if (holder != null) holder.scheduleRenderUpdate();
        }
    }

    @Override
    public void onRemove() {
        if (station != null && station.level() != null && !station.level().isClientSide && RobotManager.registryProvider != null) {
            RobotManager.registryProvider.getRegistry(station.level()).removeStation(station);
        }
        valid = false;
        station = null;
    }

    @Override
    public boolean isBlocking() {
        return true;
    }

    @Override
    public VoxelShape getBoundingBox() {
        return BOXES[side.get3DDataValue()];
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCRoboticsItems.ROBOT_STATION.get());
    }

    @Override
    public InteractionResult onPluggableActivate(Player player, BlockHitResult trace, Level level) {
        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof ItemRobot)) {
            held = player.getOffhandItem();
        }
        if (held.getItem() instanceof ItemRobot && holder instanceof TilePipeHolder tile) {
            return ItemRobot.placeOnStation(held, player, level, tile, side, this);
        }
        return InteractionResult.PASS;
    }

    private void refreshRenderState() {
        if (station == null) {
            renderState = RobotStationState.None;
        } else if (!station.isTaken()) {
            renderState = RobotStationState.Available;
        } else if (station.isMainStation()) {
            renderState = RobotStationState.Linked;
        } else {
            renderState = RobotStationState.Reserved;
        }
    }

    public RobotStationState getRenderState() {
        return renderState == null ? RobotStationState.None : renderState;
    }

    @Override
    public void writeCreationPayload(FriendlyByteBuf buffer) {
        refreshRenderState();
        buffer.writeByte(getRenderState().ordinal());
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, LogicalSide side) {
        refreshRenderState();
        buffer.writeByte(getRenderState().ordinal());
    }

    @Override
    public void readPayload(FriendlyByteBuf buffer, LogicalSide side, NetworkEvent.Context ctx) throws IOException {
        readState(buffer);
    }

    public static RobotStationPluggable readFromNbt(PluggableDefinition definition, IPipeHolder holder, Direction side, net.minecraft.nbt.CompoundTag tag) {
        return new RobotStationPluggable(definition, holder, side);
    }

    public static RobotStationPluggable loadFromBuffer(PluggableDefinition definition, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
        RobotStationPluggable plug = new RobotStationPluggable(definition, holder, side);
        plug.readState(buffer);
        return plug;
    }

    private void readState(FriendlyByteBuf buffer) {
        int id = buffer.readUnsignedByte();
        RobotStationState[] values = RobotStationState.values();
        renderState = id >= 0 && id < values.length ? values[id] : RobotStationState.None;
    }

    @Override
    @Nullable
    @OnlyIn(Dist.CLIENT)
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        if (layer == RenderType.cutout()) {
            return new KeyRobotStation(side, getRenderState());
        }
        return null;
    }
}
