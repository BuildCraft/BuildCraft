/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders;

import java.util.ArrayList;
import java.util.LinkedList;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.common.util.Constants;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.IAreaProvider;
import buildcraft.api.core.Position;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.IBoxProvider;
import buildcraft.core.LaserData;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.BlueprintReadConfiguration;
import buildcraft.core.blueprints.RecursiveBlueprintReader;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.ICommandReceiver;
import buildcraft.core.network.PacketCommand;
import buildcraft.core.utils.Utils;

public class TileArchitect extends TileBuildCraft implements IInventory, IBoxProvider, ICommandReceiver {

	public String currentAuthorName = "";

	public Box box = new Box();
	public String name = "";
	public BlueprintReadConfiguration readConfiguration = new BlueprintReadConfiguration();

	public LinkedList<LaserData> subLasers = new LinkedList<LaserData>();

	public ArrayList<BlockPos> subBlueprints = new ArrayList<BlockPos>();

	private SimpleInventory inv = new SimpleInventory(2, "Architect", 1);

	private RecursiveBlueprintReader reader;

	public TileArchitect() {
		box.kind = Kind.BLUE_STRIPES;
	}

	@Override
	public void update() {
		super.update();

		if (!worldObj.isRemote) {
			if (reader != null) {
				reader.iterate();

				if (reader.isDone()) {
					reader = null;
				}
			}
		}
	}

	@Override
	public void initialize() {
		super.initialize();

		if (!worldObj.isRemote) {
			if (!box.isInitialized()) {
				IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, pos);

				if (a != null) {
					box.initialize(a);
					a.removeFromWorld();
					sendNetworkUpdate();
				}
			}
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
			initializeComputing();
		}

		return result;
	}

	@Override
	public void setInventorySlotContents(int i, ItemStack itemstack) {
		inv.setInventorySlotContents(i, itemstack);

		if (i == 0) {
			initializeComputing();
		}
	}

	@Override
	public ItemStack getStackInSlotOnClosing(int slot) {
		return inv.getStackInSlotOnClosing(slot);
	}

	@Override
	public int getInventoryStackLimit() {
		return 1;
	}

	@Override
	public void openInventory(EntityPlayer playerIn) {

	}

	@Override
	public void closeInventory(EntityPlayer playerIn) {

	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		if (nbt.hasKey("box")) {
			box.initialize(nbt.getCompoundTag("box"));
		}

		inv.readFromNBT(nbt);

		name = nbt.getString("name");
		currentAuthorName = nbt.getString("lastAuthor");

		if (nbt.hasKey("readConfiguration")) {
			readConfiguration.readFromNBT(nbt.getCompoundTag("readConfiguration"));
		}

		NBTTagList subBptList = nbt.getTagList("subBlueprints", Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < subBptList.tagCount(); ++i) {
			BlockPos index = Utils.readBlockPos(subBptList.getCompoundTagAt(i));

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

		nbt.setString("name", name);
		nbt.setString("lastAuthor", currentAuthorName);

		NBTTagCompound readConf = new NBTTagCompound();
		readConfiguration.writeToNBT(readConf);
		nbt.setTag("readConfiguration", readConf);

		NBTTagList subBptList = new NBTTagList();

		for (BlockPos b : subBlueprints) {
			NBTTagCompound subBpt = new NBTTagCompound();
			Utils.writeBlockPos(subBpt, b);
			subBptList.appendTag(subBpt);
		}

		nbt.setTag("subBlueprints", subBptList);
	}

	@Override
	public void writeData(ByteBuf stream) {
		box.writeData(stream);
		Utils.writeUTF(stream, name);
		readConfiguration.writeData(stream);
		stream.writeShort(subLasers.size());
		for (LaserData ld: subLasers) {
			ld.writeData(stream);
		}
	}

	@Override
	public void readData(ByteBuf stream) {
		box.readData(stream);
		name = Utils.readUTF(stream);
		readConfiguration.readData(stream);
		int size = stream.readUnsignedShort();
		subLasers.clear();
		for (int i = 0; i < size; i++) {
			LaserData ld = new LaserData();
			ld.readData(stream);
			subLasers.add(ld);
		}
	}
	@Override
	public void invalidate() {
		super.invalidate();
		destroy();
	}

	private void initializeComputing() {
		if (worldObj.isRemote) {
			return;
		}

		reader = new RecursiveBlueprintReader(this);
	}

	public int getComputingProgressScaled(int scale) {
		if (reader != null) {
			return (int) (reader.getComputingProgressScaled() * scale);
		} else {
			return 0;
		}
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

	public BuildCraftPacket getPacketSetName() {
		return new PacketCommand(this, "setName", new CommandWriter() {
			public void write(ByteBuf data) {
				Utils.writeUTF(data, name);
			}
		});
	}
	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if ("setName".equals(command)) {
			this.name = Utils.readUTF(stream);
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

	public void rpcSetConfiguration (BlueprintReadConfiguration conf) {
		readConfiguration = conf;

		BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setReadConfiguration", new CommandWriter() {
			public void write(ByteBuf data) {
				readConfiguration.writeData(data);
			}
		}));
	}

	public void addSubBlueprint(TileEntity sub) {
		addSubBlueprint(sub.getPos());

		sendNetworkUpdate();
	}

	private void addSubBlueprint(BlockPos index) {
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

	public String getName() {
		return "Template";
	}
}