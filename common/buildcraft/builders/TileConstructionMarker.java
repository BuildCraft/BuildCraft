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

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.NetworkData;
import buildcraft.api.core.Position;
import buildcraft.core.LaserData;
import buildcraft.core.TileBuildCraft;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BptBuilderBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.network.RPC;
import buildcraft.core.network.RPCHandler;
import buildcraft.core.network.RPCSide;

public class TileConstructionMarker extends TileBuildCraft implements IBuildingItemsProvider {

	public static HashSet<TileConstructionMarker> currentMarkers = new HashSet<TileConstructionMarker>();

	public ForgeDirection direction = ForgeDirection.UNKNOWN;

	@NetworkData
	public LaserData laser;

	@NetworkData
	public ItemStack itemBlueprint;

	public BptBuilderBase bluePrintBuilder;
	public BptContext bptContext;

	private ArrayList<BuildingItem> buildersInAction = new ArrayList<BuildingItem>();
	private NBTTagCompound initNBT;

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (worldObj.isRemote) {
			return;
		}

		if (itemBlueprint != null && bluePrintBuilder == null) {
			bluePrintBuilder = new BptBuilderBlueprint((Blueprint) ItemBlueprint.loadBlueprint(itemBlueprint),
					worldObj, xCoord, yCoord, zCoord);
			bptContext = bluePrintBuilder.getContext();
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

		nbt.setInteger("direction", direction.ordinal());

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

		direction = ForgeDirection.values()[nbt.getInteger("direction")];

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
	public void launchItem(BuildingItem item) {
		buildersInAction.add(item);
	}
}
