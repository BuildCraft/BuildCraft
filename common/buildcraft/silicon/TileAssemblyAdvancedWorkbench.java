package buildcraft.silicon;

import buildcraft.core.network.PacketSlotChange;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;

public class TileAssemblyAdvancedWorkbench extends TileEntity implements IInventory {

	private ItemStack[] craftingSlots;
	private ItemStack[] storageSlots;
	private ItemStack outputSlot;
	@Override
	public int getSizeInventory() {
		return 16;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getInvName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getInventoryStackLimit() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void openChest() {
		// TODO Auto-generated method stub

	}

	@Override
	public void closeChest() {
		// TODO Auto-generated method stub

	}

	public float getRecentEnergyAverage() {
		// TODO Auto-generated method stub
		return 0;
	}

	public float getStoredEnergy() {
		// TODO Auto-generated method stub
		return 0f;
	}

	public float getRequiredEnergy() {
		// TODO Auto-generated method stub
		return 0f;
	}

	public void handleSlotChange(PacketSlotChange packet1) {
		// TODO Auto-generated method stub

	}

}
