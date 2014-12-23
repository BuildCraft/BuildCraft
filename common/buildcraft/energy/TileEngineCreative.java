/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.energy;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.EnumFacing;
import buildcraft.api.tools.IToolWrench;
import buildcraft.core.PowerMode;
import buildcraft.core.utils.StringUtils;

public class TileEngineCreative extends TileEngine {
	private PowerMode powerMode = PowerMode.M2;

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
	public boolean onBlockActivated(EntityPlayer player, EnumFacing side) {
		if (!getWorld().isRemote) {
			Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;

			if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, pos)) {
				powerMode = powerMode.getNext();
				energy = 0;

				// TODO: check
				player.addChatMessage(new ChatComponentTranslation("chat.pipe.power.iron.mode", powerMode.maxPower));

				sendNetworkUpdate();

				((IToolWrench) equipped).wrenchUsed(player, pos);
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
	public float getPistonSpeed() {
		return 0.02F * (powerMode.ordinal() + 1);
	}

	@Override
	public void engineUpdate() {
		super.engineUpdate();

		if (isRedstonePowered) {
			addEnergy(calculateCurrentOutput());
		}
	}

	@Override
	public boolean isBurning() {
		return isRedstonePowered;
	}

	@Override
	public int maxEnergyReceived() {
		return calculateCurrentOutput();
	}

	@Override
	public int maxEnergyExtracted() {
		return calculateCurrentOutput();
	}

	@Override
	public int getMaxEnergy() {
		return calculateCurrentOutput();
	}

	@Override
	public int calculateCurrentOutput() {
		return powerMode.maxPower;
	}
}
