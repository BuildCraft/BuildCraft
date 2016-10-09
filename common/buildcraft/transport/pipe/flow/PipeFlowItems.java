package buildcraft.transport.pipe.flow;

import java.util.*;

import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.core.IStackFilter;
import buildcraft.api.inventory.IItemTransactor;
import buildcraft.lib.inventory.ItemTransactorHelper;
import buildcraft.lib.inventory.NoSpaceTransactor;
import buildcraft.lib.misc.ParticleUtil;
import buildcraft.lib.misc.data.DelayedList;
import buildcraft.transport.api_move.IFlowItems;
import buildcraft.transport.api_move.IPipe;
import buildcraft.transport.api_move.IPipe.ConnectedType;
import buildcraft.transport.api_move.IPipeHolder;
import buildcraft.transport.api_move.PipeFlow;

public class PipeFlowItems extends PipeFlow implements IFlowItems {
    private double targetSpeed = 0.05;
    private double speedDelta = 0.01;
    private boolean allowBouncing = false;

    private final DelayedList<TravellingItem> items = new DelayedList<>();

    public PipeFlowItems(IPipe pipe) {
        super(pipe);
    }

    public PipeFlowItems(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
    }

    // IFlow Items

    @Override
    public void setTargetSpeed(double targetSpeed) {
        this.targetSpeed = targetSpeed;
    }

    @Override
    public double getTargetSpeed() {
        return this.targetSpeed;
    }

    @Override
    public void setSpeedDelta(double speedDelta) {
        this.speedDelta = speedDelta;
    }

    @Override
    public double getSpeedDelta() {
        return this.speedDelta;
    }

    @Override
    public int tryExtractStack(int count, EnumFacing from, IStackFilter filter) {
        TileEntity tile = pipe.getConnectedTile(from);
        IItemTransactor trans = ItemTransactorHelper.getTransactor(tile, from.getOpposite());
        ItemStack stack = trans.extract(filter, 0, count, false);
        if (stack != null && stack.stackSize > 0) {
            insertItem(stack, null, targetSpeed, from);
            return stack.stackSize;
        }

        return 0;
    }

    // PipeFlow

    @Override
    public boolean canConnect(EnumFacing face, PipeFlow other) {
        return other instanceof PipeFlowItems;
    }

    @Override
    public boolean canConnect(EnumFacing face, TileEntity oTile) {
        return ItemTransactorHelper.getTransactor(oTile, face.getOpposite()) != NoSpaceTransactor.INSTANCE;
    }

    @Override
    public void onTick() {
        World world = pipe.getHolder().getPipeWorld();

        List<TravellingItem> toTick;
        synchronized (items) {
            toTick = items.advance();
        }

        if (world.isRemote) {
            return;
        }

        for (TravellingItem item : toTick) {
            EnumFacing to = item.to;
            if (to == null) {
                // TODO: fire drop event
                dropItem(item);
            } else {
                ConnectedType type = pipe.getConnectedType(to);

                if (type == ConnectedType.PIPE) {
                    IPipe oPipe = pipe.getConnectedPipe(to);
                    PipeFlow flow = oPipe.getFlow();

                    // TODO: Replace with interface for inserting
                    if (flow instanceof PipeFlowItems) {
                        PipeFlowItems oItemFlow = (PipeFlowItems) flow;
                        double nSpeed = item.speed;
                        if (nSpeed < targetSpeed) {
                            nSpeed += speedDelta;
                            if (nSpeed > targetSpeed) {
                                nSpeed = targetSpeed;
                            }
                        } else if (nSpeed > targetSpeed) {
                            nSpeed -= speedDelta;
                            if (nSpeed < targetSpeed) {
                                nSpeed = targetSpeed;
                            }
                        }
                        oItemFlow.insertItem(item.stack, item.colour, nSpeed, to.getOpposite());
                    } else {
                        dropItem(item);
                    }

                } else if (type == ConnectedType.TILE) {
                    TileEntity tile = pipe.getConnectedTile(to);
                    IItemTransactor trans = ItemTransactorHelper.getTransactor(tile, to.getOpposite());
                    ItemStack leftOver = trans.insert(item.stack, false, false);
                    dropItem(item, leftOver);
                } else {
                    // Something went wrong???
                    dropItem(item);
                }

                // TODO: Inform client
            }
        }
    }

    private void dropItem(TravellingItem item) {
        dropItem(item, item.stack);
    }

    private void dropItem(TravellingItem item, ItemStack stack) {
        if (stack == null) {
            return;
        }

        EnumFacing to = item.from.getOpposite();
        IPipeHolder holder = pipe.getHolder();
        World world = holder.getPipeWorld();
        BlockPos pos = holder.getPipePos();

        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;

        EntityItem ent = new EntityItem(world, x, y, z, stack);

        ent.motionX = to.getFrontOffsetX() * 0.04;
        ent.motionY = to.getFrontOffsetY() * 0.04;
        ent.motionZ = to.getFrontOffsetZ() * 0.04;

        world.spawnEntityInWorld(ent);
    }

    public void insertItem(ItemStack stack, EnumDyeColor colour, double speed, EnumFacing from) {
        TravellingItem item = new TravellingItem(this, stack);

        World world = pipe.getHolder().getPipeWorld();
        long now = world.getTotalWorldTime();

        item.from = from;
        item.speed = speed;
        item.colour = colour;

        item.to = getItemDest(item);

        double dist = getPipeLength(item.from);

        if (item.to != null) {
            dist += getPipeLength(item.to);
        }

        item.genTimings(now, dist);

        items.add((int) (item.tickFinished - now), item);
    }

    private double getPipeLength(EnumFacing to) {
        // TODO: Check the length between this pipes centre and the next block along
        return 0.5;
    }

    private EnumFacing getItemDest(TravellingItem item) {
        Set<EnumFacing> possible = EnumSet.allOf(EnumFacing.class);
        possible.remove(item.from);

        for (EnumFacing to : EnumFacing.VALUES) {
            if (to == item.from) {
                continue;
            }
            ConnectedType type = pipe.getConnectedType(to);
            if (type == null) {
                possible.remove(to);
            }
        }

        // fire event

        List<EnumFacing> poss = new ArrayList<>(possible);
        // TODO: replace this with a random.nextInt() for the index used
        Collections.shuffle(poss);

        if (poss.size() > 0) {
            return poss.get(0);
        } else if (allowBouncing) {
            return item.from;
        } else {
            return null;
        }
    }

    @SideOnly(Side.CLIENT)
    public List<TravellingItem> getAllItemsForRender() {
        synchronized (items) {
            List<TravellingItem> all = new ArrayList<>();
            for (List<TravellingItem> innerList : items.getAllElements()) {
                all.addAll(innerList);
            }
            return all;
        }
    }
}
