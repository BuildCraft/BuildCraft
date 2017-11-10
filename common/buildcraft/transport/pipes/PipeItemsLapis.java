/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumColor;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.pipes.events.PipeEventItem;
import buildcraft.transport.statements.ActionPipeColor;

public class PipeItemsLapis extends Pipe<PipeTransportItems> {

	public PipeItemsLapis(Item item) {
		super(new PipeTransportItems(), item);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (container == null) {
			return PipeIconProvider.TYPE.PipeItemsLapis_Black.ordinal();
		}
		return PipeIconProvider.TYPE.PipeItemsLapis_Black.ordinal() + container.getBlockMetadata();
	}

	@Override
	public boolean blockActivated(EntityPlayer player, ForgeDirection direction) {
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, container.xCoord, container.yCoord, container.zCoord)) {
			if (player.isSneaking()) {
				setColor(getColor().getPrevious());
			} else {
				setColor(getColor().getNext());
			}

			((IToolWrench) equipped).wrenchUsed(player, container.xCoord, container.yCoord, container.zCoord);
			return true;
		} else {
			int color = ColorUtils.getColorIDFromDye(player.getCurrentEquippedItem());
			if (color >= 0 && color < 16) {
				setColor(EnumColor.fromId(15 - color));
			}
		}

		return false;
	}

	public EnumColor getColor() {
		return EnumColor.fromId(container.getBlockMetadata());
	}

	public void setColor(EnumColor color) {
		if (color.ordinal() != container.getBlockMetadata()) {
			container.getWorldObj().setBlockMetadataWithNotify(container.xCoord, container.yCoord, container.zCoord, color.ordinal(), 3);
			container.scheduleRenderUpdate();
		}
	}

	public void eventHandler(PipeEventItem.ReachedCenter event) {
		event.item.color = getColor();
	}

	public void eventHandler(PipeEventItem.AdjustSpeed event) {
		event.slowdownAmount /= 4;
	}

	@Override
	protected void actionsActivated(Collection<StatementSlot> actions) {
		super.actionsActivated(actions);

		for (StatementSlot action : actions) {
			if (action.statement instanceof ActionPipeColor) {
				setColor(((ActionPipeColor) action.statement).color);
				break;
			}
		}
	}

	@Override
	public LinkedList<IActionInternal> getActions() {
		LinkedList<IActionInternal> result = super.getActions();
		result.addAll(Arrays.asList(BuildCraftTransport.actionPipeColor));

		return result;
	}
}
