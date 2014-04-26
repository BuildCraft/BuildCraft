package buildcraft.factory;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.network.NetworkData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;

public class TileMultiblockMaster extends TileBuildCraft {

	@NetworkData
	public boolean formed = false;

	private boolean firstRun = true;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		formed = nbt.getBoolean("formed");
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setBoolean("formed", formed);
	}

	@Override
	public void updateEntity() {
		if (worldObj != null && !worldObj.isRemote) {
			if (firstRun && formed) {
				formMultiblock(null);
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
				firstRun = false;
			}
		}
	}

	public void onBlockActivated(EntityPlayer player) {
		ItemStack stack = player.getCurrentEquippedItem();

		if (!formed && !player.isSneaking() && stack != null && stack.getItem() instanceof IToolWrench) {
			IToolWrench wrench = (IToolWrench) stack.getItem();

			if (wrench.canWrench(player, xCoord, yCoord, zCoord)) {
				formMultiblock(player);
				wrench.wrenchUsed(player, xCoord, yCoord, zCoord);
				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
			}
		} else if (formed) {
			player.addChatComponentMessage(new ChatComponentText("You activated this multi-block!"));
		}
	}

	public void formMultiblock(EntityPlayer player) {
		formed = true;
	}

	public void deformMultiblock() {
		formed = false;
	}

}
