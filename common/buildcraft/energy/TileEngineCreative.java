/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;

import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.PowerMode;
import buildcraft.core.lib.engines.TileEngineBase;
import buildcraft.core.lib.utils.StringUtils;

public class TileEngineCreative extends TileEngineBase {
	private PowerMode powerMode = PowerMode.M2;

	@Override
	protected EnergyStage computeEnergyStage() {
		return EnergyStage.BLUE;
	}

	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		if (!getWorldObj().isRemote) {
			Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;

			if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, xCoord, yCoord, zCoord)) {
				powerMode = powerMode.getNext();
				energy = 0;

				if (!(player instanceof FakePlayer)) {
					if (BuildCraftCore.hidePowerNumbers) {
						player.addChatMessage(new ChatComponentTranslation("chat.pipe.power.iron.mode.numberless",
								StringUtils.localize("chat.pipe.power.iron.level." + powerMode.maxPower)));
					} else {
						player.addChatMessage(new ChatComponentTranslation("chat.pipe.power.iron.mode",
								powerMode.maxPower));
					}
				}

				sendNetworkUpdate();

				((IToolWrench) equipped).wrenchUsed(player, xCoord, yCoord, zCoord);
				return true;
			}
		}

		return !player.isSneaking();
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);

		powerMode = PowerMode.fromId(data.getByte("mode"));
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		data.setByte("mode", (byte) powerMode.ordinal());
	}

	@Override
	public void readData(ByteBuf stream) {
		super.readData(stream);
		powerMode = PowerMode.fromId(stream.readUnsignedByte());
	}

	@Override
	public void writeData(ByteBuf stream) {
		super.writeData(stream);
		stream.writeByte(powerMode.ordinal());
	}

	@Override
	public void updateHeat() {

	}

	@Override
	public float getPistonSpeed() {
		return 0.02F * (powerMode.ordinal() + 1);
	}

	@Override
	public void engineUpdate() {
		super.engineUpdate();

		if (isRedstonePowered) {
			addEnergy(getIdealOutput());
		}
	}

	@Override
	public boolean isBurning() {
		return isRedstonePowered;
	}

	@Override
	public int getMaxEnergy() {
		return getIdealOutput();
	}

	@Override
	public int getIdealOutput() {
		return powerMode.maxPower;
	}
}
