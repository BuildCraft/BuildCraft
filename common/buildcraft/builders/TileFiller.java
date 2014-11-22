/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import cpw.mods.fml.relauncher.Side;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.builders.patterns.PatternFill;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.ICommandReceiver;
import buildcraft.core.network.PacketCommand;
import buildcraft.core.utils.Utils;

public class TileFiller extends TileAbstractBuilder implements IHasWork, IControllable, ICommandReceiver {

	private static int POWER_ACTIVATION = 500;

	public FillerPattern currentPattern = PatternFill.INSTANCE;

	private BptBuilderTemplate currentTemplate;
	private BptContext context;

	private final Box box = new Box();
	private boolean done = false;
	private IControllable.Mode lastMode = IControllable.Mode.Unknown;
	private SimpleInventory inv = new SimpleInventory(27, "Filler", 64);

	private NBTTagCompound initNBT = null;

	public TileFiller() {
		inv.addListener(this);
		box.kind = Kind.STRIPES;
	}

	@Override
	public void initialize() {
		super.initialize();

		if (worldObj.isRemote) {
			return;
		}

		IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord, yCoord,
				zCoord);

		if (a != null) {
			box.initialize(a);

			if (a instanceof TileMarker) {
				((TileMarker) a).removeFromWorld();
			}

			sendNetworkUpdate();
		}

		if (currentPattern != null && currentTemplate == null) {
			currentTemplate = currentPattern
					.getTemplateBuilder(box, getWorldObj());
			context = currentTemplate.getContext();
		}

		if (initNBT != null && currentTemplate != null) {
			currentTemplate.loadBuildStateToNBT(
					initNBT.getCompoundTag("builderState"), this);
		}

		initNBT = null;
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (lastMode == Mode.Off) {
			return;
		}

		if (!box.isInitialized()) {
			return;
		}

		if (getBattery().getEnergyStored() < POWER_ACTIVATION) {
			return;
		}

		boolean oldDone = done;

		if (done) {
			if (lastMode == Mode.Loop) {
				done = false;
			} else {
				return;
			}
		}

		if (currentPattern != null && currentTemplate == null) {
			currentTemplate = currentPattern.getTemplateBuilder(box, getWorldObj());
			context = currentTemplate.getContext();
		}

		if (currentTemplate != null) {
			currentTemplate.buildNextSlot(worldObj, this, xCoord, yCoord, zCoord);

			if (currentTemplate.isDone(this)) {
				done = true;
				currentTemplate = null;
			}
		}

		if (oldDone != done) {
			sendNetworkUpdate();
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
	public String getInventoryName() {
		return "Filler";
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		inv.readFromNBT(nbt);

		if (nbt.hasKey("pattern")) {
			currentPattern = (FillerPattern) FillerManager.registry.getPattern(nbt.getString("pattern"));
		}

		if (currentPattern == null) {
			currentPattern = PatternFill.INSTANCE;
		}

		if (nbt.hasKey("box")) {
			box.initialize(nbt.getCompoundTag("box"));
		}

		done = nbt.getBoolean("done");
		lastMode = Mode.values()[nbt.getByte("lastMode")];

		// The rest of load has to be done upon initialize.
		initNBT = (NBTTagCompound) nbt.getCompoundTag("bpt").copy();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		inv.writeToNBT(nbt);

		if (currentPattern != null) {
			nbt.setString("pattern", currentPattern.getUniqueTag());
		}

		NBTTagCompound boxStore = new NBTTagCompound();
		box.writeToNBT(boxStore);
		nbt.setTag("box", boxStore);

		nbt.setBoolean("done", done);
		nbt.setByte("lastMode", (byte) lastMode.ordinal());

		NBTTagCompound bptNBT = new NBTTagCompound();

		if (currentTemplate != null) {
			NBTTagCompound builderCpt = new NBTTagCompound();
			currentTemplate.saveBuildStateToNBT(builderCpt, this);
			bptNBT.setTag("builderState", builderCpt);
		}

		nbt.setTag("bpt", bptNBT);
	}

	@Override
	public int getInventoryStackLimit() {
		return inv.getInventoryStackLimit();
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		if (worldObj.getTileEntity(xCoord, yCoord, zCoord) != this) {
			return false;
		}

		return entityplayer.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D,
				zCoord + 0.5D) <= 64D;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	public void setPattern(FillerPattern pattern) {
		if (pattern != null && currentPattern != pattern) {
			currentPattern = pattern;
			currentTemplate = null;
			done = false;
			sendNetworkUpdate();
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		box.writeData(data);
		data.writeBoolean(done);
		Utils.writeUTF(data, currentPattern.getUniqueTag());
	}

	@Override
	public void readData(ByteBuf data) {
		box.readData(data);
		done = data.readBoolean();
		setPattern((FillerPattern) FillerManager.registry.getPattern(Utils.readUTF(data)));

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean hasWork() {
		return !done && lastMode != Mode.Off;
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean isItemValidForSlot(int slot, ItemStack stack) {
		return true;
	}

	public void rpcSetPatternFromString (final String name) {
		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "set", new CommandWriter() {
			public void write(ByteBuf data) {
				Utils.writeUTF(data, name);
			}
		}));
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isServer() && command.equals("set")) {
			String name = Utils.readUTF(stream);
			setPattern((FillerPattern) FillerManager.registry.getPattern(name));
		}
	}

	@Override
	public boolean hasCustomInventoryName() {
		return false;
	}

	@Override
	public Box getBox() {
		return box;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return new Box (this).extendToEncompass(box).expand(50).getBoundingBox();
	}

	@Override
	public boolean isBuildingMaterialSlot(int i) {
		return true;
	}

	@Override
	public Mode getControlMode() {
		return this.lastMode;
	}

	@Override
	public void setControlMode(Mode mode) {
		this.lastMode = mode;
	}

	@Override
	public boolean acceptsControlMode(Mode mode) {
		return mode == IControllable.Mode.On ||
				mode == IControllable.Mode.Off ||
				mode == IControllable.Mode.Loop;
	}

}
