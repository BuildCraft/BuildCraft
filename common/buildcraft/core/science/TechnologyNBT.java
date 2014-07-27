/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */

package buildcraft.core.science;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IInvSlot;
import buildcraft.core.inventory.InventoryIterator;
import buildcraft.core.inventory.SimpleInventory;
import buildcraft.core.inventory.StackHelper;
import buildcraft.core.utils.NBTUtils;

public final class TechnologyNBT {

	private static HashMap<String, TechnologyNBT> technologyBooksClient = new HashMap<String, TechnologyNBT>();
	private static HashMap<String, TechnologyNBT> technologyBooksServer = new HashMap<String, TechnologyNBT>();
	private static int globalId = 0;

	private static int DEFAULT_PROCESS_TIME = 10 * 20;

	public SimpleInventory inventory = new SimpleInventory(3, "mainInv", 999);
	public SimpleInventory leftToCompute = new SimpleInventory(3, "leftInv", 999);

	private HashSet<String> foundTechnologies = new HashSet<String>();
	private String researchedTechnology;
	private ItemStack itemInComputation;

	private int progress = 0;
	private int processTime = 0;
	private String id;

	private TechnologyNBT(NBTTagCompound nbt) {
		loadFromNBT(nbt);

		if (!foundTechnologies.contains(Tier.WoodenGear.getTechnology().getID())) {
			foundTechnologies.add(Tier.WoodenGear.getTechnology().getID());
		}
	}

	public boolean isKnown(Technology t) {
		return foundTechnologies.contains(t.getID());
	}

	public boolean canBeResearched(Technology t) {
		if (foundTechnologies.contains(t.getID())) {
			return false;
		}

		for (Technology p : t.getPrerequisites()) {
			if (!isKnown(p)) {
				return false;
			}
		}

		return true;
	}

	public void update() {
		if (researchedTechnology != null) {
			if (itemInComputation != null) {
				if (progress < processTime) {
					progress++;
				} else {
					for (IInvSlot slot : InventoryIterator.getIterable(leftToCompute, ForgeDirection.UNKNOWN)) {
						if (StackHelper.isMatchingItem(itemInComputation, slot.getStackInSlot())) {
							slot.decreaseStackInSlot(1);
							break;
						}
					}

					progress = 0;
					processTime = 0;
					itemInComputation = null;

					boolean workToDo = false;

					for (IInvSlot slot : InventoryIterator.getIterable(leftToCompute, ForgeDirection.UNKNOWN)) {
						if (slot.getStackInSlot() != null) {
							workToDo = true;
						}
					}

					if (!workToDo) {
						foundTechnologies.add(researchedTechnology);
						researchedTechnology = null;
					}
				}
			} else {
				for (IInvSlot invSlot : InventoryIterator.getIterable(inventory, ForgeDirection.UNKNOWN)) {
					for (IInvSlot reqSlot : InventoryIterator.getIterable(leftToCompute, ForgeDirection.UNKNOWN)) {
						if (invSlot.getStackInSlot() != null
							&& reqSlot.getStackInSlot() != null
								&& StackHelper.isMatchingItem(invSlot.getStackInSlot(), reqSlot.getStackInSlot())) {

							itemInComputation = invSlot.decreaseStackInSlot(1);
							progress = 0;

							if (itemInComputation.getItem() instanceof IItemTechnologyProvider) {
								processTime = ((IItemTechnologyProvider) itemInComputation.getItem()).timeToProcess();
							} else {
								processTime = DEFAULT_PROCESS_TIME;
							}

							break;
						}
					}

					if (itemInComputation != null) {
						break;
					}
				}
			}
		}
	}

	public void writeToNBT(NBTTagCompound nbt) {
		NBTTagList foundNBT = new NBTTagList();

		for (String s : foundTechnologies) {
			foundNBT.appendTag(new NBTTagString(s));
		}

		nbt.setTag("foundTechnologies", foundNBT);

		if (researchedTechnology != null) {
			nbt.setString("researchedTechnology", researchedTechnology);
		}

		NBTTagCompound inventoryNBT = new NBTTagCompound();
		inventory.writeToNBT(inventoryNBT);
		nbt.setTag("inventory", inventoryNBT);

		NBTTagCompound leftToComputeNBT = new NBTTagCompound();
		leftToCompute.writeToNBT(leftToComputeNBT);
		nbt.setTag("leftTocompute", leftToComputeNBT);

		if (itemInComputation != null) {
			NBTTagCompound itemInComputationNBT = new NBTTagCompound();
			itemInComputation.writeToNBT(itemInComputationNBT);
			nbt.setTag("itemInComputation", itemInComputationNBT);
		}

		nbt.setInteger("progress", progress);
		nbt.setInteger("processTime", processTime);

		nbt.setString("id", id);
	}

	public void loadFromNBT(NBTTagCompound nbt) {
		NBTTagList foundNBT = nbt.getTagList("foundTechnologies", Constants.NBT.TAG_STRING);

		for (int i = 0; i < foundNBT.tagCount(); ++i) {
			foundTechnologies.add(foundNBT.getStringTagAt(i));
		}

		if (nbt.hasKey("researchedTechnology")) {
			researchedTechnology = nbt.getString("researchedTechnology");
		} else {
			researchedTechnology = null;
		}

		inventory.readFromNBT(nbt.getCompoundTag("inventory"));

		leftToCompute.readFromNBT(nbt.getCompoundTag("leftTocompute"));

		if (nbt.hasKey("itemInComputation")) {
			itemInComputation = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("itemInComputation"));
		} else {
			itemInComputation = null;
		}

		progress = nbt.getInteger("progress");
		processTime = nbt.getInteger("processTime");

		id = nbt.getString("id");
	}

	public float getProgress() {
		if (processTime == 0) {
			return 0;
		} else {
			return (float) progress / (float) processTime;
		}
	}

	public static TechnologyNBT getTechnology(EntityPlayer holder, ItemStack stack) {
		HashMap<String, TechnologyNBT> technologyBooks;

		if (holder.worldObj.isRemote) {
			technologyBooks = technologyBooksClient;
		} else {
			technologyBooks = technologyBooksServer;
		}

		NBTTagCompound nbt = NBTUtils.getItemData(stack);

		if (!nbt.hasKey("id")
				|| "".equals(nbt.getString("id"))) {
			String id = holder.getDisplayName() + "-"
					+ Long.toHexString((new Date()).getTime())
					+ "-" + globalId++;

			nbt.setString("id", id);

			TechnologyNBT techno = new TechnologyNBT(nbt);
			technologyBooks.put(id, techno);

			return techno;
		} else {
			String id = nbt.getString("id");

			if (technologyBooks.containsKey(id)) {
				return technologyBooks.get(id);
			} else {
				TechnologyNBT techno = new TechnologyNBT(nbt);
				technologyBooks.put(id, techno);

				return techno;
			}
		}
	}

	public void startResearch(Technology t) {
		researchedTechnology = t.getID();

		for (int i = 0; i < 3; ++i) {
			if (t.getRequirements()[i] != null) {
				leftToCompute.setInventorySlotContents(i, t.getRequirements()[i].copy());
			}
		}
	}

	public Technology getResearchedTechnology() {
		if (researchedTechnology != null) {
			return Technology.getTechnology(researchedTechnology);
		} else {
			return null;
		}
	}
}
