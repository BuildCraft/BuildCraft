package ct.buildcraft.robotics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ct.buildcraft.api.core.BlockIndex;
import ct.buildcraft.api.robots.DockingStation;
import ct.buildcraft.api.robots.EntityRobotBase;
import ct.buildcraft.api.robots.IRequestProvider;
import ct.buildcraft.api.robots.RobotManager;
import ct.buildcraft.api.statements.IStatementParameter;
import ct.buildcraft.api.statements.StatementSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.item.DyeColor;
import ct.buildcraft.compat.CompatCapTransfromer;
import ct.buildcraft.lib.misc.CapUtil;
import ct.buildcraft.robotics.statements.ActionStationRequestItems;
import ct.buildcraft.robotics.plug.RobotStationPluggable;
import ct.buildcraft.silicon.plug.PluggableGate;
import ct.buildcraft.transport.pipe.behaviour.PipeBehaviourWood;
import ct.buildcraft.transport.pipe.flow.PipeFlowItems;
import ct.buildcraft.transport.pipe.flow.PipeFlowFluids;
import ct.buildcraft.transport.pipe.flow.PipeFlowPower;
import ct.buildcraft.api.transport.IInjectable;
import ct.buildcraft.transport.tile.TilePipeHolder;
import ct.buildcraft.transport.pipe.Pipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import org.jetbrains.annotations.NotNull;

public class DockingStationPipe extends DockingStation implements IRequestProvider {
    private TilePipeHolder pipe;
    private boolean removingInvalidStation;

    private final IInjectable injectablePipe = new IInjectable() {
        @Override
        public boolean canInjectItems(Direction from) {
            return getPipe() != null && getPipe().getPipe() != null
                    && getPipe().getPipe().flow instanceof PipeFlowItems;
        }

        @Override
        public ItemStack injectItem(ItemStack stack, boolean doAdd, Direction from, DyeColor color, double speed) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            if (getPipe() == null || !(getPipe().getPipe().flow instanceof PipeFlowItems items)) {
                return stack;
            }
            if (doAdd) {
                // In PipeFlowItems the "from" side is the side the stack came from and it is excluded from routing
                // when the item reaches the pipe centre. The robot station sits on side(), so use that side here. The
                // old 1.7 transport used side().getOpposite(), but the 1.19 flow semantics are inverted compared to
                // the old TravelingItem injection path; using the station side prevents picker output from instantly
                // bouncing/dropping instead of travelling into the pipe network.
                items.insertItemsForce(stack.copy(), normalizeOutputSide(from), color, speed);
            }
            return ItemStack.EMPTY;
        }
    };

    private final IFluidHandler injectableFluidPipe = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 0;
        }

        @Override
        public @NotNull FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
            return canFillFluid(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || getPipe() == null || getPipe().getPipe() == null
                    || !(getPipe().getPipe().flow instanceof PipeFlowFluids fluids)) {
                return 0;
            }
            return fluids.insertFluidsForce(resource.copy(), normalizeOutputSide(side()), action);
        }

        @Override
        public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }

        private boolean canFillFluid(FluidStack stack) {
            if (stack.isEmpty() || getPipe() == null || getPipe().getPipe() == null
                    || !(getPipe().getPipe().flow instanceof PipeFlowFluids fluids)) {
                return false;
            }
            return fluids.insertFluidsForce(stack.copy(), normalizeOutputSide(side()), FluidAction.SIMULATE) > 0;
        }
    };

    public DockingStationPipe() {
    }

    public DockingStationPipe(TilePipeHolder pipe, Direction side) {
        super(new BlockIndex(pipe.getPipePos()), side);
        this.pipe = pipe;
        setLevel(pipe.getPipeWorld());
    }

    public TilePipeHolder getPipe() {
        Level stationLevel = level();
        if (stationLevel == null) {
            return pipe != null && !pipe.isRemoved() ? pipe : null;
        }

        BlockPos pos = new BlockPos(x(), y(), z());

        // A station whose chunk is merely unloaded must stay in the robotics registry so the robot can rebind by id
        // when both sides load again. Only validate and remove the station if the chunk is loaded.
        if (!stationLevel.isLoaded(pos)) {
            return pipe;
        }

        BlockEntity blockEntity = stationLevel.getBlockEntity(pos);
        if (blockEntity instanceof TilePipeHolder holder
                && !holder.isRemoved()
                && holder.getPipe() != null
                && holder.getPipe() != Pipe.EMPTY
                && side() != null
                && holder.getPluggable(side()) instanceof RobotStationPluggable) {
            pipe = holder;
            return pipe;
        }

        pipe = null;
        removeInvalidStation(stationLevel);
        return null;
    }

    private void removeInvalidStation(Level stationLevel) {
        if (stationLevel == null || stationLevel.isClientSide || RobotManager.registryProvider == null || removingInvalidStation) {
            return;
        }

        removingInvalidStation = true;
        try {
            RobotManager.registryProvider.getRegistry(stationLevel).removeStation(this);
        } finally {
            removingInvalidStation = false;
        }
    }

    private void scheduleRenderUpdateIfLoaded() {
        if (pipe != null && !pipe.isRemoved() && pipe.getLevel() != null) {
            pipe.scheduleRenderUpdate();
        }
    }

    private Direction normalizeOutputSide(Direction from) {
        return side() != null ? side() : from;
    }

    @Override
    public Iterable<StatementSlot> getActiveActions() {
        if (getPipe() == null) {
            return Collections.emptyList();
        }

        List<StatementSlot> actions = new ArrayList<>();
        for (Direction direction : Direction.values()) {
            if (getPipe().getPluggable(direction) instanceof PluggableGate gate) {
                actions.addAll(gate.logic.getActiveActions());
            }
        }
        return actions;
    }

    @Override
    public boolean isInitialized() {
        return getPipe() != null && getPipe().getPipe() != null;
    }

    @Override
    public boolean take(EntityRobotBase robot) {
        if (getPipe() == null) return false;
        boolean result = super.take(robot);
        if (result) {
            pipe.scheduleRenderUpdate();
        }
        return result;
    }

    @Override
    public boolean takeAsMain(EntityRobotBase robot) {
        if (getPipe() == null) return false;
        boolean result = super.takeAsMain(robot);
        if (result) {
            pipe.scheduleRenderUpdate();
        }
        return result;
    }

    @Override
    public void unsafeRelease(EntityRobotBase robot) {
        super.unsafeRelease(robot);
        scheduleRenderUpdateIfLoaded();
    }

    @Override
    public IInjectable getItemOutput() {
        return getPipe() != null && getPipe().getPipe().flow instanceof PipeFlowItems ? injectablePipe : null;
    }

    @Override
    public Direction getItemOutputSide() {
        return side();
    }

    @Override
    public Container getItemInput() {
        Direction inputSide = getItemInputPipeSide();
        if (getPipe() == null || inputSide == null || level() == null) return null;
        BlockEntity neighbour = level().getBlockEntity(new net.minecraft.core.BlockPos(x(), y(), z()).relative(inputSide));
        return neighbour instanceof Container container ? container : null;
    }

    @Override
    public Direction getItemInputSide() {
        Direction inputSide = getItemInputPipeSide();
        return inputSide == null ? null : inputSide.getOpposite();
    }

    @Override
    public IFluidHandler getFluidOutput() {
        return getPipe() != null && getPipe().getPipe() != null && getPipe().getPipe().flow instanceof PipeFlowFluids ? injectableFluidPipe : null;
    }

    @Override
    public Direction getFluidOutputSide() {
        return side();
    }

    @Override
    public IFluidHandler getFluidInput() {
        Direction inputSide = getFluidInputPipeSide();
        if (getPipe() == null || inputSide == null || level() == null) return null;
        BlockEntity neighbour = level().getBlockEntity(new net.minecraft.core.BlockPos(x(), y(), z()).relative(inputSide));
        if (neighbour == null) return null;
        return CompatCapTransfromer.INSTANCE.getCap(neighbour, CapUtil.CAP_FLUIDS, inputSide.getOpposite()).orElse(null);
    }

    @Override
    public Direction getFluidInputSide() {
        Direction inputSide = getFluidInputPipeSide();
        return inputSide == null ? null : inputSide.getOpposite();
    }

    private Direction getItemInputPipeSide() {
        if (getPipe() == null || getPipe().getPipe() == null) return null;
        if (!(getPipe().getPipe().flow instanceof PipeFlowItems)) return null;
        if (!(getPipe().getPipe().behaviour instanceof PipeBehaviourWood wood)) return null;
        Direction inputSide = wood.getCurrentDir();
        if (inputSide == null || inputSide == side()) return null;
        return inputSide;
    }

    private Direction getFluidInputPipeSide() {
        if (getPipe() == null || getPipe().getPipe() == null) return null;
        if (!(getPipe().getPipe().flow instanceof PipeFlowFluids)) return null;
        if (!(getPipe().getPipe().behaviour instanceof PipeBehaviourWood wood)) return null;
        Direction inputSide = wood.getCurrentDir();
        if (inputSide == null || inputSide == side()) return null;
        return inputSide;
    }

    @Override
    public boolean providesPower() {
        return getPipe() != null && getPipe().getPipe() != null && getPipe().getPipe().flow instanceof PipeFlowPower;
    }

    @Override
    public int getRequestsCount() {
        return getActiveItemRequests().size();
    }

    @Override
    public ItemStack getRequest(int slot) {
        List<ItemStack> requests = getActiveItemRequests();
        return slot >= 0 && slot < requests.size() ? requests.get(slot).copy() : ItemStack.EMPTY;
    }

    @Override
    public ItemStack offerItem(int slot, ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (getRequest(slot).isEmpty()) {
            return stack;
        }

        IInjectable output = getItemOutput();
        Direction outputSide = getItemOutputSide();
        if (output == null || outputSide == null || !output.canInjectItems(outputSide)) {
            return stack;
        }
        return output.injectItem(stack.copy(), true, outputSide, null, 0.08D);
    }

    private List<ItemStack> getActiveItemRequests() {
        List<ItemStack> requests = new ArrayList<>();
        if (getPipe() == null || getPipe().getPipe() == null || !(getPipe().getPipe().flow instanceof PipeFlowItems)) {
            return requests;
        }

        for (StatementSlot slot : getActiveActions()) {
            if (!(slot.statement instanceof ActionStationRequestItems) || slot.parameters == null) {
                continue;
            }

            for (IStatementParameter parameter : slot.parameters) {
                if (parameter == null) {
                    continue;
                }
                ItemStack requested = parameter.getItemStack();
                if (!requested.isEmpty()) {
                    requests.add(requested.copy());
                }
            }
        }
        return requests;
    }

    @Override
    public IRequestProvider getRequestProvider() {
        Level level = level();
        if (level != null) {
            BlockPos pos = new BlockPos(x(), y(), z());
            for (Direction dir : Direction.values()) {
                BlockEntity neighbour = level.getBlockEntity(pos.relative(dir));
                if (neighbour instanceof IRequestProvider provider) {
                    return provider;
                }
            }
        }
        return this;
    }

    @Override
    public void onChunkUnload() {
        pipe = null;
    }
}
