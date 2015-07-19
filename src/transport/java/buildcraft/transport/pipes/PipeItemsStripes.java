/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import cofh.api.energy.IEnergyHandler;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.WorldServer;

import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IStripesHandler;
import buildcraft.api.transport.IStripesHandler.StripesHandlerType;
import buildcraft.api.transport.IStripesPipe;
import buildcraft.api.transport.PipeManager;
import buildcraft.core.lib.inventory.InvUtils;
import buildcraft.core.lib.utils.BlockUtils;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.transport.BuildCraftTransport;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.statements.ActionPipeDirection;
import buildcraft.transport.utils.TransportUtils;

public class PipeItemsStripes extends Pipe<PipeTransportItems> implements IEnergyHandler, IStripesPipe {
    private EnumFacing actionDir = null;

    public PipeItemsStripes(Item item) {
        super(new PipeTransportItems(), item);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (container.getWorldObj().isRemote) {
            return;
        }
    }

    public void eventHandler(PipeEventItem.DropItem event) {
        if (container.getWorldObj().isRemote) {
            return;
        }

        EnumFacing direction = actionDir;
        if (direction == null) {
            direction = event.direction;
        }

        Vec3 p = new Vec3(container.xCoord, container.yCoord, container.zCoord, direction);
        p.moveForwards(1.0);

        ItemStack stack = event.entity.getEntityItem();
        EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(), (int) p.x, (int) p.y, (int) p.z).get();

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
            case UNKNOWN:
                break;
        }

        /** Check if there's a handler for this item type. */
        for (IStripesHandler handler : PipeManager.stripesHandlers) {
            if (handler.getType() == StripesHandlerType.ITEM_USE && handler.shouldHandle(stack)) {
                if (handler.handle(getWorld(), (int) p.x, (int) p.y, (int) p.z, direction, stack, player, this)) {
                    event.entity = null;
                    return;
                }
            }
        }
    }

    @Override
    public void dropItem(ItemStack itemStack, EnumFacing direction) {
        Vec3 p = new Vec3(container.xCoord, container.yCoord, container.zCoord, direction);
        p.moveForwards(1.0);

        InvUtils.dropItems(getWorld(), itemStack, (int) p.x, (int) p.y, (int) p.z);
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
                actionDir = ((ActionPipeDirection) action.statement).direction;
                break;
            }
        }
    }

    @Override
    public void sendItem(ItemStack itemStack, EnumFacing direction) {
        Vec3 pos =
            new Vec3(container.xCoord + 0.5, container.yCoord + TransportUtils.getPipeFloorOf(itemStack), container.zCoord + 0.5, direction);
        pos.moveBackwards(0.25D);

        TravelingItem newItem = TravelingItem.make(pos.x, pos.y, pos.z, itemStack);
        transport.injectItem(newItem, direction);
    }

    @Override
    public TextureAtlasSpriteProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        return PipeIconProvider.TYPE.Stripes.ordinal();
    }

    @Override
    public boolean canPipeConnect(TileEntity tile, EnumFacing side) {
        if (tile instanceof TileGenericPipe) {
            TileGenericPipe tilePipe = (TileGenericPipe) tile;

            if (tilePipe.pipe instanceof PipeItemsStripes) {
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
        if (maxReceive == 0) {
            return 0;
        } else if (simulate) {
            return maxReceive;
        }

        EnumFacing o = actionDir;
        if (o == null) {
            o = getOpenOrientation();
        }

        if (o != null) {
            Vec3 p = new Vec3(container.xCoord, container.yCoord, container.zCoord, o);
            p.moveForwards(1.0);

            if (!BlockUtils.isUnbreakableBlock(getWorld(), (int) p.x, (int) p.y, (int) p.z)) {
                Block block = getWorld().getBlock((int) p.x, (int) p.y, (int) p.z);
                int metadata = getWorld().getBlockMetadata((int) p.x, (int) p.y, (int) p.z);

                ItemStack stack = new ItemStack(block, 1, metadata);
                EntityPlayer player = CoreProxy.proxy.getBuildCraftPlayer((WorldServer) getWorld(), (int) p.x, (int) p.y, (int) p.z).get();

                for (IStripesHandler handler : PipeManager.stripesHandlers) {
                    if (handler.getType() == StripesHandlerType.BLOCK_BREAK && handler.shouldHandle(stack)) {
                        if (handler.handle(getWorld(), (int) p.x, (int) p.y, (int) p.z, o, stack, player, this)) {
                            return maxReceive;
                        }
                    }
                }

                ArrayList<ItemStack> stacks = block.getDrops(getWorld(), (int) p.x, (int) p.y, (int) p.z, metadata, 0);

                if (stacks != null) {
                    for (ItemStack s : stacks) {
                        if (s != null) {
                            sendItem(s, o.getOpposite());
                        }
                    }
                }

                getWorld().setBlockToAir((int) p.x, (int) p.y, (int) p.z);
            }
        }

        return maxReceive;
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
