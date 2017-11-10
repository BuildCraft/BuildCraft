/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.filler.FillerManager;
import buildcraft.api.statements.IStatementContainer;
import buildcraft.api.statements.IStatementParameter;
import buildcraft.api.statements.StatementManager;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IHasWork;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.blueprints.BptBuilderTemplate;
import buildcraft.core.builders.TileAbstractBuilder;
import buildcraft.core.builders.patterns.FillerPattern;
import buildcraft.core.builders.patterns.PatternFill;
import buildcraft.core.internal.ILEDProvider;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;

public class TileFiller extends TileAbstractBuilder implements IHasWork, IControllable, ICommandReceiver, IStatementContainer, ILEDProvider {
	private static int POWER_ACTIVATION = 500;

	public FillerPattern currentPattern = PatternFill.INSTANCE;
	public IStatementParameter[] patternParameters;

	private BptBuilderTemplate currentTemplate;

	private final Box box = new Box();
	private boolean done = false;
	private boolean excavate = true;
	private SimpleInventory inv = new SimpleInventory(27, "Filler", 64);

	private NBTTagCompound initNBT = null;

	public TileFiller() {
		inv.addListener(this);
		box.kind = Kind.STRIPES;
	}

	public boolean isExcavate() {
		return excavate;
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
			a.removeFromWorld();
			sendNetworkUpdate();
		}

		if (currentTemplate == null) {
			initTemplate();
		}

		if (initNBT != null && currentTemplate != null) {
			currentTemplate.loadBuildStateToNBT(
					initNBT.getCompoundTag("builderState"), this);
		}

		initNBT = null;
	}

	private void initTemplate() {
		if (currentPattern != null && box.isInitialized() && box.sizeX() > 0 && box.sizeY() > 0 && box.sizeZ() > 0) {
			currentTemplate = currentPattern.getTemplateBuilder(box, getWorldObj(), patternParameters);
			currentTemplate.blueprint.excavate = excavate;
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (mode == Mode.Off) {
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
			if (mode == Mode.Loop) {
				done = false;
			} else {
				return;
			}
		}

		if (currentTemplate == null) {
			initTemplate();
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

		if (nbt.hasKey("pp")) {
			readParametersFromNBT(nbt.getCompoundTag("pp"));
		} else {
			initPatternParameters();
		}

		if (nbt.hasKey("box")) {
			box.initialize(nbt.getCompoundTag("box"));
		}

		done = nbt.getBoolean("done");
		excavate = nbt.hasKey("excavate") ? nbt.getBoolean("excavate") : true;

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
		nbt.setBoolean("excavate", excavate);

		NBTTagCompound bptNBT = new NBTTagCompound();

		if (currentTemplate != null) {
			NBTTagCompound builderCpt = new NBTTagCompound();
			currentTemplate.saveBuildStateToNBT(builderCpt, this);
			bptNBT.setTag("builderState", builderCpt);
		}

		nbt.setTag("bpt", bptNBT);

		NBTTagCompound ppNBT = new NBTTagCompound();
		writeParametersToNBT(ppNBT);
		nbt.setTag("pp", ppNBT);
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

	private void initPatternParameters() {
		patternParameters = new IStatementParameter[currentPattern.maxParameters()];
		for (int i = 0; i < currentPattern.minParameters(); i++) {
			patternParameters[i] = currentPattern.createParameter(i);
		}
	}

	public void setPattern(FillerPattern pattern) {
		if (pattern != null && currentPattern != pattern) {
			currentPattern = pattern;
			currentTemplate = null;
			done = false;
			initPatternParameters();
			sendNetworkUpdate();
		}
	}

	private void writeParametersToNBT(NBTTagCompound nbt) {
		nbt.setByte("length", (byte) (patternParameters != null ? patternParameters.length : 0));
		if (patternParameters != null) {
			for (int i = 0; i < patternParameters.length; i++) {
				if (patternParameters[i] != null) {
					NBTTagCompound patternData = new NBTTagCompound();
					patternData.setString("kind", patternParameters[i].getUniqueTag());
					patternParameters[i].writeToNBT(patternData);
					nbt.setTag("p" + i, patternData);
				}
			}
		}
	}

	private void readParametersFromNBT(NBTTagCompound nbt) {
		patternParameters = new IStatementParameter[nbt.getByte("length")];
		for (int i = 0; i < patternParameters.length; i++) {
			if (nbt.hasKey("p" + i)) {
				NBTTagCompound patternData = nbt.getCompoundTag("p" + i);
				patternParameters[i] = StatementManager.createParameter(patternData.getString("kind"));
				patternParameters[i].readFromNBT(patternData);
			}
		}
	}

	@Override
	public void writeData(ByteBuf data) {
		box.writeData(data);
		data.writeByte((done ? 1 : 0) | (excavate ? 2 : 0));
		NetworkUtils.writeUTF(data, currentPattern.getUniqueTag());

		NBTTagCompound parameterData = new NBTTagCompound();
		writeParametersToNBT(parameterData);
		NetworkUtils.writeNBT(data, parameterData);
	}

	@Override
	public void readData(ByteBuf data) {
		box.readData(data);
		int flags = data.readUnsignedByte();
		done = (flags & 1) > 0;
		excavate = (flags & 2) > 0;
		FillerPattern pattern = (FillerPattern) FillerManager.registry.getPattern(NetworkUtils.readUTF(data));
		NBTTagCompound parameterData = NetworkUtils.readNBT(data);
		readParametersFromNBT(parameterData);
		setPattern(pattern);

		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
	}

	@Override
	public boolean hasWork() {
		return !done && mode != Mode.Off;
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

	public void rpcSetPatternFromString(final String name) {
		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setPattern", new CommandWriter() {
			public void write(ByteBuf data) {
				NetworkUtils.writeUTF(data, name);
			}
		}));
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		super.receiveCommand(command, side, sender, stream);
		if (side.isServer()) {
			if ("setPattern".equals(command)) {
				String name = NetworkUtils.readUTF(stream);
				setPattern((FillerPattern) FillerManager.registry.getPattern(name));

				done = false;
			} else if ("setParameters".equals(command)) {
				NBTTagCompound patternData = NetworkUtils.readNBT(stream);
				readParametersFromNBT(patternData);

				currentTemplate = null;
				done = false;
			} else if ("setFlags".equals(command)) {
				excavate = stream.readBoolean();
				currentTemplate = null;

				sendNetworkUpdate();
				done = false;
			}
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
		return new Box(this).extendToEncompass(box).expand(50).getBoundingBox();
	}

	@Override
	public boolean isBuildingMaterialSlot(int i) {
		return true;
	}

	@Override
	public boolean acceptsControlMode(Mode mode) {
		return mode == IControllable.Mode.On ||
				mode == IControllable.Mode.Off ||
				mode == IControllable.Mode.Loop;
	}

	@Override
	public TileEntity getTile() {
		return this;
	}

	public void rpcSetParameter(int i, IStatementParameter patternParameter) {
		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setParameters", new CommandWriter() {
			public void write(ByteBuf data) {
				NBTTagCompound parameterData = new NBTTagCompound();
				writeParametersToNBT(parameterData);
				NetworkUtils.writeNBT(data, parameterData);
			}
		}));
	}

	@Override
	public int getLEDLevel(int led) {
		return (led == 0 ? done : buildersInAction.size() > 0) ? 15 : 0;
	}

	public void setExcavate(boolean excavate) {
		this.excavate = excavate;
	}
}
