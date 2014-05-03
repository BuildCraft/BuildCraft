/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.LinkedList;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.IAction;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.triggers.ActionPipeDirection;

public class PipeItemsIron extends Pipe<PipeTransportItems> {

	private int standardIconIndex = PipeIconProvider.TYPE.PipeItemsIron_Standard.ordinal();
	private int solidIconIndex = PipeIconProvider.TYPE.PipeAllIron_Solid.ordinal();
	private PipeLogicIron logic = new PipeLogicIron(this) {
		@Override
		protected boolean isValidConnectingTile(TileEntity tile) {
			if (tile instanceof TileGenericPipe) {
				Pipe otherPipe = ((TileGenericPipe) tile).pipe;
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

	public PipeItemsIron(Item item) {
		super(new PipeTransportItems(), item);

		transport.allowBouncing = true;
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		return logic.blockActivated(entityplayer);
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
	public boolean outputOpen(ForgeDirection to) {
		return super.outputOpen(to) && logic.outputOpen(to);
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN) {
			return standardIconIndex;
		} else {
			int metadata = container.getBlockMetadata();

			if (metadata != direction.ordinal()) {
				return solidIconIndex;
			} else {
				return standardIconIndex;
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	protected void actionsActivated(Map<IAction, Boolean> actions) {
		super.actionsActivated(actions);

		for (Map.Entry<IAction, Boolean> action : actions.entrySet()) {
			if (action.getKey() instanceof ActionPipeDirection && action.getValue() != null && action.getValue()) {
				logic.setFacing(((ActionPipeDirection) action.getKey()).direction);
				break;
			}
		}
	}

	@Override
	public LinkedList<IAction> getActions() {
		LinkedList<IAction> action = super.getActions();
		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS) {
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
