/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.EnumFacing;
import buildcraft.BuildCraftTransport;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.BlockBuildCraft;
import buildcraft.core.PowerMode;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.gates.StatementSlot;
import buildcraft.transport.statements.ActionPowerLimiter;

public class PipePowerIron extends Pipe<PipeTransportPower> {

	public PipePowerIron(Item item) {
		super(new PipeTransportPower(), item);
		transport.initFromPipe(getClass());
	}

	@Override
	public int getIconIndex(EnumFacing direction) {
		if (container == null) {
			return PipeIconProvider.TYPE.PipePowerIronM128.ordinal();
		}
		return PipeIconProvider.TYPE.PipePowerIronM2.ordinal() + container.getBlockMetadata();
	}

	@Override
	public boolean blockActivated(EntityPlayer player) {
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, container.getPos())) {
			if (player.isSneaking()) {
				setMode(getMode().getPrevious());
			} else {
				setMode(getMode().getNext());
			}
			if (getWorld().isRemote) {
				player.addChatMessage(new ChatComponentText(String.format(
						StringUtils.localize("chat.pipe.power.iron.mode"),
						getMode().maxPower)));
			}

			((IToolWrench) equipped).wrenchUsed(player, container.getPos());
			return true;
		}

		return false;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		transport.maxPower = getMode().maxPower;
	}

	public PowerMode getMode() {
		return PowerMode.fromId(container.getBlockMetadata());
	}

	public void setMode(PowerMode mode) {
		if (mode.ordinal() != container.getBlockMetadata()) {
			//TODO: Check if that is correct
			container.getWorld().setBlockState(container.getPos(), container.getWorld().getBlockState(container.getPos()).withProperty(BlockBuildCraft.FACING_PROP, mode), 3);
			container.scheduleRenderUpdate();
		}
	}

	/*@Override
	@SideOnly(Side.CLIENT)
	public IIconProvider getIconProvider() {
		return BuildCraftTransport.instance.pipeIconProvider;
	}*/

	@Override
	protected void actionsActivated(Collection<StatementSlot> actions) {
		super.actionsActivated(actions);

		for (StatementSlot action : actions) {
			if (action.statement instanceof ActionPowerLimiter) {
				setMode(((ActionPowerLimiter) action.statement).limit);
				break;
			}
		}
	}

	@Override
	public LinkedList<IActionInternal> getActions() {
		LinkedList<IActionInternal> action = super.getActions();
		for (PowerMode mode : PowerMode.VALUES) {
			action.add(BuildCraftTransport.actionPowerLimiter[mode.ordinal()]);
		}
		return action;
	}
}
