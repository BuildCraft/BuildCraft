package buildcraft.energy;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.pipes.PipePowerIron;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEngineCreative extends TileEngine {

	private PipePowerIron.PowerMode powerMode = PipePowerIron.PowerMode.M2;

	@Override
	public ResourceLocation getTextureFile() {
		return CREATIVE_TEXTURE;
	}

	@Override
	protected EnergyStage computeEnergyStage() {
		return EnergyStage.RED;
	}

	@Override
	public boolean onBlockActivated(EntityPlayer player, ForgeDirection side) {
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;

		if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(player, xCoord, yCoord, zCoord)) {
			powerMode = powerMode.getNext();
			energy = 0;

			if (!getWorld().isRemote) {
				player.addChatMessage(new ChatComponentText(String.format(StringUtils.localize("chat.pipe.power.iron.mode"), powerMode.maxPower)));
			}

			((IToolWrench) equipped).wrenchUsed(player, xCoord, yCoord, zCoord);
			return true;
		}

		return false;
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
