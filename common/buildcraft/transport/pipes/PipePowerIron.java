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
import net.minecraft.util.ChatComponentText;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.gates.IAction;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.triggers.ActionPowerLimiter;

public class PipePowerIron extends Pipe<PipeTransportPower> {

	public static enum PowerMode {

		M2(2), M4(4), M8(8), M16(16), M32(32), M64(64), M128(128);
		public static final PowerMode[] VALUES = values();
		public final int maxPower;

		private PowerMode(int max) {
			this.maxPower = max;
		}

		public PowerMode getNext() {
			PowerMode next = VALUES[(ordinal() + 1) % VALUES.length];
			return next;
		}

		public PowerMode getPrevious() {
			PowerMode previous = VALUES[(ordinal() + VALUES.length - 1) % VALUES.length];
			return previous;
		}

		public static PowerMode fromId(int id) {
			if (id < 0 || id >= VALUES.length) {
				return M128;
			}
			return VALUES[id];
		}
	}

	public PipePowerIron(Item item) {
		super(new PipeTransportPower(), item);
		transport.initFromPipe(getClass());
	}

	@Override
	public int getIconIndex(ForgeDirection direction) {
		if (container == null) {
			return PipeIconProvider.TYPE.PipePowerIronM128.ordinal();
		}
		return PipeIconProvider.TYPE.PipePowerIronM2.ordinal() + container.getBlockMetadata();
	}

	@Override
	public boolean blockActivated(EntityPlayer player) {
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, container.xCoord, container.yCoord, container.zCoord)) {
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

			((IToolWrench) equipped).wrenchUsed(player, container.xCoord, container.yCoord, container.zCoord);
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
			container.getWorldObj().setBlockMetadataWithNotify(container.xCoord, container.yCoord, container.zCoord, mode.ordinal(), 3);
			container.scheduleRenderUpdate();
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
			if (action.getKey() instanceof ActionPowerLimiter && action.getValue() == Boolean.TRUE) {
				setMode(((ActionPowerLimiter) action.getKey()).limit);
				break;
			}
		}
	}

	@Override
	public LinkedList<IAction> getActions() {
		LinkedList<IAction> action = super.getActions();
		for (PowerMode mode : PowerMode.VALUES) {
			action.add(BuildCraftTransport.actionPowerLimiter[mode.ordinal()]);
		}
		return action;
	}
}
