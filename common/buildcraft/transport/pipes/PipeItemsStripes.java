/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;

import net.minecraftforge.fluids.IFluidBlock;

import cofh.api.energy.IEnergyHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesHandler.StripesHandlerType;
import buildcraft.api.transport.IStripesPipe;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.statements.ActionPipeDirection;
import buildcraft.transport.utils.TransportUtils;

public class PipeItemsStripes extends Pipe<PipeTransportItems> implements IEnergyHandler, IStripesPipe {
    private RFBattery battery = new RFBattery(320 * 50, 640, 0);
    private EnumPipePart actionDir = EnumPipePart.CENTER;

    public PipeItemsStripes(Item item) {
        super(new PipeTransportItems(), item);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (container.getWorld().isRemote) {
            return;
        }

        if (battery.getEnergyStored() >= 10) {
            EnumPipePart o = actionDir;
            if (o == EnumPipePart.CENTER) {
                o = EnumPipePart.fromFacing(getOpenOrientation());
            }

            if (o != EnumPipePart.CENTER) {
                Vec3 vec = Utils.convert(container.getPos()).add(Utils.convert(o.face));
                BlockPos veci = Utils.convertFloor(vec);

                if (!BlockUtils.isUnbreakableBlock(getWorld(), Utils.convertFloor(vec))) {
                    IBlockState state = getWorld().getBlockState(Utils.convertFloor(vec));
                    Block block = state.getBlock();

                    if (block instanceof BlockLiquid || block instanceof IFluidBlock) {
                        return;
                    }

                    ItemStack stack = new ItemStack(block, 1, block.getMetaFromState(state));
                    EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(), veci).get();

                    for (IStripesHandler handler : PipeManager.stripesHandlers) {
                        if (handler.getType() == StripesHandlerType.BLOCK_BREAK && handler.shouldHandle(stack)) {
                            if (handler.handle(getWorld(), veci, o.face, stack, player, this)) {
                                return;
                            }
                        }
                    }

                    List<ItemStack> stacks = block.getDrops(getWorld(), veci, state, 0);

                    if (stacks != null) {
                        for (ItemStack s : stacks) {
                            if (s != null) {
                                sendItem(s, o.opposite().face);
                            }
                        }
                    }

                    getWorld().setBlockToAir(veci);
                }
            }

            return;
        }
    }

    public void eventHandler(PipeEventItem.DropItem event) {
        if (container.getWorld().isRemote) {
            return;
        }

        EnumFacing direction = actionDir.face;
        if (direction == null) {
            direction = event.direction;
        }

        Vec3 p = Utils.convert(container.getPos()).add(Utils.convert(direction));

        ItemStack stack = event.entity.getEntityItem();
        EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(), Utils.convertFloor(p)).get();

        switch (direction) {
            case DOWN:
                player.rotationPitch = 90;
                player.rotationYaw = 0;
                break;
            case UP:
                player.rotationPitch = 270;
                player.rotationYaw = 0;
                break;
            case NORTH:
                player.rotationPitch = 0;
                player.rotationYaw = 180;
                break;
            case SOUTH:
                player.rotationPitch = 0;
                player.rotationYaw = 0;
                break;
            case WEST:
                player.rotationPitch = 0;
                player.rotationYaw = 90;
                break;
            case EAST:
                player.rotationPitch = 0;
                player.rotationYaw = 270;
                break;
        }

        /** Check if there's a handler for this item type. */
        for (IStripesHandler handler : PipeManager.stripesHandlers) {
            if (handler.getType() == StripesHandlerType.ITEM_USE && handler.shouldHandle(stack)) {
                if (handler.handle(getWorld(), Utils.convertFloor(p), direction, stack, player, this)) {
                    event.entity = null;
                    return;
                }
            }
        }
    }

    @Override
    public void dropItem(ItemStack itemStack, EnumFacing direction) {
        Vec3 p = Utils.convert(container.getPos()).add(Utils.convert(direction));
        InvUtils.dropItems(getWorld(), itemStack, Utils.convertFloor(p));
    }

    @Override
    public LinkedList<IActionInternal> getActions() {
        LinkedList<IActionInternal> action = super.getActions();
        for (EnumFacing direction : EnumFacing.VALUES) {
            if (!container.isPipeConnected(direction)) {
                action.add(BuildCraftTransport.actionPipeDirection[direction.ordinal()]);
            }
        }
        return action;
    }

    @Override
    protected void actionsActivated(Collection<StatementSlot> actions) {
        super.actionsActivated(actions);

        actionDir = null;

        for (StatementSlot action : actions) {
            if (action.statement instanceof ActionPipeDirection) {
                actionDir = EnumPipePart.fromFacing(((ActionPipeDirection) action.statement).direction);
                break;
            }
        }
    }

    @Override
    public void sendItem(ItemStack stack, EnumFacing direction) {
        Vec3 pos = Utils.convertMiddle(container.getPos()).addVector(0, TransportUtils.getPipeFloorOf(stack) - 0.5, 0);
        pos = pos.add(Utils.convert(direction, 0.25));

        TravelingItem newItem = TravelingItem.make(pos, stack);

        transport.injectItem(newItem, direction);
    }

    @Override
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        return PipeIconProvider.TYPE.PipeItemsStripes.ordinal();
    }

    @Override

    public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
        if (tile instanceof IPipeTile) {
            IPipeTile tilePipe = (IPipeTile) tile;

            if (tilePipe.getPipe() instanceof PipeItemsStripes) {
                return false;
            }
        }

        return super.canPipeConnect(tile, side);
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return battery.receiveEnergy(maxReceive, simulate);
    }

    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return 0;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return 10;
    }
}
