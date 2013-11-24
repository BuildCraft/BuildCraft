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
import buildcraft.api.gates.IAction;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.utils.EnumColor;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TransportConstants;
import buildcraft.transport.TravelingItem;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.triggers.ActionPipeColor;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraftforge.common.ForgeDirection;

public class PipeItemsLapis extends Pipe<PipeTransportItems> {

	public PipeItemsLapis(int itemID) {
		super(new PipeTransportItems(), itemID);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (container == null)
			return PipeIconProvider.TYPE.PipeItemsLapis_Black.ordinal();
		return PipeIconProvider.TYPE.PipeItemsLapis_Black.ordinal() + container.getBlockMetadata();
	}

	@Override
	public boolean blockActivated(EntityPlayer player) {
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, container.xCoord, container.yCoord, container.zCoord)) {
			if (player.isSneaking()) {
				setColor(getColor().getPrevious());
			} else {
				setColor(getColor().getNext());
			}

			((IToolWrench) equipped).wrenchUsed(player, container.xCoord, container.yCoord, container.zCoord);
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
		}
	}

	public void eventHandler(PipeEventItem.ReachedCenter event) {
		event.item.color = getColor();
	}

	public void eventHandler(PipeEventItem.AdjustSpeed event) {
		event.handled = true;
		TravelingItem item = event.item;

		if (item.getSpeed() > TransportConstants.PIPE_NORMAL_SPEED) {
			item.setSpeed(item.getSpeed() - TransportConstants.PIPE_NORMAL_SPEED / 4.0F);
		}

		if (item.getSpeed() < TransportConstants.PIPE_NORMAL_SPEED) {
			item.setSpeed(TransportConstants.PIPE_NORMAL_SPEED);
		}
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
