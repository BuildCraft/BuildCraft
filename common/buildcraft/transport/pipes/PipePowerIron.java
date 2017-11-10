/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.pipes;

import java.util.Collection;
import java.util.LinkedList;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.util.ChatComponentTranslation;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.statements.IActionInternal;
import buildcraft.api.statements.StatementSlot;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.PowerMode;
import buildcraft.core.lib.utils.StringUtils;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportPower;
import buildcraft.transport.statements.ActionPowerLimiter;

public class PipePowerIron extends Pipe<PipeTransportPower> {

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
	public boolean blockActivated(EntityPlayer player, ForgeDirection direction) {
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, container.xCoord, container.yCoord, container.zCoord)) {
			if (player.isSneaking()) {
				setMode(getMode().getPrevious());
			} else {
				setMode(getMode().getNext());
			}
			if (getWorld().isRemote && !(player instanceof FakePlayer)) {
				if (BuildCraftCore.hidePowerNumbers) {
					player.addChatMessage(new ChatComponentTranslation("chat.pipe.power.iron.mode.numberless",
							StringUtils.localize("chat.pipe.power.iron.level." + getMode().maxPower)));
				} else {
					player.addChatMessage(new ChatComponentTranslation("chat.pipe.power.iron.mode",
							getMode().maxPower));
				}
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
		// TODO: A bit of a hack
		for (PowerMode mode : PowerMode.VALUES) {
			action.add(BuildCraftTransport.actionPowerLimiter[mode.ordinal()]);
		}
		return action;
	}
}
