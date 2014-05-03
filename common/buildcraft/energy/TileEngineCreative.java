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
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.NetworkData;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.pipes.PipePowerIron;

public class TileEngineCreative extends TileEngine {

	@NetworkData
	private PipePowerIron.PowerMode powerMode = PipePowerIron.PowerMode.M2;

	@Override
	public ResourceLocation getBaseTexture() {
		return BASE_TEXTURES[3];
	}

	@Override
	public ResourceLocation getChamberTexture() {
		return CHAMBER_TEXTURES[3];
	}

	@Override
	public ResourceLocation getTrunkTexture(EnergyStage stage) {
		return TRUNK_TEXTURES[3];
	}

	@Override
	protected EnergyStage computeEnergyStage() {
		return EnergyStage.BLUE;
	}

	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		if (!getWorld().isRemote) {
			Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;

			if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, xCoord, yCoord, zCoord)) {
				powerMode = powerMode.getNext();
				energy = 0;

				player.addChatMessage(new ChatComponentText(String.format(StringUtils.localize("chat.pipe.power.iron.mode"), powerMode.maxPower)));

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

		powerMode = PipePowerIron.PowerMode.fromId(data.getByte("mode"));
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);

		data.setByte("mode", (byte) powerMode.ordinal());
	}

	@Override
	public float getPistonSpeed() {
		return 0.02F * (powerMode.ordinal() + 1);
	}

	@Override
	public void engineUpdate() {
		super.engineUpdate();

		if (isRedstonePowered) {
			addEnergy(getCurrentOutput());
		}
	}

	@Override
	public boolean isBurning() {
		return isRedstonePowered;
	}

	@Override
	public int getScaledBurnTime(int scale) {
		return 0;
	}

	@Override
	public double maxEnergyReceived() {
		return getCurrentOutput();
	}

	@Override
	public double maxEnergyExtracted() {
		return getCurrentOutput();
	}

	@Override
	public double getMaxEnergy() {
		return getCurrentOutput();
	}

	@Override
	public double getCurrentOutput() {
		return powerMode.maxPower;
	}

	@Override
	public float explosionRange() {
		return 0;
	}

}
