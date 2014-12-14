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
import java.util.HashSet;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraft.util.EnumFacing;
import buildcraft.BuildCraftCore;
import buildcraft.api.core.Position;
import buildcraft.core.Box;
import buildcraft.core.Box.Kind;
import buildcraft.core.IBoxProvider;
import buildcraft.core.LaserData;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.builders.BuildingItem;
import buildcraft.core.builders.IBuildingItemsProvider;
import buildcraft.core.network.BuildCraftPacket;
import buildcraft.core.network.CommandWriter;
import buildcraft.core.network.ICommandReceiver;
import buildcraft.core.network.PacketCommand;
import buildcraft.core.utils.Utils;

public class TileConstructionMarker extends TileBuildCraft implements IBuildingItemsProvider, IBoxProvider, ICommandReceiver {

	public static HashSet<TileConstructionMarker> currentMarkers = new HashSet<TileConstructionMarker>();

	public EnumFacing direction = null;

	public LaserData laser;
	public ItemStack itemBlueprint;
	public Box box = new Box();

	public BptBuilderBase bluePrintBuilder;
	public BptContext bptContext;

	private ArrayList<BuildingItem> buildersInAction = new ArrayList<BuildingItem>();
	private NBTTagCompound initNBT;

	@Override
	public void initialize () {
		super.initialize();
		box.kind = Kind.BLUE_STRIPES;

		if (worldObj.isRemote) {
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "uploadBuildersInAction", null));
		}
	}

	private BuildCraftPacket createLaunchItemPacket(final BuildingItem i) {
		return new PacketCommand(this, "launchItem", new CommandWriter() {
			public void write(ByteBuf data) {
				i.writeData(data);
			}
		});
	}

	@Override
	public void update() {
		super.update();

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
			BlueprintBase bpt = BlueprintBase.instantiate(itemBlueprint, worldObj, pos, direction);

			if (bpt instanceof Blueprint) {
				bluePrintBuilder = new BptBuilderBlueprint((Blueprint) bpt, worldObj, pos);
				bptContext = bluePrintBuilder.getContext();
				box.initialize(bluePrintBuilder);
				sendNetworkUpdate();
			} else {
				return;
			}
		}

		if (laser == null && direction != null) {
			laser = new LaserData();
			laser.head = new Position(pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F);
			laser.tail = new Position(pos.getX() + 0.5F + direction.getFrontOffsetX() * 0.5F,
					pos.getY() + 0.5F + direction.getFrontOffsetY() * 0.5F,
					pos.getZ() + 0.5F + direction.getFrontOffsetZ() * 0.5F);
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

		nbt.setByte("direction", (byte) (direction != null ? direction.ordinal() : 6));

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

		if (nbt.getByte("direction") <= 5) {
			direction = EnumFacing.getFront(nbt.getByte("direction"));
		} else {
			direction = null;
		}

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
		if (!worldObj.isRemote) {
			currentMarkers.add(this);
		}
	}

	@Override
	public void invalidate() {
		if (!worldObj.isRemote) {
			currentMarkers.remove(this);
		}
	}

	public boolean needsToBuild() {
		return bluePrintBuilder != null && !bluePrintBuilder.isDone(this);
	}

	public BptContext getContext () {
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
			BuildCraftCore.instance.sendToServer(new PacketCommand(this, "uploadBuildersInAction", null));
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
			Utils.writeStack(stream, itemBlueprint);
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
			itemBlueprint = Utils.readStack(stream);
		} else {
			itemBlueprint = null;
		}
	}
}
