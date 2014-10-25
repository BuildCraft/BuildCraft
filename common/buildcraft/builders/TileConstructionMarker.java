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

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.core.NetworkData;
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
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCMessageInfo;
import buildcraft.core.network.RPCSide;

public class TileConstructionMarker extends TileBuildCraft implements IBuildingItemsProvider, IBoxProvider {

	public static HashSet<TileConstructionMarker> currentMarkers = new HashSet<TileConstructionMarker>();

	public ForgeDirection direction = ForgeDirection.UNKNOWN;

	@NetworkData
	public LaserData laser;

	@NetworkData
	public ItemStack itemBlueprint;

	@NetworkData
	public Box box = new Box();

	public BptBuilderBase bluePrintBuilder;
	public BptContext bptContext;

	private ArrayList<BuildingItem> buildersInAction = new ArrayList<BuildingItem>();
	private NBTTagCompound initNBT;

	@Override
	public void initialize() {
		box.kind = Kind.BLUE_STRIPES;

		if (worldObj.isRemote) {
			RPCHandler.rpcServer(this, "uploadBuildersInAction");
		}
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
			BlueprintBase bpt = BlueprintBase.instantiate(itemBlueprint, worldObj, xCoord, yCoord, zCoord, direction);

			bluePrintBuilder = new BptBuilderBlueprint((Blueprint) bpt, worldObj, xCoord, yCoord, zCoord);
			bptContext = bluePrintBuilder.getContext();
			box.initialize(bluePrintBuilder);
			sendNetworkUpdate();
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
		RPCHandler.rpcBroadcastWorldPlayers(worldObj, this, "launchItem", item);
	}

	@RPC(RPCSide.CLIENT)
	private void launchItem(BuildingItem item) {
		buildersInAction.add(item);
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

	@RPC(RPCSide.SERVER)
	private void uploadBuildersInAction(RPCMessageInfo info) {
		for (BuildingItem i : buildersInAction) {
			RPCHandler.rpcPlayer(info.sender, this, "launchItem", i);
		}
	}
}
