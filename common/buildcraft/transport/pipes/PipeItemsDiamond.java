/**
 * BuildCraft is open-source. It is distributed under the terms of the
 * BuildCraft Open Source License. It grants rights to read, modify, compile
 * or run the code. It does *NOT* grant the right to redistribute this software
 * or its modifications in any form, binary or source, except if expressively
 * granted by the copyright holder.
 */

package buildcraft.transport.pipes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedList;

import cpw.mods.fml.common.FMLLog;

import net.minecraft.src.Item;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTBase;
import net.minecraft.src.NBTTagCompound;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.Orientations;
import buildcraft.api.core.Position;
import buildcraft.api.transport.IPipedItem;
import buildcraft.core.DefaultProps;
import buildcraft.core.network.IClientState;
import buildcraft.transport.IPipeTransportItemsHook;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeTransportItems;

public class PipeItemsDiamond extends Pipe implements IPipeTransportItemsHook, IClientState {

	public PipeItemsDiamond(int itemID) {
		super(new PipeTransportItems(), new PipeLogicDiamond(), itemID);
	}

	@Override
	public String getTextureFile() {
		return DefaultProps.TEXTURE_BLOCKS;
	}
	
	@Override
	public int getTextureIndex(Orientations direction) {
		if (direction == Orientations.Unknown){
			return 1 * 16 + 5;
		}
		return BuildCraftTransport.diamondTextures[direction.ordinal()];
	}

	@Override
	public LinkedList<Orientations> filterPossibleMovements(LinkedList<Orientations> possibleOrientations, Position pos,
			IPipedItem item) {
		LinkedList<Orientations> filteredOrientations = new LinkedList<Orientations>();
		LinkedList<Orientations> defaultOrientations = new LinkedList<Orientations>();

		// Filtered outputs
		for (Orientations dir : possibleOrientations) {
			boolean foundFilter = false;

			// NB: if there's several of the same match, the probability
			// to use that filter is higher, this is why there are
			// no breaks here.
			PipeLogicDiamond diamondLogic = (PipeLogicDiamond)logic;
			for (int slot = 0; slot < 9; ++slot) {
				ItemStack stack = diamondLogic.getStackInSlot(dir.ordinal() * 9 + slot);

				if (stack != null)
					foundFilter = true;

				if (stack != null && stack.itemID == item.getItemStack().itemID)
					if ((Item.itemsList[item.getItemStack().itemID].isDamageable()))
						filteredOrientations.add(dir);
					else if (stack.getItemDamage() == item.getItemStack().getItemDamage())
						filteredOrientations.add(dir);
			}
			if (!foundFilter)
				defaultOrientations.add(dir);
		}
		if (filteredOrientations.size() != 0)
			return filteredOrientations;
		else
			return defaultOrientations;
	}

	@Override
	public void entityEntered(IPipedItem item, Orientations orientation) {

	}

	@Override
	public void readjustSpeed(IPipedItem item) {
		((PipeTransportItems) transport).defaultReajustSpeed(item);
	}

	// ICLIENTSTATE
	@Override
	public void writeData(DataOutputStream data) throws IOException {
		NBTTagCompound nbt = new NBTTagCompound();
		((PipeLogicDiamond) logic).writeToNBT(nbt);
		NBTBase.writeNamedTag(nbt, data);
	}

	@Override
	public void readData(DataInputStream data) throws IOException {
		NBTBase nbt = NBTBase.readNamedTag(data);
		if (nbt instanceof NBTTagCompound) {
			logic.readFromNBT((NBTTagCompound) nbt);
		}
	}

}
