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
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.IFluidHandler;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.IAction;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.triggers.ActionPipeDirection;

public class PipeFluidsIron extends Pipe<PipeTransportFluids> {

	protected int standardIconIndex = PipeIconProvider.TYPE.PipeFluidsIron_Standard.ordinal();
	protected int solidIconIndex = PipeIconProvider.TYPE.PipeAllIron_Solid.ordinal();
	private PipeLogicIron logic = new PipeLogicIron(this) {
		@Override
		protected boolean isValidConnectingTile(TileEntity tile) {
			if (tile instanceof TileGenericPipe) {
				Pipe otherPipe = ((TileGenericPipe) tile).pipe;
				if (otherPipe instanceof PipeFluidsWood || otherPipe instanceof PipeStructureCobblestone) {
					return false;
				} else {
					return otherPipe.transport instanceof PipeTransportFluids;
				}
			}

			return tile instanceof IFluidHandler;
		}
	};

	public PipeFluidsIron(Item item) {
		super(new PipeTransportFluids(), item);
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
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN) {
			return standardIconIndex;
		}
		if (container != null && container.getBlockMetadata() == direction.ordinal()) {
			return standardIconIndex;
		}
		return solidIconIndex;

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
