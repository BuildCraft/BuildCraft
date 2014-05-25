/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.NetworkData;
import buildcraft.api.mj.IBatteryObject;
import buildcraft.api.mj.MjAPI;
import buildcraft.api.mj.MjBattery;
import buildcraft.api.power.IPowerEmitter;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.TileBuffer;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.utils.StringUtils;

public class TileEnergyConverter extends TileBuildCraft implements IPowerReceptor, IPowerEmitter {
	private static enum Mode {
		FromOldToNew("from_old_to_new"), FromNewToOld("from_new_to_old");
		private final String localizeName;

		Mode(String localizeName) {
			this.localizeName = localizeName;
		}

		public Mode next() {
			if (this == FromOldToNew) {
				return FromNewToOld;
			}
			return FromOldToNew;
		}
	}

	@MjBattery(maxCapacity = 1000, maxReceivedPerCycle = 64, minimumConsumption = 1)
	@NetworkData
	private double mjStored = 0;
	@NetworkData
	private Mode mode = Mode.FromOldToNew;
	private TileBuffer[] tileCache;
	private PowerHandler powerHandler, zeroPowerHandler;

	public TileEnergyConverter() {
		powerHandler = new PowerHandler(this, PowerHandler.Type.MACHINE, MjAPI.getMjBattery(this));
		zeroPowerHandler = new PowerHandler(this, PowerHandler.Type.MACHINE);
		zeroPowerHandler.configure(0, 0, 0, 0);
	}

	public static String getLocalizedModeName(ItemStack stack) {
		int mode = 0;
		if (stack != null && stack.getTagCompound() != null) {
			mode = stack.getTagCompound().getInteger("converterMode");
			if (mode >= Mode.values().length || mode < 0) {
				mode = 0;
			}
		}
		return StringUtils.localize("chat.pipe.power.energyConverter." + Mode.values()[mode].localizeName);
	}

	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		if (!getWorld().isRemote) {
			Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;

			if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, xCoord, yCoord, zCoord)) {
				mode = mode.next();

				player.addChatMessage(new ChatComponentText(String.format(
						StringUtils.localize("chat.pipe.power.energyConverter"),
						StringUtils.localize("chat.pipe.power.energyConverter." + mode.localizeName))));

				sendNetworkUpdate();

				((IToolWrench) equipped).wrenchUsed(player, xCoord, yCoord, zCoord);
				return true;
			}
		}

		return !player.isSneaking();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setDouble("mjStored", mjStored);
		nbt.setInteger("converterMode", mode.ordinal());
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		mjStored = nbt.getDouble("mjStored");
		mode = Mode.values()[nbt.getInteger("converterMode")];
	}

	public TileBuffer getTileBuffer(ForgeDirection side) {
		if (tileCache == null) {
			tileCache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		}

		return tileCache[side.ordinal()];
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (mode == Mode.FromOldToNew) {
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = getTileBuffer(side).getTile();
				if (tile instanceof TileEnergyConverter) {
					continue;
				}
				IBatteryObject object = MjAPI.getMjBattery(tile, MjAPI.DEFAULT_POWER_FRAMEWORK, side.getOpposite());
				if (object != null && mjStored > 0) {
					double wantToUse = Math.min(mjStored, object.getEnergyRequested());
					object.addEnergy(wantToUse);
					mjStored -= wantToUse;
				}
			}
		} else if (mode == Mode.FromNewToOld) {
			for (ForgeDirection side : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = getTileBuffer(side).getTile();
				if (tile instanceof TileEnergyConverter) {
					continue;
				}
				if (tile instanceof IPowerReceptor && mjStored > 0) {
					IPowerReceptor receptor = (IPowerReceptor) tile;
					PowerHandler.PowerReceiver powerReceiver = receptor.getPowerReceiver(side.getOpposite());
					if (powerReceiver == null) {
						continue;
					}
					double wantToUse = Math.min(mjStored, powerReceiver.getMaxEnergyReceived());
					if (wantToUse > powerReceiver.getMinEnergyReceived()) {
						powerReceiver.receiveEnergy(PowerHandler.Type.MACHINE, wantToUse, side);
						mjStored -= wantToUse;
					}
				}
			}
		}
	}

	@Override
	public PowerHandler.PowerReceiver getPowerReceiver(ForgeDirection side) {
		return (mode == Mode.FromOldToNew ? powerHandler : zeroPowerHandler).getPowerReceiver();
	}

	@Override
	public void doWork(PowerHandler workProvider) {
	}

	@Override
	public boolean canEmitPowerFrom(ForgeDirection side) {
		return mode == Mode.FromOldToNew;
	}
}
