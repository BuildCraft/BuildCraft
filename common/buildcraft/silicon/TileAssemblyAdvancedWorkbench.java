package buildcraft.silicon;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import buildcraft.api.core.Orientations;
import buildcraft.core.IMachine;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.SimpleInventory;
import buildcraft.core.utils.Utils;
import net.minecraft.src.ChunkCoordinates;
import net.minecraft.src.Container;
import net.minecraft.src.CraftingManager;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ICrafting;
import net.minecraft.src.IInventory;
import net.minecraft.src.InventoryCraftResult;
import net.minecraft.src.InventoryCrafting;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.NBTTagList;
import net.minecraft.src.SlotCrafting;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;

public class TileAssemblyAdvancedWorkbench extends TileEntity implements IInventory, ILaserTarget, IMachine {
	private final class InternalInventoryCraftingContainer extends Container {
		@Override
		public boolean canInteractWith(EntityPlayer var1) {
			return false;
		}
	}

	private final class InternalInventoryCrafting extends InventoryCrafting {
		int[] bindings = new int[9];
		ItemStack[] tempStacks;
		public int[] hitCount;
		private InternalInventoryCrafting() {
			super(new InternalInventoryCraftingContainer(), 3, 3);
		}

		@Override
		public ItemStack getStackInSlot(int par1) {
			if(tempStacks != null) {
				if(par1 >= 0 && par1 < 9) {
					if(bindings[par1] >= 0) {
						return tempStacks[bindings[par1]];
					}
				}
				return null;
			}
			return craftingSlots.getStackInSlot(par1);
		}

		@Override
		public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {
			if (tempStacks!=null) {
				tempStacks[bindings[par1]]=par2ItemStack;
			}
		}

		@Override
		public ItemStack decrStackSize(int par1, int par2) {
			if (tempStacks!=null) {
				return tempStacks[bindings[par1]].splitStack(par2);
			} else {
				return null;
			}
		}
	}

	private final class InternalPlayer extends EntityPlayer {
		public InternalPlayer() {
			super(TileAssemblyAdvancedWorkbench.this.worldObj);
		}

		@Override
		public void sendChatToPlayer(String var1) {
		}

		@Override
		public boolean canCommandSenderUseCommand(int var1, String var2) {
			return false;
		}

		@Override
		public ChunkCoordinates func_82114_b() {
			return null;
		}
	}

	public InventoryCraftResult craftResult;
	private InternalInventoryCrafting internalInventoryCrafting;

	public TileAssemblyAdvancedWorkbench() {
		craftingSlots = new SimpleInventory(9, "CraftingSlots", 1);
		storageSlots = new ItemStack[27];
		craftResult = new InventoryCraftResult();
	}

	private SimpleInventory craftingSlots;
	private ItemStack[] storageSlots;

	private SlotCrafting craftSlot;

	private float storedEnergy;
	private float[] recentEnergy = new float[20];
	private boolean craftable;
	private int tick;
	private int recentEnergyAverage;
	private InternalPlayer internalPlayer;

	@Override
	public int getSizeInventory() {
		return 27;
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
	}
	@Override
	public String getInvName() {
		return "AdvancedWorkbench";
	}

	@Override
	public void onInventoryChanged() {
		super.onInventoryChanged();
		craftable = craftResult.getStackInSlot(0)!=null;
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

	public int getRecentEnergyAverage() {
		return recentEnergyAverage;
	}

	public float getStoredEnergy() {
		return storedEnergy;
	}

	public float getRequiredEnergy() {
		return craftResult.getStackInSlot(0) != null ? 500f : 0f;
	}

	@Override
	public void updateEntity() {
		if (internalPlayer == null) {
			internalInventoryCrafting = new InternalInventoryCrafting();
			internalPlayer = new InternalPlayer();
			craftSlot = new SlotCrafting(internalPlayer, internalInventoryCrafting, craftResult, 0, 0, 0);
			updateCraftingResults();
		}
		if (!CoreProxy.proxy.isSimulating(worldObj)) {
			return;
		}
		tick++;
		tick = tick % recentEnergy.length;
		recentEnergy[tick] = 0.0f;
		while (storedEnergy >= getRequiredEnergy() && craftResult.getStackInSlot(0)!=null)
		{
			ItemStack[] tempStorage = Arrays.copyOf(storageSlots, storageSlots.length);
			internalInventoryCrafting.tempStacks = tempStorage;
			internalInventoryCrafting.hitCount=new int[27];
			for (int j = 0; j < craftingSlots.getSizeInventory(); j++)
			{
				if (craftingSlots.getStackInSlot(j) == null)
				{
					internalInventoryCrafting.bindings[j] = -1;
					continue;
				}
				boolean matchedStorage = false;
				for (int i = 0; i < tempStorage.length; i++)
				{
					if (tempStorage[i]!=null && craftingSlots.getStackInSlot(j).isItemEqual(tempStorage[i]) && internalInventoryCrafting.hitCount[i]<tempStorage[i].stackSize && internalInventoryCrafting.hitCount[i]<tempStorage[i].getMaxStackSize())
					{
						internalInventoryCrafting.bindings[j] = i;
						internalInventoryCrafting.hitCount[i]++;
						matchedStorage = true;
						break;
					}
				}
				if (!matchedStorage)
				{
					craftable = false;
					internalInventoryCrafting.tempStacks = null;
					internalInventoryCrafting.hitCount = null;
					return;
				}
			}
			craftSlot.func_82870_a(internalPlayer, craftResult.getStackInSlot(0));
			for (int i=0; i<tempStorage.length; i++) {
				if (tempStorage[i]!=null && tempStorage[i].stackSize<=0) tempStorage[i]=null;
			}
			storageSlots = tempStorage;
			storedEnergy -= getRequiredEnergy();
			List<ItemStack> outputs = Lists.newArrayList(craftResult.getStackInSlot(0).copy());
			for (int i=0;i<internalPlayer.inventory.mainInventory.length;i++) {
				if (internalPlayer.inventory.mainInventory[i]!=null) {
					outputs.add(internalPlayer.inventory.mainInventory[i]);
					internalPlayer.inventory.mainInventory[i]=null;
				}
			}
			for (ItemStack output : outputs) {
				System.out.printf("Output stack is %s\n",output);
				boolean putToPipe = Utils.addToRandomPipeEntry(this, Orientations.YPos, output);
				if (!putToPipe)
				{
					System.out.println(output);
					for (int i = 0; i < storageSlots.length; i++)
					{
						System.out.printf("%d: %s %s\n", i,output, storageSlots[i]);
						if (output.stackSize <= 0) {
							break;
						}
						if (storageSlots[i]!=null && output.isStackable() && output.isItemEqual(storageSlots[i])) {
							storageSlots[i].stackSize += output.stackSize;
							if (storageSlots[i].stackSize > output.getMaxStackSize()) {
								output.stackSize = storageSlots[i].stackSize - output.getMaxStackSize();
								storageSlots[i].stackSize = output.getMaxStackSize();
							} else {
								output.stackSize = 0;
							}
						} else if (storageSlots[i] == null) {
							System.out.println(output);
							storageSlots[i] = output.copy();
							System.out.println(storageSlots[i]+":"+output);
							output.stackSize = 0;
							System.out.println(storageSlots[i]+":"+output);
						}
					}
					if (output.stackSize > 0) {
						output = Utils.addToRandomInventory(output, worldObj, xCoord, yCoord, zCoord, Orientations.Unknown);
					}
					if (output.stackSize > 0) {
						Utils.dropItems(worldObj, output, xCoord, yCoord, zCoord);
					}
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
		craftResult.setInventorySlotContents(0, CraftingManager.getInstance().func_82787_a(internalInventoryCrafting, worldObj));
		onInventoryChanged();
	}

	public IInventory getCraftingSlots() {
		return craftingSlots;
	}

	public ItemStack getOutputSlot() {
		return craftResult.getStackInSlot(0);
	}

	@Override
	public boolean hasCurrentWork() {
		return craftable;
	}

	@Override
	public void receiveLaserEnergy(float energy) {
		storedEnergy += energy;
		recentEnergy[tick] += energy;
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

	@Override
	public boolean isActive() {
		return hasCurrentWork();
	}

	@Override
	public boolean manageLiquids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return false;
	}

	@Override
	public boolean allowActions() {
		return true;
	}

	public void getGUINetworkData(int i, int j) {
		int currentStored = (int)(storedEnergy * 100.0);
		switch (i) {
		case 1:
			currentStored = (currentStored & 0xFFFF0000) | (j & 0xFFFF);
			storedEnergy = (currentStored / 100.0f);
			break;
		case 3:
			currentStored = (currentStored & 0xFFFF) | ( (j & 0xFFFF) << 16);
			storedEnergy = (currentStored / 100.0f);
			break;
		case 4:
			recentEnergyAverage = recentEnergyAverage & 0xFFFF0000 | ( j & 0xFFFF);
			break;
		case 5:
			recentEnergyAverage = (recentEnergyAverage & 0xFFFF) | ( (j & 0xFFFF) << 16);
			break;
		}
	}

	public void sendGUINetworkData(Container container, ICrafting iCrafting) {
		int currentStored = (int)(storedEnergy * 100.0);
		int lRecentEnergy = 0;
		for (int i = 0; i < recentEnergy.length; i++)
		{
			lRecentEnergy += (int)(recentEnergy[i] * 100.0 / (float)(recentEnergy.length - 1));
		}
		iCrafting.updateCraftingInventoryInfo(container, 1, currentStored & 0xFFFF);
		iCrafting.updateCraftingInventoryInfo(container, 3, (currentStored >>> 16) & 0xFFFF);
		iCrafting.updateCraftingInventoryInfo(container, 4, lRecentEnergy & 0xFFFF);
		iCrafting.updateCraftingInventoryInfo(container, 5, (lRecentEnergy >>> 16) & 0xFFFF);
	}

}
