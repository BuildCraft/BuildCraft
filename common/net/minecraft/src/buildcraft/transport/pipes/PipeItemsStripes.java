/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package net.minecraft.src.buildcraft.transport.pipes;

import java.util.ArrayList;
import java.util.TreeMap;

import net.minecraft.src.BuildCraftBlockUtil;
import net.minecraft.src.BuildCraftTransport;
import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.BuildCraftAPI;
import net.minecraft.src.buildcraft.api.EntityPassiveItem;
import net.minecraft.src.buildcraft.api.IPowerReceptor;
import net.minecraft.src.buildcraft.api.Orientations;
import net.minecraft.src.buildcraft.api.Position;
import net.minecraft.src.buildcraft.api.PowerFramework;
import net.minecraft.src.buildcraft.api.PowerProvider;
import net.minecraft.src.buildcraft.core.Utils;
import net.minecraft.src.buildcraft.transport.BlockGenericPipe;
import net.minecraft.src.buildcraft.transport.EntityData;
import net.minecraft.src.buildcraft.transport.IItemTravelingHook;
import net.minecraft.src.buildcraft.transport.ItemPipe;
import net.minecraft.src.buildcraft.transport.Pipe;
import net.minecraft.src.buildcraft.transport.PipeLogicStripes;
import net.minecraft.src.buildcraft.transport.PipeTransportItems;

public class PipeItemsStripes extends Pipe implements IItemTravelingHook, IPowerReceptor {

	private PowerProvider powerProvider;

	public PipeItemsStripes(int itemID) {
		super(new PipeTransportItems(), new PipeLogicStripes(), itemID);

		((PipeTransportItems) transport).travelHook = this;

		powerProvider = PowerFramework.currentFramework.createPowerProvider();
		powerProvider.configure(25, 1, 1, 1, 1);
		powerProvider.configurePowerPerdition(1, 1);
	}

	@Override
	public int getMainBlockTexture() {
		return 16 * 7 + 14;
	}

	@Override
	public void doWork() {
		if (powerProvider.useEnergy(1, 1, true) == 1) {
			Orientations o = getOpenOrientation();

			if (o != Orientations.Unknown) {
				Position p = new Position (xCoord, yCoord, zCoord, o);
				p.moveForwards(1.0);

				ArrayList <ItemStack> stacks = BuildCraftBlockUtil.getItemStackFromBlock(worldObj, (int) p.x, (int) p.y, (int)p.z);

				if (stacks != null)
					for (ItemStack s : stacks)
						if (s != null) {
							EntityPassiveItem newItem = new EntityPassiveItem(worldObj,
									xCoord + 0.5, yCoord + Utils.getPipeFloorOf(s),
									zCoord + 0.5, s);

							this.container.entityEntering(newItem, o.reverse());
						}

				worldObj.setBlock((int) p.x, (int) p.y, (int)p.z, 0);
			}
		}

	}

	@Override
	public void drop(PipeTransportItems pipe, EntityData data) {
		Position p = new Position (xCoord, yCoord, zCoord, data.orientation);
		p.moveForwards(1.0);

		if (convertPipe(pipe, data))
			BuildCraftTransport.pipeItemsStipes.onItemUse(new ItemStack(
					BuildCraftTransport.pipeItemsStipes), BuildCraftAPI
					.getBuildCraftPlayer(worldObj), worldObj, (int) p.x,
					(int) p.y - 1, (int) p.z, 1);
		else if (worldObj.getBlockId((int) p.x, (int) p.y, (int) p.z) == 0)
			data.item.item.getItem().onItemUse(data.item.item,
					BuildCraftAPI.getBuildCraftPlayer(worldObj), worldObj, (int) p.x,
					(int) p.y - 1, (int) p.z, 1);
		else
			data.item.item.getItem().onItemUse(data.item.item,
					BuildCraftAPI.getBuildCraftPlayer(worldObj), worldObj, (int) p.x,
					(int) p.y, (int) p.z, 1);
	}

	@Override
	public void centerReached(PipeTransportItems pipe, EntityData data) {
		convertPipe(pipe, data);
	}

	@SuppressWarnings("unchecked")
	public boolean convertPipe (PipeTransportItems pipe, EntityData data) {
		if (data.item.item.getItem() instanceof ItemPipe)
			if (!(data.item.item.itemID == BuildCraftTransport.pipeItemsStipes.shiftedIndex)) {
				Pipe newPipe = BlockGenericPipe.createPipe(worldObj, xCoord, yCoord, zCoord, data.item.item.itemID);
				newPipe.setTile(this.container);
				newPipe.setWorld(worldObj);
				this.container.pipe = newPipe;
				((PipeTransportItems) newPipe.transport).travelingEntities = (TreeMap<Integer, EntityData>) pipe.travelingEntities
						.clone();

				data.item.item.stackSize--;

				if (data.item.item.stackSize <= 0)
					((PipeTransportItems) newPipe.transport).travelingEntities.remove(data.item.entityId);

				pipe.scheduleRemoval(data.item);

				return true;
			}

		return false;
	}


	@Override
	public void setPowerProvider(PowerProvider provider) {
		powerProvider = provider;
	}

	@Override
	public PowerProvider getPowerProvider() {
		return powerProvider;
	}

	@Override
	public int powerRequest() {
		return getPowerProvider().maxEnergyReceived;
	}

	@Override
	public void endReached(PipeTransportItems pipe, EntityData data,
			TileEntity tile) {

	}
}
