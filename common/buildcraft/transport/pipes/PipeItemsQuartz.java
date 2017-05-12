/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import net.minecraft.item.Item;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.TravelingItem;


import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.statements.ActionPipeDirection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.LinkedList;


import buildcraft.api.core.BCLog;

public class PipeItemsQuartz extends Pipe<PipeTransportItems> {

    /*public PipeItemsQuartz(Item item) {
        super(new PipeTransportItems(), item);

    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        return PipeIconProvider.TYPE.PipeItemsQuartz.ordinal();
    }

    public void eventHandler(PipeEventItem.AdjustSpeed event) {
        event.slowdownAmount /= 4;
    }*/

    private int standardIconIndex = PipeIconProvider.TYPE.PipeItemsQuartz.ordinal();
    private int solidIconIndex = PipeIconProvider.TYPE.PipeItemsIron_Solid.ordinal();
    private PipeLogicQuartz logic = new PipeLogicQuartz(this) {
        @Override
        protected boolean isValidConnectingTile(TileEntity tile) {
            if (tile instanceof IPipeTile) {
                Pipe<?> otherPipe = (Pipe<?>) ((IPipeTile) tile).getPipe();
                if (otherPipe instanceof PipeItemsWood) {
                    return false;
                }
                if (otherPipe.transport instanceof PipeTransportItems) {
                    return true;
                }
                return false;
            }
            if (tile instanceof IInventory) {
                return true;
            }
            return false;
        }
    };

    public PipeItemsQuartz(Item item) {
        super(new PipeTransportItems(), item);

        transport.allowBouncing = true;
    }

    @Override
    public boolean blockActivated(EntityPlayer entityplayer, EnumFacing side) {
        return logic.blockActivated(entityplayer, EnumPipePart.fromFacing(side));
    }

    @Override
    public void onNeighborBlockChange(int blockId) {
        logic.switchOnRedstone();
        super.onNeighborBlockChange(blockId);
    }

    @Override
    public void onBlockPlaced() {
        logic.onBlockPlaced();
        super.onBlockPlaced();
    }

    @Override
    public void initialize() {
        logic.initialize();
        super.initialize();
    }

    @Override
    public boolean outputOpen(EnumFacing to) {
        boolean val = super.outputOpen(to) && logic.outputOpen(to);
        if(val){
        }
        return val;
    }

    @Override
    public void prepareForItemPush(TravelingItem data){
        //BCLog.logger.info("ItemPush: " + data);
        //BCLog.logger.info("Position Switched " + data.input);
        this.logic.switchPosition(data);
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        return standardIconIndex;
        /*
        if (direction == null) {
            return standardIconIndex;
        } else {
            int metadata = container.getBlockMetadata();

            if (metadata != direction.ordinal()) {
                return solidIconIndex;
            } else {
                return standardIconIndex;
            }
        }*/
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    protected void actionsActivated(Collection<StatementSlot> actions) {
        super.actionsActivated(actions);

        for (StatementSlot action : actions) {
            if (action.statement instanceof ActionPipeDirection) {
                logic.setFacing(((ActionPipeDirection) action.statement).direction);
                break;
            }
        }
    }

    @Override
    public LinkedList<IActionInternal> getActions() {
        LinkedList<IActionInternal> action = super.getActions();
        for (EnumFacing direction : EnumFacing.VALUES) {
            if (container.isPipeConnected(direction)) {
                action.add(BuildCraftTransport.actionPipeDirection[direction.ordinal()]);
            }
        }
        return action;
    }

    @Override
    public boolean canConnectRedstone() {
        return true;
    }
}
