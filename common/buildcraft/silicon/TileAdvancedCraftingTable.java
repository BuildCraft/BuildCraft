package buildcraft.silicon;

import buildcraft.api.power.ILaserTarget;
import buildcraft.BuildCraftCore;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuffer;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.inventory.InventoryCopy;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import buildcraft.core.inventory.InventoryMapper;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.CraftingFilter;
import buildcraft.core.inventory.filters.IStackFilter;
import buildcraft.core.network.PacketIds;
import buildcraft.core.network.PacketSlotChange;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.utils.CraftingHelper;
import buildcraft.core.utils.Utils;
import com.google.common.collect.Lists;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatMessageComponent;
import net.minecraft.util.ChunkCoordinates;
import net.minecraftforge.common.ForgeDirection;
import static net.minecraftforge.common.ForgeDirection.DOWN;
import static net.minecraftforge.common.ForgeDirection.EAST;
import static net.minecraftforge.common.ForgeDirection.NORTH;
import static net.minecraftforge.common.ForgeDirection.SOUTH;
import static net.minecraftforge.common.ForgeDirection.WEST;
import net.minecraftforge.oredict.OreDictionary;
import org.bouncycastle.util.Arrays;

public class TileAdvancedCraftingTable extends TileEntity implements IInventory, ILaserTarget, IMachine, IActionReceptor, ISidedInventory {

	private final class InternalInventoryCraftingContainer extends Container {

		@Override
		public boolean canInteractWith(EntityPlayer var1) {
			return false;
		}
	}

	private final class CraftingGrid extends SimpleInventory {

		public int[] oreIDs = new int[9];

		public CraftingGrid() {
			super(9, "CraftingSlots", 1);
			Arrays.fill(oreIDs, -1);
		}

		@Override
		public void setInventorySlotContents(int slotId, ItemStack itemstack) {
			super.setInventorySlotContents(slotId, itemstack);
			if (TileAdvancedCraftingTable.this.worldObj == null || !TileAdvancedCraftingTable.this.worldObj.isRemote)
				oreIDs[slotId] = itemstack == null ? -1 : OreDictionary.getOreID(itemstack);
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
			if (tempStacks != null && slot >= 0 && slot < 9 && bindings[slot] >= 0) {
				tempStacks[bindings[slot]] = par2ItemStack;
			}
		}

		@Override
		public ItemStack decrStackSize(int slot, int amount) {
			if (tempStacks != null && slot >= 0 && slot < 9 && bindings[slot] >= 0) {
				if (tempStacks[bindings[slot]].stackSize <= amount) {
					ItemStack result = tempStacks[bindings[slot]];
					tempStacks[bindings[slot]] = null;
					return result;
				} else {
					ItemStack result = tempStacks[bindings[slot]].splitStack(amount);
					if (tempStacks[bindings[slot]].stackSize <= 0) {
						tempStacks[bindings[slot]] = null;
					}
					return result;
				}
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
			super(TileAdvancedCraftingTable.this.worldObj, "[BuildCraft]");
			posX = TileAdvancedCraftingTable.this.xCoord;
			posY = TileAdvancedCraftingTable.this.yCoord + 1;
			posZ = TileAdvancedCraftingTable.this.zCoord;
		}

		@Override
		public void sendChatToPlayer(ChatMessageComponent var1) {
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
		craftingSlots = new CraftingGrid();
		storageSlots = new SimpleInventory(24, "StorageSlots", 64);
		storageSlots.addListener(this);
		invInput = new InventoryMapper(storageSlots, 0, 15);
		invOutput = new InventoryMapper(storageSlots, 15, 9);
		craftResult = new InventoryCraftResult();
	}
	private static final int[] SLOTS = Utils.createSlotArray(0, 24);
	private static final EnumSet<ForgeDirection> SEARCH_SIDES = EnumSet.of(DOWN, NORTH, SOUTH, EAST, WEST);
	private static final float REQUIRED_POWER = 500F;
	private final CraftingGrid craftingSlots;
	private final SimpleInventory storageSlots;
	private final InventoryMapper invInput;
	private final InventoryMapper invOutput;
	private SlotCrafting craftSlot;
	private float storedEnergy;
	private float[] recentEnergy = new float[20];
	private boolean craftable;
	private boolean justCrafted;
	private int tick;
	private int recentEnergyAverage;
	private InternalPlayer internalPlayer;
	private IRecipe currentRecipe;
	private ActionMachineControl.Mode lastMode = ActionMachineControl.Mode.Unknown;
	private TileBuffer[] cache;

	@Override
	public int getSizeInventory() {
		return storageSlots.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return storageSlots.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return storageSlots.decrStackSize(slot, amount);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return storageSlots.getStackInSlotOnClosing(slot);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		storageSlots.setInventorySlotContents(slot, stack);
	}

	@Override
	public void writeToNBT(NBTTagCompound data) {
		super.writeToNBT(data);
		storageSlots.writeToNBT(data, "StorageSlots");
		craftingSlots.writeToNBT(data);
		data.setFloat("StoredEnergy", storedEnergy);
	}

	@Override
	public void readFromNBT(NBTTagCompound data) {
		super.readFromNBT(data);
		storageSlots.readFromNBT(data, "StorageSlots");
		craftingSlots.readFromNBT(data);
		storedEnergy = data.getFloat("StoredEnergy");
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
		return storageSlots.getInventoryStackLimit();
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
		return craftResult.getStackInSlot(0) != null ? REQUIRED_POWER : 0f;
	}

	public int getProgressScaled(int i) {
		return (int) ((storedEnergy * i) / REQUIRED_POWER);
	}

	@Override
	public void invalidate() {
		super.invalidate();
		cache = null;
	}

	@Override
	public void updateEntity() {
		if (internalPlayer == null) {
			internalInventoryCrafting = new InternalInventoryCrafting();
			internalPlayer = new InternalPlayer();
			craftSlot = new SlotCrafting(internalPlayer, internalInventoryCrafting, craftResult, 0, 0, 0);
			updateRecipe();
		}
		if (!CoreProxy.proxy.isSimulating(worldObj))
			return;
		if (lastMode == ActionMachineControl.Mode.Off)
			return;
		updateRecipe();
		searchNeighborsForIngredients();
		locateAndBindIngredients();
		updateRecipeOutputDisplay();
		justCrafted = false;
		tick++;
		tick = tick % recentEnergy.length;
		recentEnergy[tick] = 0.0f;
		if (canCraftAndOutput()) {
			if (storedEnergy >= getRequiredEnergy()) {
				craftItem();
				justCrafted = true;
			}
		} else {
			craftable = false;
			internalInventoryCrafting.tempStacks = null;
			internalInventoryCrafting.hitCount = null;
			storedEnergy = 0;
		}
	}

	private boolean canCraftAndOutput() {
		if (!hasIngredients())
			return false;
		ItemStack output = getRecipeOutput();
		if (output == null)
			return false;
		return InvUtils.isRoomForStack(output, ForgeDirection.UP, invOutput);
	}

	private void locateAndBindIngredients() {
		internalInventoryCrafting.tempStacks = new InventoryCopy(storageSlots).getItemStacks();
		internalInventoryCrafting.hitCount = new int[internalInventoryCrafting.tempStacks.length];
		ItemStack[] inputSlots = internalInventoryCrafting.tempStacks;
		for (int gridSlot = 0; gridSlot < craftingSlots.getSizeInventory(); gridSlot++) {
			internalInventoryCrafting.bindings[gridSlot] = -1;
			if (craftingSlots.getStackInSlot(gridSlot) == null)
				continue;
			boolean foundMatch = false;
			for (int inputSlot = 0; inputSlot < inputSlots.length; inputSlot++) {
				if (!isMatchingIngredient(gridSlot, inputSlot))
					continue;
				if (internalInventoryCrafting.hitCount[inputSlot] < inputSlots[inputSlot].stackSize
						&& internalInventoryCrafting.hitCount[inputSlot] < inputSlots[inputSlot].getMaxStackSize()) {
					internalInventoryCrafting.bindings[gridSlot] = inputSlot;
					internalInventoryCrafting.hitCount[inputSlot]++;
					foundMatch = true;
					break;
				}
			}
			if (!foundMatch)
				return;
		}
	}

	private boolean isMatchingIngredient(int gridSlot, int inputSlot) {
		ItemStack inputStack = internalInventoryCrafting.tempStacks[inputSlot];
		if (inputStack == null)
			return false;
		if (StackHelper.instance().isMatchingItem(craftingSlots.getStackInSlot(gridSlot), inputStack, true, false))
			return true;
		if (StackHelper.instance().isCraftingEquivalent(craftingSlots.oreIDs[gridSlot], inputStack))
			return true;
		return false;
	}

	private boolean hasIngredients() {
		return currentRecipe != null && currentRecipe.matches(internalInventoryCrafting, worldObj);
	}

	private void craftItem() {
		ItemStack recipeOutput = getRecipeOutput();
		craftSlot.onPickupFromSlot(internalPlayer, recipeOutput);
		ItemStack[] tempStorage = internalInventoryCrafting.tempStacks;
		for (int i = 0; i < tempStorage.length; i++) {
			if (tempStorage[i] != null && tempStorage[i].stackSize <= 0) {
				tempStorage[i] = null;
			}
			storageSlots.getItemStacks()[i] = tempStorage[i];
		}
		storedEnergy -= getRequiredEnergy();
		List<ItemStack> outputs = Lists.newArrayList(recipeOutput.copy());
		for (int i = 0; i < internalPlayer.inventory.mainInventory.length; i++) {
			if (internalPlayer.inventory.mainInventory[i] != null) {
				outputs.add(internalPlayer.inventory.mainInventory[i]);
				internalPlayer.inventory.mainInventory[i] = null;
			}
		}
		for (ItemStack output : outputs) {
			output.stackSize -= Transactor.getTransactorFor(invOutput).add(output, ForgeDirection.UP, true).stackSize;
			if (output.stackSize > 0) {
				output.stackSize -= Utils.addToRandomInventoryAround(worldObj, xCoord, yCoord, zCoord, output);
			}
			if (output.stackSize > 0) {
				InvUtils.dropItems(worldObj, output, xCoord, yCoord + 1, zCoord);
			}
		}
	}

	private void searchNeighborsForIngredients() {
		if (cache == null) {
			cache = TileBuffer.makeBuffer(worldObj, xCoord, yCoord, zCoord, false);
		}
		for (IInvSlot slot : InventoryIterator.getIterable(craftingSlots, ForgeDirection.UP)) {
			ItemStack ingred = slot.getStackInSlot();
			if (ingred == null)
				continue;
			IStackFilter filter = new CraftingFilter(ingred);
			if (InvUtils.countItems(invInput, ForgeDirection.UP, filter) < InvUtils.countItems(craftingSlots, ForgeDirection.UP, filter)) {
				for (ForgeDirection side : SEARCH_SIDES) {
					TileEntity tile = cache[side.ordinal()].getTile();
					if (tile instanceof IInventory) {
						IInventory inv = Utils.getInventory(((IInventory) tile));
						ItemStack result = InvUtils.moveOneItem(inv, side.getOpposite(), invInput, side, filter);
						if (result != null) {
							return;
						}
					}
				}
			}
		}
	}

	public void updateCraftingMatrix(int slot, ItemStack stack) {
		craftingSlots.setInventorySlotContents(slot, stack);
		updateRecipe();
		if (CoreProxy.proxy.isRenderWorld(worldObj)) {
			PacketSlotChange packet = new PacketSlotChange(PacketIds.ADVANCED_WORKBENCH_SETSLOT, xCoord, yCoord, zCoord, slot, stack);
			CoreProxy.proxy.sendToServer(packet.getPacket());
		}
	}

	private void updateRecipe() {
		if (internalInventoryCrafting == null) {
			return;
		}
		internalInventoryCrafting.recipeUpdate(true);
		if (this.currentRecipe == null || !this.currentRecipe.matches(internalInventoryCrafting, worldObj)) {
			currentRecipe = CraftingHelper.findMatchingRecipe(internalInventoryCrafting, worldObj);
		}
		internalInventoryCrafting.recipeUpdate(false);
		onInventoryChanged();
	}

	private void updateRecipeOutputDisplay() {
		if (internalInventoryCrafting == null || currentRecipe == null) {
			craftResult.setInventorySlotContents(0, null);
			return;
		}
		ItemStack resultStack = getRecipeOutput();
		if (resultStack == null) {
			internalInventoryCrafting.recipeUpdate(true);
			resultStack = getRecipeOutput();
			internalInventoryCrafting.recipeUpdate(false);
		}
		craftResult.setInventorySlotContents(0, resultStack);
		onInventoryChanged();
	}

	private ItemStack getRecipeOutput() {
		if (internalInventoryCrafting == null || currentRecipe == null) {
			return null;
		}
		return currentRecipe.getCraftingResult(internalInventoryCrafting);
	}

	public IInventory getCraftingSlots() {
		return craftingSlots;
	}

	public IInventory getOutputSlot() {
		return craftResult;
	}

	@Override
	public boolean requiresLaserEnergy() {
		return craftable && !justCrafted && lastMode != ActionMachineControl.Mode.Off && storedEnergy < REQUIRED_POWER * 10;
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
		return requiresLaserEnergy();
	}

	@Override
	public boolean manageFluids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return false;
	}

	@Override
	public boolean allowAction(IAction action) {
		return action == BuildCraftCore.actionOn || action == BuildCraftCore.actionOff;
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
	public int[] getAccessibleSlotsFromSide(int side) {
		return SLOTS;
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack stack, int side) {
		return isItemValidForSlot(slot, stack);
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack stack, int side) {
		return slot >= 15;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return slot < 15;
	}

	@Override
	public void actionActivated(IAction action) {
		if (action == BuildCraftCore.actionOn) {
			lastMode = ActionMachineControl.Mode.On;
		} else if (action == BuildCraftCore.actionOff) {
			lastMode = ActionMachineControl.Mode.Off;
		}
	}

	@Override
	public boolean isInvalidTarget() {
		return isInvalid();
	}
}
