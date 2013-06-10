package buildcraft.silicon;

import java.util.Arrays;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import buildcraft.core.IMachine;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.utils.CraftingHelper;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.utils.Utils;

import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeDirection;

public class TileAdvancedCraftingTable extends TileEntity implements IInventory, ILaserTarget, IMachine {

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
		private boolean useRecipeStack;

		private InternalInventoryCrafting() {
			super(new InternalInventoryCraftingContainer(), 3, 3);
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			if (slot >= 0 && slot < 9) {
				if (useRecipeStack || tempStacks == null) {
					return craftingSlots.getStackInSlot(slot);
				} else {

					if (bindings[slot] >= 0) {
						return tempStacks[bindings[slot]];
					}
				}
			}

			// vanilla returns null for out of bound stacks in InventoryCrafting as well
			return null;
		}

		@Override
		public void setInventorySlotContents(int slot, ItemStack par2ItemStack) {
			if (tempStacks != null) {
				tempStacks[bindings[slot]] = par2ItemStack;
			}
		}

		@Override
		public ItemStack decrStackSize(int slot, int amount) {
			if (tempStacks != null) {
				ItemStack result = tempStacks[bindings[slot]].splitStack(amount);
				if (tempStacks[bindings[slot]].stackSize <= 0) {
					tempStacks[bindings[slot]] = null;
				}
				return result;
			} else {
				return null;
			}
		}

		public void recipeUpdate(boolean flag) {
			useRecipeStack = flag;
		}
	}

	private final class InternalPlayer extends EntityPlayer {

		public InternalPlayer() {
			super(TileAdvancedCraftingTable.this.worldObj);
			posX = TileAdvancedCraftingTable.this.xCoord;
			posY = TileAdvancedCraftingTable.this.yCoord + 1;
			posZ = TileAdvancedCraftingTable.this.zCoord;
			username = "[Buildcraft]";
		}

		@Override
		public void sendChatToPlayer(String var1) {
		}

		@Override
		public boolean canCommandSenderUseCommand(int var1, String var2) {
			return false;
		}

		@Override
		public ChunkCoordinates getPlayerCoordinates() {
			return null;
		}
	}
	public InventoryCraftResult craftResult;
	private InternalInventoryCrafting internalInventoryCrafting;

	public TileAdvancedCraftingTable() {
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
	private IRecipe currentRecipe;

	@Override
	public int getSizeInventory() {
		return 27;
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		if (slot < storageSlots.length) {
			return storageSlots[slot];
		}
		return null;
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		if (slot < storageSlots.length && storageSlots[slot] != null) {
			ItemStack var3;

			if (this.storageSlots[slot].stackSize <= amount) {
				var3 = this.storageSlots[slot];
				this.storageSlots[slot] = null;
				this.onInventoryChanged();
				return var3;
			} else {
				var3 = this.storageSlots[slot].splitStack(amount);

				if (this.storageSlots[slot].stackSize == 0) {
					this.storageSlots[slot] = null;
				}

				this.onInventoryChanged();
				return var3;
			}

		}
		return null;
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		if (slot >= storageSlots.length) {
			return null;
		}
		if (this.storageSlots[slot] != null) {
			ItemStack var2 = this.storageSlots[slot];
			this.storageSlots[slot] = null;
			return var2;
		} else {
			return null;
		}
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		if (slot >= storageSlots.length) {
			return;
		}
		this.storageSlots[slot] = stack;

		if (stack != null && stack.stackSize > this.getInventoryStackLimit()) {
			stack.stackSize = this.getInventoryStackLimit();
		}

		this.onInventoryChanged();
	}

	@Override
	public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
		super.writeToNBT(par1nbtTagCompound);
		NBTTagList var2 = new NBTTagList();

		for (int var3 = 0; var3 < this.storageSlots.length; ++var3) {
			if (this.storageSlots[var3] != null) {
				NBTTagCompound var4 = new NBTTagCompound();
				var4.setByte("Slot", (byte) var3);
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

		for (int var3 = 0; var3 < var2.tagCount(); ++var3) {
			NBTTagCompound var4 = (NBTTagCompound) var2.tagAt(var3);
			int var5 = var4.getByte("Slot") & 255;

			if (var5 >= 0 && var5 < this.storageSlots.length) {
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
		craftable = craftResult.getStackInSlot(0) != null;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer player) {
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
		updateCraftingResults();
		tick++;
		tick = tick % recentEnergy.length;
		recentEnergy[tick] = 0.0f;
		while (storedEnergy >= getRequiredEnergy() && craftResult.getStackInSlot(0) != null) {
			ItemStack[] tempStorage = Arrays.copyOf(storageSlots, storageSlots.length);
			internalInventoryCrafting.tempStacks = tempStorage;
			internalInventoryCrafting.hitCount = new int[27];
			for (int j = 0; j < craftingSlots.getSizeInventory(); j++) {
				if (craftingSlots.getStackInSlot(j) == null) {
					internalInventoryCrafting.bindings[j] = -1;
					continue;
				}
				boolean matchedStorage = false;
				for (int i = 0; i < tempStorage.length; i++) {
					if (tempStorage[i] != null && craftingSlots.getStackInSlot(j).isItemEqual(tempStorage[i])
							&& internalInventoryCrafting.hitCount[i] < tempStorage[i].stackSize
							&& internalInventoryCrafting.hitCount[i] < tempStorage[i].getMaxStackSize()) {
						internalInventoryCrafting.bindings[j] = i;
						internalInventoryCrafting.hitCount[i]++;
						matchedStorage = true;
						break;
					}
				}
				if (!matchedStorage) {
					craftable = false;
					internalInventoryCrafting.tempStacks = null;
					internalInventoryCrafting.hitCount = null;
					return;
				}
			}
			craftSlot.onPickupFromSlot(internalPlayer, craftResult.getStackInSlot(0));
			for (int i = 0; i < tempStorage.length; i++) {
				if (tempStorage[i] != null && tempStorage[i].stackSize <= 0) {
					tempStorage[i] = null;
				}
			}
			storageSlots = tempStorage;
			storedEnergy -= getRequiredEnergy();
			List<ItemStack> outputs = Lists.newArrayList(craftResult.getStackInSlot(0).copy());
			for (int i = 0; i < internalPlayer.inventory.mainInventory.length; i++) {
				if (internalPlayer.inventory.mainInventory[i] != null) {
					outputs.add(internalPlayer.inventory.mainInventory[i]);
					internalPlayer.inventory.mainInventory[i] = null;
				}
			}
			for (ItemStack output : outputs) {
				boolean putToPipe = Utils.addToRandomPipeEntry(this, ForgeDirection.UP, output);
				if (!putToPipe) {
					output.stackSize -= Transactor.getTransactorFor(this).add(output, ForgeDirection.UP, true).stackSize;
					if (output.stackSize > 0) {
						output.stackSize -= Utils.addToRandomInventory(output, worldObj, xCoord, yCoord, zCoord).stackSize;
					}
					if (output.stackSize > 0) {
						Utils.dropItems(worldObj, output, xCoord, yCoord + 1, zCoord);
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
		if (internalInventoryCrafting == null) {
			return;
		}
		internalInventoryCrafting.recipeUpdate(true);
		if (this.currentRecipe == null || !this.currentRecipe.matches(internalInventoryCrafting, worldObj)) {
			currentRecipe = CraftingHelper.findMatchingRecipe(internalInventoryCrafting, worldObj);
		}

		ItemStack resultStack = null;
		if (currentRecipe != null) {
			resultStack = currentRecipe.getCraftingResult(internalInventoryCrafting);
		}
		craftResult.setInventorySlotContents(0, resultStack);
		internalInventoryCrafting.recipeUpdate(false);
		onInventoryChanged();
	}

	public IInventory getCraftingSlots() {
		return craftingSlots;
	}

	public IInventory getOutputSlot() {
		return craftResult;
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

	public void getGUINetworkData(int id, int data) {
		int currentStored = (int) (storedEnergy * 100.0);
		switch (id) {
			case 1:
				currentStored = (currentStored & 0xFFFF0000) | (data & 0xFFFF);
				storedEnergy = (currentStored / 100.0f);
				break;
			case 3:
				currentStored = (currentStored & 0xFFFF) | ((data & 0xFFFF) << 16);
				storedEnergy = (currentStored / 100.0f);
				break;
			case 4:
				recentEnergyAverage = recentEnergyAverage & 0xFFFF0000 | (data & 0xFFFF);
				break;
			case 5:
				recentEnergyAverage = (recentEnergyAverage & 0xFFFF) | ((data & 0xFFFF) << 16);
				break;
		}
	}

	public void sendGUINetworkData(Container container, ICrafting iCrafting) {
		int currentStored = (int) (storedEnergy * 100.0);
		int lRecentEnergy = 0;
		for (int i = 0; i < recentEnergy.length; i++) {
			lRecentEnergy += (int) (recentEnergy[i] * 100.0 / (recentEnergy.length - 1));
		}
		iCrafting.sendProgressBarUpdate(container, 1, currentStored & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 3, (currentStored >>> 16) & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 4, lRecentEnergy & 0xFFFF);
		iCrafting.sendProgressBarUpdate(container, 5, (lRecentEnergy >>> 16) & 0xFFFF);
	}

	@Override
	public boolean isInvNameLocalized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isStackValidForSlot(int slot, ItemStack stack) {
		return true;
	}
}
