package buildcraft.silicon;

import java.util.Arrays;

import buildcraft.api.core.Orientations;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.SimpleInventory;
import buildcraft.core.utils.Utils;
import net.minecraft.src.Container;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.TileEntity;

public class TileAssemblyAdvancedWorkbench extends TileEntity implements IInventory, ILaserTarget {
	public TileAssemblyAdvancedWorkbench() {
		craftingSlots = new SimpleInventory(9, "CraftingSlots", 1);
		storageSlots = new ItemStack[27];
	}

	private SimpleInventory craftingSlots;
	private ItemStack[] storageSlots;
	private ItemStack outputSlot;

	private float storedEnergy;
	private boolean craftable;

	@Override
	public int getSizeInventory() {
		return 27 + 9 + 1;
	}

	@Override
	public ItemStack getStackInSlot(int var1) {
		if (var1 < storageSlots.length)
		{
			return storageSlots[var1];
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int var1, int var2) {
		if (var1 < storageSlots.length && storageSlots[var1]!=null)
		{
            ItemStack var3;

            if (this.storageSlots[var1].stackSize <= var2)
            {
                var3 = this.storageSlots[var1];
                this.storageSlots[var1] = null;
                this.onInventoryChanged();
                return var3;
            }
            else
            {
                var3 = this.storageSlots[var1].splitStack(var2);

                if (this.storageSlots[var1].stackSize == 0)
                {
                    this.storageSlots[var1] = null;
                }

                this.onInventoryChanged();
                return var3;
            }

		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int var1) {
		if (var1 >= storageSlots.length) return null;
        if (this.storageSlots[var1] != null)
        {
            ItemStack var2 = this.storageSlots[var1];
            this.storageSlots[var1] = null;
            return var2;
        }
        else
        {
            return null;
        }
	}

	@Override
	public void setInventorySlotContents(int var1, ItemStack var2) {
		if (var1 >= storageSlots.length) return;
        this.storageSlots[var1] = var2;

        if (var2 != null && var2.stackSize > this.getInventoryStackLimit())
        {
            var2.stackSize = this.getInventoryStackLimit();
        }

        this.onInventoryChanged();
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
        NBTTagList var2 = new NBTTagList();

        for (int var3 = 0; var3 < this.storageSlots.length; ++var3)
        {
            if (this.storageSlots[var3] != null)
            {
                NBTTagCompound var4 = new NBTTagCompound();
                var4.setByte("Slot", (byte)var3);
                this.storageSlots[var3].writeToNBT(var4);
                var2.appendTag(var4);
            }
        }

        par1nbtTagCompound.setTag("StorageSlots", var2);
        craftingSlots.writeToNBT(par1nbtTagCompound);
        par1nbtTagCompound.setFloat("StoredEnergy", storedEnergy);
	}

	@Override
	public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
		super.readFromNBT(par1nbtTagCompound);
        NBTTagList var2 = par1nbtTagCompound.getTagList("StorageSlots");
        this.storageSlots = new ItemStack[27];

        for (int var3 = 0; var3 < var2.tagCount(); ++var3)
        {
            NBTTagCompound var4 = (NBTTagCompound)var2.tagAt(var3);
            int var5 = var4.getByte("Slot") & 255;

            if (var5 >= 0 && var5 < this.storageSlots.length)
            {
                this.storageSlots[var5] = ItemStack.loadItemStackFromNBT(var4);
            }
        }
        craftingSlots.readFromNBT(par1nbtTagCompound);
        storedEnergy = par1nbtTagCompound.getFloat("StoredEnergy");
        updateCraftingResults();
	}
	@Override
	public String getInvName() {
		return "AdvancedWorkbench";
	}

	@Override
	public void onInventoryChanged() {
		super.onInventoryChanged();
		craftable = outputSlot!=null;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer var1) {
		return worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
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
		return outputSlot != null ? 100f : 0f;
	}

	@Override
	public void updateEntity() {
		if (!CoreProxy.proxy.isSimulating(worldObj)) {
			return;
		}
		while (storedEnergy >= getRequiredEnergy() && craftable)
		{
			ItemStack[] tempStorage = Arrays.copyOf(storageSlots, storageSlots.length);
			for (int j = 0; j < craftingSlots.getSizeInventory(); j++)
			{
				if (craftingSlots.getStackInSlot(j) == null)
				{
					continue;
				}
				boolean matchedStorage = false;
				for (int i = 0; i < tempStorage.length; i++)
				{
					if (craftingSlots.getStackInSlot(j)!=null && tempStorage[i]!=null && craftingSlots.getStackInSlot(j).isItemEqual(tempStorage[i]))
					{
						tempStorage[i] = Utils.consumeItem(tempStorage[i]);
						matchedStorage = true;
						break;
					}
				}
				if (!matchedStorage)
				{
					craftable = false;
					return;
				}
			}
			storageSlots = tempStorage;
			storedEnergy -= getRequiredEnergy();
			ItemStack output = outputSlot.copy();
			boolean putToPipe = Utils.addToRandomPipeEntry(this, Orientations.Unknown, output);
			if (!putToPipe)
			{
				for (int i = 0; i < storageSlots.length; i++)
				{
					if (output.stackSize == 0) {
						break;
					}
					if (storageSlots[i]!=null && output.isStackable() && output.isItemEqual(storageSlots[i]))
					{
						storageSlots[i].stackSize += output.stackSize;
						if (storageSlots[i].stackSize > output.getMaxStackSize())
						{
							output.stackSize = storageSlots[i].stackSize - output.getMaxStackSize();
							storageSlots[i].stackSize = output.getMaxStackSize();
						}
					} else if (storageSlots[i] == null) {
						storageSlots[i] = output;
						output.stackSize = 0;
					}
				}
				if (output.stackSize > 0)
				{
					Utils.dropItems(worldObj, output, xCoord, yCoord, zCoord);
				}
			}
		}
	}
	public void updateCraftingMatrix(int slot, ItemStack stack) {
		craftingSlots.setInventorySlotContents(slot, stack);
		updateCraftingResults();
		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			PacketSlotChange packet = new PacketSlotChange(PacketIds.ADVANCED_WORKBENCH_SETSLOT, xCoord, yCoord, zCoord, slot, stack);
			CoreProxy.proxy.sendToServer(packet.getPacket());
		}
	}

	private void updateCraftingResults() {
		outputSlot = CraftingManager.getInstance().findMatchingRecipe(new InventoryCrafting(
				new Container() {
					@Override
					public boolean canInteractWith(EntityPlayer var1) {
						return false;
					}
		},3,3)
		{
			@Override
			public ItemStack getStackInSlot(int par1) {
				return craftingSlots.getStackInSlot(par1);
			}
		});
		onInventoryChanged();
	}

	public IInventory getCraftingSlots() {
		return craftingSlots;
	}

	public ItemStack getOutputSlot() {
		return outputSlot;
	}

	public void setOutputSlot(ItemStack par2ItemStack) {
		this.outputSlot = par2ItemStack;
	}

	@Override
	public boolean hasCurrentWork() {
		return craftable;
	}

	@Override
	public void receiveLaserEnergy(float energy) {
		storedEnergy += energy;
	}

	@Override
	public int getXCoord() {
		return xCoord;
	}

	@Override
	public int getYCoord() {
		return yCoord;
	}

	@Override
	public int getZCoord() {
		return zCoord;
	}

}
