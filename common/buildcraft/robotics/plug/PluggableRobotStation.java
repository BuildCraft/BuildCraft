package buildcraft.robotics.plug;

import buildcraft.api.mj.IMjConnector;
import buildcraft.api.mj.IMjReadable;
import buildcraft.api.mj.IMjReceiver;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IDockingStationProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pipe.PipeEventHandler;
import buildcraft.api.transport.pipe.PipeEventPower;
import buildcraft.api.transport.pipe.PipeEventStatement;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.expression.DefaultContexts;
import buildcraft.lib.expression.FunctionContext;
import buildcraft.lib.expression.info.ContextInfo;
import buildcraft.lib.expression.info.VariableInfo;
import buildcraft.lib.expression.node.value.NodeVariableBoolean;
import buildcraft.lib.expression.node.value.NodeVariableObject;
import buildcraft.lib.misc.data.ModelVariableData;
import buildcraft.robotics.BCRoboticsItems;
import buildcraft.robotics.BCRoboticsModels;
import buildcraft.robotics.BCRoboticsStatements;
import buildcraft.robotics.client.model.key.KeyPlugRobotStation;
import buildcraft.robotics.entity.EntityRobot;
import buildcraft.robotics.pipe.DockingStationPipe;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// public class PluggableRobotStation extends PipePluggable implements IPipePluggableItem, IEnergyReceiver, IDebuggable, IDockingStationProvider
public class PluggableRobotStation extends PipePluggable implements IMjReceiver, IMjReadable, IDebuggable, IDockingStationProvider {
    // public enum EnumRobotStationState
    public enum EnumRobotStationState implements StringRepresentable {
        None,
        Available,
        Reserved,
        Linked;

        public String getTextureSuffix() {
            return name().toLowerCase(Locale.ROOT);
        }

        @Override
        public String getSerializedName() {
            return getTextureSuffix();
        }
    }

    public static final FunctionContext MODEL_FUNC_CTX;
    private static final NodeVariableObject<Direction> MODEL_SIDE;
    private static final NodeVariableObject<EnumRobotStationState> MODEL_ROBOT_STATION_STATE;
    private static final NodeVariableBoolean MODEL_CB;
    public static final ContextInfo MODEL_VAR_INFO;

    private static final VoxelShape[] BOXES = new VoxelShape[6];

    public final ModelVariableData clientModelData = new ModelVariableData();

    static {
        double ll = 2 / 16.0;
        double lu = 4 / 16.0 + 0.001;
        double ul = 12 / 16.0;
        double uu = 14 / 16.0;

        double min = 4 / 16.0;
        double max = 12 / 16.0;

        BOXES[Direction.DOWN.ordinal()] = Shapes.box(min, ll, min, max, lu, max);
        BOXES[Direction.UP.ordinal()] = Shapes.box(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.ordinal()] = Shapes.box(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.ordinal()] = Shapes.box(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.ordinal()] = Shapes.box(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.ordinal()] = Shapes.box(ul, min, min, uu, max, max);

        MODEL_FUNC_CTX = DefaultContexts.createWithAll();

        MODEL_SIDE = MODEL_FUNC_CTX.putVariableObject("side", Direction.class);
        MODEL_ROBOT_STATION_STATE = MODEL_FUNC_CTX.putVariableObject("state", EnumRobotStationState.class);
        MODEL_CB = MODEL_FUNC_CTX.putVariableBoolean("cb");

        MODEL_VAR_INFO = new ContextInfo(MODEL_FUNC_CTX);
        VariableInfo.VariableInfoObject<Direction> infoSide = MODEL_VAR_INFO.createInfoObject(MODEL_SIDE);
        infoSide.cacheType = VariableInfo.CacheType.ALWAYS;
        infoSide.setIsComplete = true;
        infoSide.possibleValues.addAll(Arrays.asList(Direction.values()));

        VariableInfo.VariableInfoObject<EnumRobotStationState> infoStage = MODEL_VAR_INFO.createInfoObject(MODEL_ROBOT_STATION_STATE);
        infoStage.cacheType = VariableInfo.CacheType.ALWAYS;
        infoStage.setIsComplete = true;
        infoStage.possibleValues.addAll(Arrays.asList(EnumRobotStationState.values()));
    }

    private EnumRobotStationState renderState;
    private DockingStationPipe station;
    private boolean isValid = false;


    public PluggableRobotStation(PluggableDefinition definition, IPipeHolder holder, Direction side) {
        super(definition, holder, side);
    }

    public PluggableRobotStation(PluggableDefinition definition, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
        super(definition, holder, side);
        readData(buffer);
    }

    public PluggableRobotStation(PluggableDefinition definition, IPipeHolder holder, Direction side, CompoundTag nbt) {
        super(definition, holder, side);
    }

    // Calen 1.18.2: PluggableRobotStation::new impl IPluggableCreator
    @Override
    // public void writeToNBT(CompoundTag nbt)
    public CompoundTag writeToNbt() {
        return new CompoundTag();
    }

//    @Override
//    public void readFromNBT(CompoundTag nbt) {
//
//    }

    @Override
    // public ItemStack[] getDropItems(IPipeHolder pipe)
    public void addDrops(NonNullList<ItemStack> toDrop, int fortune) {
        toDrop.add(new ItemStack(BCRoboticsItems.robotStation.get()));
    }

    @Override
    public ItemStack getPickStack() {
        return new ItemStack(BCRoboticsItems.robotStation.get());
    }

    @Override
    public void onTick() {
        if (holder.getPipeWorld().isClientSide) {
            clientModelData.tick();
        }
    }

    @Override
    public DockingStation getStation() {
        return station;
    }

    @Override
    // public boolean isBlocking(IPipeHolder pipe, Direction direction)
    public boolean isBlocking() {
        return true;
    }

    @Override
    // public void invalidate()
    public void onRemove() {
        if (station != null && station.getPipe() != null && !station.getPipe().getPipeWorld().isClientSide) {
            RobotManager.registryProvider.getRegistry(station.world).removeStation(station);
            isValid = false;
        }
    }

    // @Override
    // public void validate(IPipeHolder pipe, Direction direction)
    public void validate() {
        IPipeHolder pipe = this.holder;
        Direction direction = this.side;
        if (!isValid && !pipe.getPipeWorld().isClientSide) {
            station = (DockingStationPipe) RobotManager.registryProvider.getRegistry(pipe.getPipeWorld()).getStation(((BlockEntity) pipe).getBlockPos(), direction);

            if (station == null) {
                station = new DockingStationPipe(pipe, direction);
                RobotManager.registryProvider.getRegistry(pipe.getPipeWorld()).registerStation(station);
            }

            isValid = true;
        }
    }

    @Override
    // public AABB getBoundingBox(Direction side)
    public VoxelShape getBoundingBox() {
//        float[][] bounds = new float[3][2];
//        // X START - END
//        bounds[0][0] = 0.25F;
//        bounds[0][1] = 0.75F;
//        // Y START - END
//        bounds[1][0] = 0.125F;
//        bounds[1][1] = 0.251F;
//        // Z START - END
//        bounds[2][0] = 0.25F;
//        bounds[2][1] = 0.75F;
//
//        MatrixTranformations.transform(bounds, side);
//        return new AxisAlignedBB(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
        return BOXES[side.ordinal()];
    }

    private void refreshRenderState() {
        this.renderState = station.isTaken() ? (station.isMainStation() ? EnumRobotStationState.Linked : EnumRobotStationState.Reserved)
                : EnumRobotStationState.Available;
    }

    public EnumRobotStationState getRenderState() {
        if (renderState == null) {
            renderState = EnumRobotStationState.None;
        }
        return renderState;
    }

    // @Override
    // public IPipePluggableStaticRenderer getRenderer() {
    //     return RobotStationRenderer.INSTANCE;
    // }
    @OnlyIn(Dist.CLIENT)
    @Override
    public PluggableModelKey getModelRenderKey(RenderType layer) {
        if (layer == RenderType.cutout()) return new KeyPlugRobotStation(side, renderState);
        return null;
    }

    @Override
    public void writeCreationPayload(FriendlyByteBuf buffer) {
        super.writeCreationPayload(buffer);
        validate();
        writeData(buffer);
    }

    @Override
//    public void readPayload(PacketBuffer buffer, Dist side, MessageContext ctx) throws IOException
    public void readPayload(FriendlyByteBuf buffer, NetworkDirection side, NetworkEvent.Context ctx) throws IOException {
        super.readPayload(buffer, side, ctx);
        if (side == NetworkDirection.PLAY_TO_CLIENT) {
            readData(buffer);
        }
    }

    @Override
    public void writePayload(FriendlyByteBuf buffer, Dist side) {
        super.writePayload(buffer, side);
        if (side == Dist.DEDICATED_SERVER) {
            writeData(buffer);
        }
    }

    // @Override
    public void writeData(FriendlyByteBuf data) {
        refreshRenderState();
        data.writeByte(getRenderState().ordinal());
    }

//    @Override
//    public boolean requiresRenderUpdate(PipePluggable o) {
//        return getRenderState() != ((PluggableRobotStation) o).getRenderState();
//    }

    // @Override
    public void readData(FriendlyByteBuf data) {
        byte num = data.readByte();
        this.renderState = EnumRobotStationState.values()[(num % 4 + 4) % 4];
    }

    // @Override
    // public PipePluggable createPipePluggable(IPipe pipe, Direction side, ItemStack stack) {
    //     return new PluggableRobotStation();
    // }

    @Override
    public long getPowerRequested() {
        if (station != null && station.robotTaking() != null && station.robotTaking().getBattery() != null && station.robotTaking().getDockingStation() == station) {
            return station.robotTaking().getBattery().getCapacity() - station.robotTaking().getBattery().getStored();
        }
        return 0;
    }

    @Override
    // public long receiveEnergy(Direction from, int maxReceive, boolean simulate)
    public long receivePower(long maxReceive, boolean simulate) {
        if (station != null && station.robotTaking() != null && station.robotTaking().getBattery() != null && station.robotTaking().getDockingStation() == station) {
            return ((EntityRobot) station.robotTaking()).receiveEnergy(maxReceive, simulate);
        }
        // return 0L;
        return maxReceive;
    }

    @Override
    // public int getEnergyStored(Direction from)
    public long getStored() {
        if (station != null && station.robotTaking() != null && station.robotTaking().getBattery() != null && station.robotTaking().getDockingStation() == station) {
            return station.robotTaking().getBattery().getStored();
        }
        return 0L;
    }

    @Override
    // public int getMaxEnergyStored(Direction from)
    public long getCapacity() {
        if (station != null && station.robotTaking() != null && station.robotTaking().getBattery() != null && station.robotTaking().getDockingStation() == station) {
            return station.robotTaking().getBattery().getCapacity();
        }
        return 0L;
    }

    @Override
    // public boolean canConnectEnergy(Direction from)
    public boolean canConnect(@Nonnull IMjConnector other) {
        return true;
    }

    @Override
    public void getDebugInfo(List<Component> left, List<Component> right, Direction side) {
        if (station == null) {
            left.add(new TextComponent("PluggableRobotStation: No station found!"));
        } else {
            refreshRenderState();
            left.add(new TextComponent("Docking Station (side " + side.name() + ", " + renderState.name() + ")"));
            if (station.robotTaking() != null && station.robotTaking() instanceof IDebuggable) {
                ((IDebuggable) station.robotTaking()).getDebugInfo(left, right, side);
            }
        }
    }

    @Override
    public <T> T getInternalCapability(@Nonnull Capability<T> cap) {
        if (cap == MjAPI.CAP_CONNECTOR || cap == MjAPI.CAP_RECEIVER || cap == MjAPI.CAP_READABLE) {
            return (T) this;
        }
        return null;
    }

    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap) {
        if (cap == MjAPI.CAP_CONNECTOR || cap == MjAPI.CAP_RECEIVER || cap == MjAPI.CAP_READABLE) {
            return LazyOptional.of(() -> this).cast();
        }
        return LazyOptional.empty();
    }

    // Model

    public static void setClientModelVariables(Direction side, EnumRobotStationState state, boolean cb) {
        MODEL_SIDE.value = side;
        MODEL_ROBOT_STATION_STATE.value = state;
        MODEL_CB.value = cb;
        ModelVariableData varData = new ModelVariableData();
        varData.setNodes(BCRoboticsModels.ROBOT_STATION_DYNAMIC.createTickableNodes());
        varData.tick();
        varData.refresh();
    }

    public void setClientModelVariables() {
        setClientModelVariables(side, getRenderState(), BCLibConfig.colourBlindMode);
    }

    public static void setModelVariablesForItem() {
        setClientModelVariables(Direction.WEST, EnumRobotStationState.Available, BCLibConfig.colourBlindMode);
    }

    @PipeEventHandler
    public void onAddActions(PipeEventStatement.AddTriggerInternal event) {
        event.triggers.add(BCRoboticsStatements.triggerRobotSleep);
        event.triggers.add(BCRoboticsStatements.triggerRobotInStation);
        event.triggers.add(BCRoboticsStatements.triggerRobotLinked);
        event.triggers.add(BCRoboticsStatements.triggerRobotReserved);
    }

    @PipeEventHandler
    public void onAddActions(PipeEventStatement.AddActionInternal event) {
        event.actions.add(BCRoboticsStatements.actionRobotGotoStation);
        event.actions.add(BCRoboticsStatements.actionRobotWakeUp);
        event.actions.add(BCRoboticsStatements.actionRobotWorkInArea);
        event.actions.add(BCRoboticsStatements.actionRobotLoadUnloadArea);
        event.actions.add(BCRoboticsStatements.actionRobotFilter);
        event.actions.add(BCRoboticsStatements.actionRobotFilterTool);
        event.actions.add(BCRoboticsStatements.actionStationRequestItems);
        event.actions.add(BCRoboticsStatements.actionStationProvideItems);
        event.actions.add(BCRoboticsStatements.actionStationAcceptFluids);
        event.actions.add(BCRoboticsStatements.actionStationProvideFluids);
        event.actions.add(BCRoboticsStatements.actionStationForceRobot);
        event.actions.add(BCRoboticsStatements.actionStationForbidRobot);
        event.actions.add(BCRoboticsStatements.actionStationAcceptItems);
        event.actions.add(BCRoboticsStatements.actionStationMachineRequestItems);
    }

    @PipeEventHandler
    public void onPowerConfigure(PipeEventPower.Configure event) {
        event.setReceiver(true);
    }
}
