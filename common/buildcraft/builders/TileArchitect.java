/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.ArrayList;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.Constants;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BlockIndex;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.Position;
import buildcraft.builders.blueprints.RecursiveBlueprintReader;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.LaserData;
import buildcraft.core.blueprints.BlueprintReadConfiguration;
import buildcraft.core.internal.IBoxProvider;
import buildcraft.core.internal.ILEDProvider;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;

public class TileArchitect extends TileBuildCraft implements IInventory, IBoxProvider, ICommandReceiver, ILEDProvider {


	public enum Mode {
		NONE, EDIT, COPY
	}

	public String currentAuthorName = "";
	public Mode mode = Mode.NONE;

	public Box box = new Box();
	public String name = "";
	public BlueprintReadConfiguration readConfiguration = new BlueprintReadConfiguration();

	public ArrayList<LaserData> subLasers = new ArrayList<LaserData>();
	public ArrayList<BlockIndex> subBlueprints = new ArrayList<BlockIndex>();

	private SimpleInventory inv = new SimpleInventory(2, "Architect", 1);
	private RecursiveBlueprintReader reader;
	private boolean clientIsWorking, initialized;

	public TileArchitect() {
		box.kind = Kind.BLUE_STRIPES;
	}

	public void storeBlueprintStack(ItemStack blueprintStack) {
		setInventorySlotContents(1, blueprintStack);
		decrStackSize(0, 1);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (!worldObj.isRemote) {
			if (mode == Mode.COPY && reader != null) {
				reader.iterate();

				if (reader.isDone()) {
					reader = null;
					sendNetworkUpdate();
				}
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!worldObj.isRemote && !initialized) {
			if (!box.isInitialized()) {
				IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, xCoord,
						yCoord, zCoord);

				if (a != null) {
					mode = Mode.COPY;
					box.initialize(a);
					a.removeFromWorld();
					sendNetworkUpdate();
					return;
				} else {
					if (BuildCraftCore.DEVELOPER_MODE) {
						mode = Mode.EDIT;
					} else {
						mode = Mode.NONE;
					}
				}
			} else {
				mode = Mode.COPY;
			}
			initialized = true;
			sendNetworkUpdate();
		}
	}

	@Override
	public int getSizeInventory() {
		return 2;
	}

	@Override
	public ItemStack getStackInSlot(int i) {
		return inv.getStackInSlot(i);
	}

	@Override
	public ItemStack decrStackSize(int i, int j) {
		ItemStack result = inv.decrStackSize(i, j);

		if (i == 0) {
			initializeBlueprint();
		}

		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv.setInventorySlotContents(i, itemstack);

		if (i == 0) {
			initializeBlueprint();
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inv.getStackInSlotOnClosing(slot);
	}

	@Override
	public String getInventoryName() {
		return "Template";
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public boolean isUseableByPlayer(EntityPlayer entityplayer) {
		return mode != Mode.NONE && worldObj.getTileEntity(xCoord, yCoord, zCoord) == this;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if (nbt.hasKey("box")) {
			box.initialize(nbt.getCompoundTag("box"));
		}

		inv.readFromNBT(nbt);

		mode = Mode.values()[nbt.getByte("mode")];
		name = nbt.getString("name");
		currentAuthorName = nbt.getString("lastAuthor");

		if (nbt.hasKey("readConfiguration")) {
			readConfiguration.readFromNBT(nbt.getCompoundTag("readConfiguration"));
		}

		NBTTagList subBptList = nbt.getTagList("subBlueprints", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < subBptList.tagCount(); ++i) {
			BlockIndex index = new BlockIndex(subBptList.getCompoundTagAt(i));

			addSubBlueprint(index);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		if (box.isInitialized()) {
			NBTTagCompound boxStore = new NBTTagCompound();
			box.writeToNBT(boxStore);
			nbt.setTag("box", boxStore);
		}

		inv.writeToNBT(nbt);

		nbt.setByte("mode", (byte) mode.ordinal());
		nbt.setString("name", name);
		nbt.setString("lastAuthor", currentAuthorName);

		NBTTagCompound readConf = new NBTTagCompound();
		readConfiguration.writeToNBT(readConf);
		nbt.setTag("readConfiguration", readConf);

		NBTTagList subBptList = new NBTTagList();

		for (BlockIndex b : subBlueprints) {
			NBTTagCompound subBpt = new NBTTagCompound();
			b.writeTo(subBpt);
			subBptList.appendTag(subBpt);
		}

		nbt.setTag("subBlueprints", subBptList);
	}

	private boolean getIsWorking() {
		return mode == Mode.COPY ? reader != null : false;
	}

	@Override
	public void writeData(ByteBuf stream) {
		box.writeData(stream);
		NetworkUtils.writeUTF(stream, name);
		stream.writeBoolean(getIsWorking());
		stream.writeByte(mode.ordinal());
		if (mode == Mode.COPY) {
			readConfiguration.writeData(stream);
			stream.writeShort(subLasers.size());
			for (LaserData ld : subLasers) {
				ld.writeData(stream);
			}
		}
	}

	@Override
	public void readData(ByteBuf stream) {
		box.readData(stream);
		name = NetworkUtils.readUTF(stream);
		clientIsWorking = stream.readBoolean();
		mode = Mode.values()[stream.readByte()];

		if (mode == Mode.COPY) {
			readConfiguration.readData(stream);
			int size = stream.readUnsignedShort();
			subLasers.clear();
			for (int i = 0; i < size; i++) {
				LaserData ld = new LaserData();
				ld.readData(stream);
				subLasers.add(ld);
			}
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	private void initializeBlueprint() {
		if (getWorldObj().isRemote) {
			return;
		}

		if (mode == Mode.COPY) {
			reader = new RecursiveBlueprintReader(this);
		}
		sendNetworkUpdate();
	}

	public int getComputingProgressScaled(int scale) {
		if (reader != null) {
			return (int) (reader.getComputingProgressScaled() * scale);
		} else {
			return 0;
		}
	}

	@Override
	public void openInventory() {
	}

	@Override
	public void closeInventory() {
	}

	@Override
	public boolean hasCustomInventoryName() {
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return false;
	}

	@Override
	public Box getBox() {
		return box;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		Box completeBox = new Box(this).extendToEncompass(box);

		for (LaserData d : subLasers) {
			completeBox.extendToEncompass(d.tail);
		}

		return completeBox.getBoundingBox();
	}

	public Packet getPacketSetName() {
		return new PacketCommand(this, "setName", new CommandWriter() {
			public void write(ByteBuf data) {
				NetworkUtils.writeUTF(data, name);
			}
		});
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if ("setName".equals(command)) {
			this.name = NetworkUtils.readUTF(stream);
			if (side.isServer()) {
				BuildCraftCore.instance.sendToPlayersNear(getPacketSetName(), this);
			}
		} else if (side.isServer()) {
			if ("setReadConfiguration".equals(command)) {
				readConfiguration.readData(stream);
				sendNetworkUpdate();
			}
		}
	}

	public void rpcSetConfiguration(BlueprintReadConfiguration conf) {
		readConfiguration = conf;

		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setReadConfiguration", new CommandWriter() {
			public void write(ByteBuf data) {
				readConfiguration.writeData(data);
			}
		}));
	}

	public void addSubBlueprint(TileEntity sub) {
		if (mode == Mode.COPY) {
			addSubBlueprint(new BlockIndex(sub));
			sendNetworkUpdate();
		}
	}

	private void addSubBlueprint(BlockIndex index) {
		subBlueprints.add(index);

		LaserData laser = new LaserData(new Position(index), new Position(this));

		laser.head.x += 0.5F;
		laser.head.y += 0.5F;
		laser.head.z += 0.5F;

		laser.tail.x += 0.5F;
		laser.tail.y += 0.5F;
		laser.tail.z += 0.5F;

		subLasers.add(laser);
	}

	@Override
	public int getLEDLevel(int led) {
		boolean condition = false;
		switch (led) {
			case 0:
				condition = clientIsWorking;
				break;
			case 1:
				condition = mode == Mode.COPY && box != null && box.isInitialized();
				break;
			case 2:
				condition = mode == Mode.EDIT;
				break;
		}
		return condition ? 15 : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return Double.MAX_VALUE;
	}
}