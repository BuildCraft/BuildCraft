/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.Position;
import buildcraft.api.gates.IAction;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.utils.EnumColor;
import buildcraft.core.utils.Utils;
import buildcraft.transport.EntityData;
import buildcraft.transport.IItemTravelingHook;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.triggers.ActionPipeColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsLapis extends Pipe implements IItemTravelingHook, IPipeTransportItemsHook {

	public PipeItemsLapis(int itemID) {
		super(new PipeTransportItems(), itemID);
		((PipeTransportItems) transport).travelHook = this;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (direction == ForgeDirection.UNKNOWN)
			return PipeIconProvider.TYPE.PipeItemsLapis_White.ordinal();
		return PipeIconProvider.TYPE.PipeItemsLapis_Black.ordinal() + container.getBlockMetadata();
	}

	@Override
	public boolean blockActivated(EntityPlayer entityplayer) {
		Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, container.xCoord, container.yCoord, container.zCoord)) {
			setColor(getColor().getNext());
			((IToolWrench) equipped).wrenchUsed(entityplayer, container.xCoord, container.yCoord, container.zCoord);
			return true;
		}

		return false;
	}

	public EnumColor getColor() {
		return EnumColor.fromId(container.getBlockMetadata());
	}

	public void setColor(EnumColor color) {
		if (color.ordinal() != container.getBlockMetadata()) {
			container.worldObj.setBlockMetadataWithNotify(container.xCoord, container.yCoord, container.zCoord, color.ordinal(), 3);
			container.scheduleRenderUpdate();
			container.markBlockForUpdate();
		}
	}

	@Override
	public void drop(PipeTransportItems transport, EntityData data) {
	}

	@Override
	public void centerReached(PipeTransportItems transport, EntityData data) {
		data.color = getColor();
	}

	@Override
	public void endReached(PipeTransportItems pipe, EntityData data, TileEntity tile) {
	}

	@Override
	public void readjustSpeed(IPipedItem item) {
		if (item.getSpeed() > Utils.pipeNormalSpeed) {
			item.setSpeed(item.getSpeed() - Utils.pipeNormalSpeed / 4.0F);
		}

		if (item.getSpeed() < Utils.pipeNormalSpeed) {
			item.setSpeed(Utils.pipeNormalSpeed);
		}
	}

	@Override
	public LinkedList<ForgeDirection> filterPossibleMovements(LinkedList<ForgeDirection> possibleOrientations, Position pos, IPipedItem item) {
		return possibleOrientations;
	}

	@Override
	public void entityEntered(IPipedItem item, ForgeDirection orientation) {
	}

	@Override
	protected void actionsActivated(Map<IAction, Boolean> actions) {
		super.actionsActivated(actions);

		for (Entry<IAction, Boolean> action : actions.entrySet()) {
			if (action.getKey() instanceof ActionPipeColor && action.getValue() != null && action.getValue()) {
				setColor(((ActionPipeColor) action.getKey()).color);
				break;
			}
		}
	}

	@Override
	public LinkedList<IAction> getActions() {
		LinkedList<IAction> result = super.getActions();
		result.addAll(Arrays.asList(BuildCraftTransport.actionPipeColor));

		return result;
	}
}
