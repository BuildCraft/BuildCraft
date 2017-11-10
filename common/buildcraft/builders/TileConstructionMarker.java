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
import java.util.HashSet;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import cpw.mods.fml.relauncher.Side;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.Position;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.LaserData;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.builders.BuildingItem;
import buildcraft.core.builders.IBuildingItemsProvider;
import buildcraft.core.internal.IBoxProvider;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.network.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;

public class TileConstructionMarker extends TileBuildCraft implements IBuildingItemsProvider, IBoxProvider, ICommandReceiver {

	public static HashSet<TileConstructionMarker> currentMarkers = new HashSet<TileConstructionMarker>();

	public ForgeDirection direction = ForgeDirection.UNKNOWN;

	public LaserData laser;
	public ItemStack itemBlueprint;
	public Box box = new Box();

	public BptBuilderBase bluePrintBuilder;
	public BptContext bptContext;

	private ArrayList<BuildingItem> buildersInAction = new ArrayList<BuildingItem>();
	private NBTTagCompound initNBT;

	@Override
	public void initialize() {
		super.initialize();
		box.kind = Kind.BLUE_STRIPES;

		if (worldObj.isRemote) {
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "uploadBuildersInAction", null));
		}
	}

	private Packet createLaunchItemPacket(final BuildingItem i) {
		return new PacketCommand(this, "launchItem", new CommandWriter() {
			public void write(ByteBuf data) {
				i.writeData(data);
			}
		});
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		BuildingItem toRemove = null;

		for (BuildingItem i : buildersInAction) {
			i.update();

			if (i.isDone) {
				toRemove = i;
			}
		}

		if (toRemove != null) {
			buildersInAction.remove(toRemove);
		}

		if (worldObj.isRemote) {
			return;
		}

		if (itemBlueprint != null && ItemBlueprint.getId(itemBlueprint) != null && bluePrintBuilder == null) {
			BlueprintBase bpt = ItemBlueprint.loadBlueprint(itemBlueprint);
			if (bpt != null && bpt instanceof Blueprint) {
				bpt = bpt.adjustToWorld(worldObj, xCoord, yCoord, zCoord, direction);
				if (bpt != null) {
					bluePrintBuilder = new BptBuilderBlueprint((Blueprint) bpt, worldObj, xCoord, yCoord, zCoord);
					bptContext = bluePrintBuilder.getContext();
					box.initialize(bluePrintBuilder);
					sendNetworkUpdate();
				}
			} else {
				return;
			}
		}

		if (laser == null && direction != ForgeDirection.UNKNOWN) {
			laser = new LaserData();
			laser.head = new Position(xCoord + 0.5F, yCoord + 0.5F, zCoord + 0.5F);
			laser.tail = new Position(xCoord + 0.5F + direction.offsetX * 0.5F,
					yCoord + 0.5F + direction.offsetY * 0.5F,
					zCoord + 0.5F + direction.offsetZ * 0.5F);
			laser.isVisible = true;
			sendNetworkUpdate();
		}

		if (initNBT != null) {
			if (bluePrintBuilder != null) {
				bluePrintBuilder.loadBuildStateToNBT(initNBT.getCompoundTag("builderState"), this);
			}

			initNBT = null;
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);

		nbt.setByte("direction", (byte) direction.ordinal());

		if (itemBlueprint != null) {
			NBTTagCompound bptNBT = new NBTTagCompound();
			itemBlueprint.writeToNBT(bptNBT);
			nbt.setTag("itemBlueprint", bptNBT);
		}

		NBTTagCompound bptNBT = new NBTTagCompound();

		if (bluePrintBuilder != null) {
			NBTTagCompound builderCpt = new NBTTagCompound();
			bluePrintBuilder.saveBuildStateToNBT(builderCpt, this);
			bptNBT.setTag("builderState", builderCpt);
		}

		nbt.setTag("bptBuilder", bptNBT);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		direction = ForgeDirection.getOrientation(nbt.getByte("direction"));

		if (nbt.hasKey("itemBlueprint")) {
			itemBlueprint = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("itemBlueprint"));
		}

		// The rest of load has to be done upon initialize.
		initNBT = (NBTTagCompound) nbt.getCompoundTag("bptBuilder").copy();
	}

	public void setBlueprint(ItemStack currentItem) {
		itemBlueprint = currentItem;
		sendNetworkUpdate();
	}

	@Override
	public ArrayList<BuildingItem> getBuilders() {
		return buildersInAction;
	}

	@Override
	public void validate() {
		super.validate();
		if (!worldObj.isRemote) {
			currentMarkers.add(this);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		if (!worldObj.isRemote) {
			currentMarkers.remove(this);
		}
	}

	public boolean needsToBuild() {
		return !isInvalid() && bluePrintBuilder != null && !bluePrintBuilder.isDone(this);
	}

	public BptContext getContext() {
		return bptContext;
	}

	@Override
	public void addAndLaunchBuildingItem(BuildingItem item) {
		buildersInAction.add(item);
		BuildCraftCore.instance.sendToPlayersNear(createLaunchItemPacket(item), this);
	}

	@Override
	public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
		if (side.isServer() && "uploadBuildersInAction".equals(command)) {
			for (BuildingItem i : buildersInAction) {
				BuildCraftCore.instance.sendToPlayer((EntityPlayer) sender, createLaunchItemPacket(i));
			}
		} else if (side.isClient() && "launchItem".equals(command)) {
			BuildingItem item = new BuildingItem();
			item.readData(stream);
			buildersInAction.add(item);
		}
	}

	@Override
	public Box getBox() {
		return box;
	}

	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		Box renderBox = new Box(this).extendToEncompass(box);

		return renderBox.expand(50).getBoundingBox();
	}

	@Override
	public void writeData(ByteBuf stream) {
		box.writeData(stream);
		stream.writeByte((laser != null ? 1 : 0) | (itemBlueprint != null ? 2 : 0));
		if (laser != null) {
			laser.writeData(stream);
		}
		if (itemBlueprint != null) {
			NetworkUtils.writeStack(stream, itemBlueprint);
		}
	}

	@Override
	public void readData(ByteBuf stream) {
		box.readData(stream);
		int flags = stream.readUnsignedByte();
		if ((flags & 1) != 0) {
			laser = new LaserData();
			laser.readData(stream);
		} else {
			laser = null;
		}
		if ((flags & 2) != 0) {
			itemBlueprint = NetworkUtils.readStack(stream);
		} else {
			itemBlueprint = null;
		}
	}
}
