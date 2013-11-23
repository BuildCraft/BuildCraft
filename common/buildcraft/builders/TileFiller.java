/**
 * Copyright (c) SpaceToad, 2011 http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License
 * 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.LaserKind;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.filler.IFillerPattern;
import buildcraft.api.filler.IPatternIterator;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.IActionReceptor;
import buildcraft.api.power.IPowerReceptor;
import buildcraft.api.power.PowerHandler;
import buildcraft.api.power.PowerHandler.PowerReceiver;
import buildcraft.api.power.PowerHandler.Type;
import buildcraft.builders.filler.pattern.PatternFill;
import buildcraft.builders.triggers.ActionFiller;
import buildcraft.core.Box;
import buildcraft.core.IMachine;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.InventoryIterator.IInvSlot;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.IGuiReturnHandler;
import buildcraft.core.network.PacketPayload;
import buildcraft.core.network.PacketPayloadStream;
import buildcraft.core.network.PacketUpdate;
import buildcraft.core.proxy.CoreProxy;
import buildcraft.core.triggers.ActionMachineControl;
import buildcraft.core.triggers.ActionMachineControl.Mode;
import buildcraft.core.utils.Utils;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.ForgeDirection;

public class TileFiller extends TileBuildCraft implements IInventory, IPowerReceptor, IMachine, IActionReceptor, IGuiReturnHandler {

	public IFillerPattern currentPattern = PatternFill.INSTANCE;
	private static int POWER_USAGE = 25;
	private final Box box = new Box();
	private boolean done = false;
	private IPatternIterator patternIterator;
	private PowerHandler powerHandler;
	private ActionMachineControl.Mode lastMode = ActionMachineControl.Mode.Unknown;
	private SimpleInventory inv = new SimpleInventory(27, "Filler", 64);

	public TileFiller() {
		inv.addListener(this);
		powerHandler = new PowerHandler(this, Type.MACHINE);
		initPowerProvider();
	}

	private void initPowerProvider() {
		powerHandler.configure(30, POWER_USAGE * 2, POWER_USAGE, POWER_USAGE * 4);
		powerHandler.configurePowerPerdition(1, 1);
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!CoreProxy.proxy.isRenderWorld(worldObj)) {
			IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord, zCoord);

			if (a != null) {
				box.initialize(a);

				if (a instanceof TileMarker) {
					((TileMarker) a).removeFromWorld();
				}

				if (!CoreProxy.proxy.isRenderWorld(worldObj) && box.isInitialized()) {
					box.createLasers(worldObj, LaserKind.Stripes);
				}
				sendNetworkUpdate();
			}
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();
		if (done) {
			if (lastMode == Mode.Loop) {
				done = false;
			}
		}
	}

	@Override
	public void doWork(PowerHandler workProvider) {
		if (CoreProxy.proxy.isRenderWorld(worldObj))
			return;
		if (done)
			return;
		if (lastMode == Mode.Off)
			return;
		if (powerHandler.useEnergy(POWER_USAGE, POWER_USAGE, false) != POWER_USAGE)
			return;
		if (!box.isInitialized())
			return;

		if (patternIterator == null)
			patternIterator = currentPattern.createPatternIterator(this, box, ForgeDirection.NORTH);

		ItemStack stackToUse = null;
		int slotNum = 0;

		for (IInvSlot slot : InventoryIterator.getIterable(inv, ForgeDirection.UNKNOWN)) {
			ItemStack stack = slot.getStackInSlot();
			if (stack != null && stack.stackSize > 0) {
				stackToUse = stack;
				slotNum = slot.getIndex();
				break;
			}
		}

		done = patternIterator.iteratePattern(stackToUse);
		powerHandler.useEnergy(POWER_USAGE, POWER_USAGE, true);

		if (stackToUse != null && stackToUse.stackSize <= 0) {
			setInventorySlotContents(slotNum, null);
		}

		if (done) {
			patternIterator = null;
			sendNetworkUpdate();
		} else if (powerHandler.getEnergyStored() >= POWER_USAGE) {
			doWork(workProvider);
		}
	}

	@Override
	public final int getSizeInventory() {
		return inv.getSizeInventory();
	}

	@Override
	public ItemStack getStackInSlot(int slot) {
		return inv.getStackInSlot(slot);
	}

	@Override
	public ItemStack decrStackSize(int slot, int amount) {
		return inv.decrStackSize(slot, amount);
	}

	@Override
	public void setInventorySlotContents(int slot, ItemStack stack) {
		inv.setInventorySlotContents(slot, stack);
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inv.getStackInSlotOnClosing(slot);
	}

	@Override
	public String getInvName() {
		return "Filler";
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		inv.readFromNBT(nbt);

		if (nbt.hasKey("pattern"))
			currentPattern = FillerManager.registry.getPattern(nbt.getString("pattern"));

		if (currentPattern == null)
			currentPattern = PatternFill.INSTANCE;

		if (nbt.hasKey("box"))
			box.initialize(nbt.getCompoundTag("box"));

		done = nbt.getBoolean("done");
		lastMode = Mode.values()[nbt.getByte("lastMode")];
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		inv.writeToNBT(nbt);

		if (currentPattern != null)
			nbt.setString("pattern", currentPattern.getUniqueTag());

		NBTTagCompound boxStore = new NBTTagCompound();
		box.writeToNBT(boxStore);
		nbt.setTag("box", boxStore);

		nbt.setBoolean("done", done);
		nbt.setByte("lastMode", (byte) lastMode.ordinal());
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if (worldObj.getBlockTileEntity(xCoord, yCoord, zCoord) != this)
			return false;
		return entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64D;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	@Override
	public void destroy() {
		box.deleteLasers();
	}

	public void setPattern(IFillerPattern pattern) {
		if (pattern != null && currentPattern != pattern) {
			currentPattern = pattern;
			patternIterator = null;
			done = false;
			sendNetworkUpdate();
		}
	}

	@Override
	public PacketPayload getPacketPayload() {
		PacketPayloadStream payload = new PacketPayloadStream(new PacketPayloadStream.StreamWriter() {
			@Override
			public void writeData(DataOutputStream data) throws IOException {
				box.writeToStream(data);
				data.writeBoolean(done);
				data.writeUTF(currentPattern.getUniqueTag());
			}
		});

		return payload;
	}

	public void handlePacketPayload(DataInputStream data) throws IOException {
		boolean initialized = box.isInitialized();
		box.readFromStream(data);
		done = data.readBoolean();
		setPattern(FillerManager.registry.getPattern(data.readUTF()));

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
		if (!initialized && box.isInitialized()) {
			box.createLasers(worldObj, LaserKind.Stripes);
		}
	}

	@Override
	public void handleDescriptionPacket(PacketUpdate packet) throws IOException {
		handlePacketPayload(((PacketPayloadStream) packet.payload).stream);
	}

	@Override
	public void handleUpdatePacket(PacketUpdate packet) throws IOException {
		handlePacketPayload(((PacketPayloadStream) packet.payload).stream);
	}

	@Override
	public PowerReceiver getPowerReceiver(ForgeDirection side) {
		return powerHandler.getPowerReceiver();
	}

	@Override
	public boolean isActive() {
		return !done && lastMode != Mode.Off;
	}

	@Override
	public boolean manageFluids() {
		return false;
	}

	@Override
	public boolean manageSolids() {
		return true;
	}

	@Override
	public void openChest() {
	}

	@Override
	public void closeChest() {
	}

	@Override
	public void actionActivated(IAction action) {
		if (action == BuildCraftCore.actionOn) {
			lastMode = ActionMachineControl.Mode.On;
		} else if (action == BuildCraftCore.actionOff) {
			lastMode = ActionMachineControl.Mode.Off;
		} else if (action == BuildCraftCore.actionLoop) {
			lastMode = ActionMachineControl.Mode.Loop;
		} else if (action instanceof ActionFiller) {
			ActionFiller actFill = (ActionFiller) action;
			setPattern(actFill.pattern);
		}
	}

	@Override
	public boolean allowAction(IAction action) {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}

	@Override
	public void writeGuiData(DataOutputStream data) throws IOException {
		data.writeUTF(currentPattern.getUniqueTag());
	}

	@Override
	public void readGuiData(DataInputStream data, EntityPlayer player) throws IOException {
		IFillerPattern prev = currentPattern;
		currentPattern = FillerManager.registry.getPattern(data.readUTF());
		if (currentPattern == null)
			currentPattern = PatternFill.INSTANCE;
		if (prev != currentPattern)
			done = false;
		sendNetworkUpdate();
	}
}
