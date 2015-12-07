package buildcraft.robotics;

import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;

import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftRobotics;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.gates.IGate;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.IStatement;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.inventory.InventoryWrapper;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.pipes.PipeFluidsWood;
import buildcraft.transport.pipes.PipeItemsWood;

public class DockingStationPipe extends DockingStation implements IRequestProvider {

    private IInjectable injectablePipe = new IInjectable() {
        @Override
        public boolean canInjectItems(EnumFacing from) {
            return true;
        }

        @Override
        public int injectItem(ItemStack stack, boolean doAdd, EnumFacing from, EnumColor color) {
            if (doAdd) {
                Vec3 vec = Utils.convertMiddle(getPos()).add(Utils.convert(side, 0.2));
                TravelingItem item = TravelingItem.make(vec, stack);

                ((PipeTransportItems) ((Pipe<?>) getPipe().getPipe()).transport).injectItem(item, from);
            }
            return stack.stackSize;
        }
    };

    private IPipeTile pipe;

    public DockingStationPipe() {
        // Loading later from NBT - DO NOT TOUCH!
    }

    public DockingStationPipe(IPipeTile iPipe, EnumFacing side) {
        super(new BlockPos(iPipe.x(), iPipe.y(), iPipe.z()), side);
        pipe = iPipe;
        world = iPipe.getWorld();
    }

    public IPipeTile getPipe() {
        if (pipe == null) {
            TileEntity tile = world.getTileEntity(getPos());
            if (tile instanceof IPipeTile) {
                pipe = (IPipeTile) tile;
            }
        }

        if (pipe == null || ((TileEntity) pipe).isInvalid()) {
            // Inconsistency - remove this pipe from the registry.
            RobotManager.registryProvider.getRegistry(world).removeStation(this);
            pipe = null;
        }

        return pipe;
    }

    @Override
    public Iterable<StatementSlot> getActiveActions() {
        return new ActionIterator(getPipe().getPipe());
    }

    @Override
    public IInjectable getItemOutput() {
        if (getPipe().getPipeType() != IPipeTile.PipeType.ITEM) {
            return null;
        }

        return injectablePipe;
    }

    @Override
    public EnumPipePart getItemOutputSide() {
        return EnumPipePart.fromFacing(side().getOpposite());
    }

    @Override
    public ISidedInventory getItemInput() {
        if (getPipe().getPipeType() != IPipeTile.PipeType.ITEM) {
            return null;
        }

        if (!(getPipe().getPipe() instanceof PipeItemsWood)) {
            return null;
        }

        int meta = ((TileEntity) getPipe()).getBlockMetadata();
        EnumFacing dir = EnumFacing.getFront(meta);

        TileEntity connectedTile = getPipe().getWorld().getTileEntity(getPos().add(Utils.convertFloor(dir)));
        if (connectedTile instanceof IInventory) {
            return InventoryWrapper.getWrappedInventory(connectedTile);
        }

        return null;
    }

    @Override
    public EnumPipePart getItemInputSide() {
        if (getPipe().getPipeType() != IPipeTile.PipeType.ITEM) {
            return EnumPipePart.CENTER;
        }

        if (!(getPipe().getPipe() instanceof PipeItemsWood)) {
            return EnumPipePart.CENTER;
        }

        int meta = ((TileEntity) getPipe()).getBlockMetadata();
        return EnumPipePart.fromMeta(meta).opposite();
    }

    @Override
    public IFluidHandler getFluidInput() {
        if (getPipe().getPipeType() != IPipeTile.PipeType.FLUID) {
            return null;
        }

        if (!(getPipe().getPipe() instanceof PipeFluidsWood)) {
            return null;
        }

        int meta = ((TileEntity) getPipe()).getBlockMetadata();
        EnumFacing dir = EnumFacing.getFront(meta);

        TileEntity connectedTile = getPipe().getWorld().getTileEntity(getPos().add(Utils.convertFloor(dir)));
        if (connectedTile instanceof IFluidHandler) {
            return (IFluidHandler) connectedTile;
        }

        return null;
    }

    @Override
    public EnumPipePart getFluidInputSide() {
        if (getPipe().getPipeType() != IPipeTile.PipeType.FLUID) {
            return EnumPipePart.CENTER;
        }

        if (!(getPipe().getPipe() instanceof PipeFluidsWood)) {
            return EnumPipePart.CENTER;
        }

        int meta = ((TileEntity) getPipe()).getBlockMetadata();
        return EnumPipePart.fromMeta(meta).opposite();
    }

    @Override
    public IFluidHandler getFluidOutput() {
        if (getPipe().getPipeType() != IPipeTile.PipeType.FLUID) {
            return null;
        }

        return (IFluidHandler) ((Pipe<?>) getPipe().getPipe()).transport;
    }

    @Override
    public EnumPipePart getFluidOutputSide() {
        return EnumPipePart.CENTER;
    }

    @Override
    public boolean providesPower() {
        return getPipe().getPipeType() == IPipeTile.PipeType.POWER;
    }

    @Override
    public IRequestProvider getRequestProvider() {
        for (EnumFacing dir : EnumFacing.VALUES) {
            TileEntity nearbyTile = getPipe().getWorld().getTileEntity(getPos().add(Utils.convertFloor(dir)));
            if (nearbyTile instanceof IRequestProvider) {
                return (IRequestProvider) nearbyTile;
            }
        }
        return this;
    }

    @Override
    public boolean isInitialized() {
        if (getPipe() == null || getPipe().getPipe() == null) {
            return false;
        }
        return ((Pipe<?>) getPipe().getPipe()).isInitialized();
    }

    @Override
    public boolean take(EntityRobotBase robot) {
        boolean result = super.take(robot);
        if (result) {
            getPipe().scheduleRenderUpdate();
        }
        return result;
    }

    @Override
    public boolean takeAsMain(EntityRobotBase robot) {
        boolean result = super.takeAsMain(robot);
        if (result) {
            getPipe().scheduleRenderUpdate();
        }
        return result;
    }

    @Override
    public void unsafeRelease(EntityRobotBase robot) {
        super.unsafeRelease(robot);
        if (robotTaking() == null && getPipe() != null) {
            getPipe().scheduleRenderUpdate();
        }
    }

    @Override
    public void onChunkUnload() {
        pipe = null;
    }

    @Override
    public int getRequestsCount() {
        return 127;
    }

    @Override
    public ItemStack getRequest(int slot) {
        EnumFacing side = EnumFacing.values()[(slot & 0x70) >> 4];
        int action = (slot & 0xc) >> 2;
        int param = slot & 0x3;
        IGate gate = getPipe().getPipe().getGate(side);
        if (gate == null) {
            return null;
        }

        List<IStatement> actions = gate.getActions();
        if (actions.size() <= action) {
            return null;
        }

        if (actions.get(action) != BuildCraftRobotics.actionStationRequestItems) {
            return null;
        }

        List<StatementSlot> activeActions = gate.getActiveActions();

        StatementSlot slotStmt = null;
        for (StatementSlot stmt : activeActions) {
            if (stmt.statement == actions.get(action)) {
                slotStmt = stmt;
                break;
            }
        }
        if (slotStmt == null) {
            return null;
        }
        if (slotStmt.parameters.length <= param) {
            return null;
        }

        if (slotStmt.parameters[param] == null) {
            return null;
        }

        return slotStmt.parameters[param].getItemStack();
    }

    @Override
    public ItemStack offerItem(int slot, ItemStack stack) {
        int consumed = injectablePipe.injectItem(stack, true, side.getOpposite(), null);
        if (stack.stackSize > consumed) {
            ItemStack newStack = stack.copy();
            newStack.stackSize -= consumed;
            return newStack;
        }
        return null;
    }
}
