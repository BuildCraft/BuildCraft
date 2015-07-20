package buildcraft.robotics;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.api.enums.EnumColor;
import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.RobotManager;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IInjectable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.gates.ActionIterator;
import buildcraft.transport.pipes.PipeFluidsWood;
import buildcraft.transport.pipes.PipeItemsWood;

public class DockingStationPipe extends DockingStation {

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
        // Loading later from NBT
    }

    public DockingStationPipe(IPipeTile iPipe, EnumFacing side) {
        super(new BlockPos(iPipe.x(), iPipe.y(), iPipe.z()), side);
        pipe = iPipe;
        world = iPipe.getWorld();
    }

    public IPipeTile getPipe() {
        if (pipe == null) {
            pipe = (IPipeTile) world.getTileEntity(getPos());
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
    public IInventory getItemInput() {
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
            return (IInventory) connectedTile;
        }

        return null;
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
    public IFluidHandler getFluidOutput() {
        if (getPipe().getPipeType() != IPipeTile.PipeType.FLUID) {
            return null;
        }

        return (IFluidHandler) ((Pipe<?>) getPipe().getPipe()).transport;
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
        return null;
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
        if (robotTaking() == null) {
            getPipe().scheduleRenderUpdate();
        }
    }

}
