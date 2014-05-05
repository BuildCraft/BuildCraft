/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.LinkedList;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.IInvSlot;

/**
 * This class allow to specify specific behavior for blocks stored in
 * blueprints:
 *
 * - what items needs to be used to create that block - how the block has to be
 * built on the world - how to rotate the block - what extra data to store /
 * load in the blueprint
 *
 * Default implementations of this can be seen in the package
 * buildcraft.api.schematics. The class SchematicUtils provide some additional
 * utilities.
 *
 * Blueprints perform "id translation" in case the block ids between a blueprint
 * and the world installation are different. Mapping is done through the
 * builder context.
 *
 * At blueprint load time, BuildCraft will check that each block id of the
 * blueprint corresponds to the block id in the installation. If not, it will
 * perform a search through the block list, and upon matching signature, it will
 * translate all blocks ids of the blueprint to the installation ones. If no
 * such block id is found, BuildCraft will assume that the block is not
 * installed and will not load the blueprint.
 */
public abstract class Schematic {

	/**
	 * Blocks are build in various stages, in order to make sure that a block
	 * can indeed be placed, and that it's unlikely to disturb other blocks.
	 */
	public static enum BuildingStage {
		/**
		 * Standalone blocks can be placed in the air, and they don't change
		 * once placed.
		 */
		STANDALONE,

		/**
		 * Supported blocks may require to be placed on a standalone block,
		 * e.g. a torch.
		 */
		SUPPORTED,

		/**
		 * Expanding blocks will grow and may disturb other block locations,
		 * like e.g. water
		 */
		EXPANDING
	}

	/**
	 * Return true if the block on the world correspond to the block stored in
	 * the blueprint at the location given by the slot. By default, this
	 * subprogram is permissive and doesn't take into account metadata.
	 */
	public boolean isAlreadyBuilt(IBuilderContext context, int x, int y, int z) {
		return true;
	}

	/**
	 * Return true if the block should not be placed to the world. Requirements
	 * will not be asked on such a block, and building will not be called. Post
	 * processing will still be called on these blocks though.
	 */
	public boolean doNotBuild() {
		return false;
	}

	/**
	 * This is called each time an item matches a reqquirement, that is: (req id
	 * == stack id) for damageable items (req id == stack id && req dmg == stack
	 * dmg) for other items by default, it will increase damage of damageable
	 * items by the amount of damage of the requirement, and remove the intended
	 * amount of non damageable item.
	 *
	 * Client may override this behavior for default items. Note that this
	 * subprogram may be called twice with the same parameters, once with a copy
	 * of requirements and stack to check if the entire requirements can be
	 * fullfilled, and once with the real inventory. Implementer is responsible
	 * for updating req (with the remaining requirements if any) and stack
	 * (after usage)
	 *
	 * returns: what was used (similer to req, but created from stack, so that
	 * any NBT based differences are drawn from the correct source)
	 */
	public ItemStack useItem(IBuilderContext context, ItemStack req, IInvSlot slot) {
		ItemStack stack = slot.getStackInSlot();
		ItemStack result = stack.copy();

		if (stack.isItemStackDamageable()) {
			if (req.getItemDamage() + stack.getItemDamage() <= stack.getMaxDamage()) {
				stack.setItemDamage(req.getItemDamage() + stack.getItemDamage());
				result.setItemDamage(req.getItemDamage());
				req.stackSize = 0;
			}

			if (stack.getItemDamage() >= stack.getMaxDamage()) {
				slot.decreaseStackInSlot();
			}
		} else {
			if (stack.stackSize >= req.stackSize) {
				result.stackSize = req.stackSize;
				stack.stackSize -= req.stackSize;
				req.stackSize = 0;
			} else {
				req.stackSize -= stack.stackSize;
				stack.stackSize = 0;
			}
		}

		if (stack.stackSize == 0 && stack.getItem().getContainerItem() != null) {
			Item container = stack.getItem().getContainerItem();
			ItemStack newStack = new ItemStack(container);
			slot.setStackInSlot(newStack);
		} else if (stack.stackSize == 0) {
			slot.setStackInSlot(null);
		}

		return result;
	}

	/**
	 * Perform a 90 degree rotation to the slot.
	 */
	public void rotateLeft(IBuilderContext context) {

	}

	/**
	 * Performs a transformations from world to blueprints. In particular, it should:
	 * - use the registry to map ids from world to blueprints
	 * - apply translations to all positions in the schematic to center in the
	 *   blueprint referencial
	 */
	public void transformToBlueprint(MappingRegistry registry, Translation transform) {

	}

	/**
	 * Performs a transformations from blueprints to worlds. In particular, it should:
	 * - use the registry to map ids from blueprints to world
	 * - apply translations to all positions in the schematic to center in the
	 *   builder referencial
	 */
	public void transformToWorld(MappingRegistry registry, Translation transform) {

	}

	/**
	 * Places the block in the world, at the location specified in the slot,
	 * using the stack in parameters
	 */
	public void writeToWorld(IBuilderContext context, int x, int y, int z, LinkedList<ItemStack> stacks) {

	}

	/**
	 * Initializes a slot from the blueprint according to an objet placed on {x,
	 * y, z} on the world. This typically means adding entries in slot.cpt. Note
	 * that "id" and "meta" will be set automatically, corresponding to the
	 * block id and meta.
	 *
	 * By default, if the block is a BlockContainer, tile information will be to
	 * save / load the block.
	 */
	public void readFromWorld(IBuilderContext context, int x, int y, int z) {

	}

	/**
	 * Called on a block when the blueprint has finished to place all the
	 * blocks. This may be useful to adjust variable depending on surrounding
	 * blocks that may not be there already at initial building.
	 */
	public void postProcessing(IBuilderContext context, int x, int y, int z) {

	}

	public void readRequirementsFromWorld(IBuilderContext context, int x, int y, int z) {

	}

	/**
	 * Returns the requirements needed to build this block. When the
	 * requirements are met, they will be removed all at once from the builder,
	 * before calling buildBlock.
	 */
	public void writeRequirementsToBuilder(IBuilderContext context, LinkedList<ItemStack> requirements) {

	}

	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {

	}

	public void readFromNBT(NBTTagCompound nbt,	MappingRegistry registry) {

	}

	public void inventorySlotsToBlueprint (MappingRegistry registry, NBTTagCompound nbt) {
		inventorySlotsToBlueprint(registry, nbt, "Items");
	}

	public void inventorySlotsToBlueprint (MappingRegistry registry, NBTTagCompound nbt, String nbtName) {
		if (!nbt.hasKey(nbtName)) {
			return;
		}

		NBTTagList list = nbt.getTagList(nbtName,
				Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound invSlot = list.getCompoundTagAt(i);
            Item item = Item.getItemById(invSlot.getInteger ("id"));
            invSlot.setInteger("id", registry.getIdForItem(item));
		}
	}

	public void inventorySlotsToWorld (MappingRegistry registry, NBTTagCompound nbt) {
		inventorySlotsToWorld (registry, nbt, "Items");
	}

	public void inventorySlotsToWorld (MappingRegistry registry, NBTTagCompound nbt, String nbtName) {
		if (!nbt.hasKey(nbtName)) {
			return;
		}

		NBTTagList list = nbt.getTagList(nbtName,
				Constants.NBT.TAG_COMPOUND);

		for (int i = 0; i < list.tagCount(); ++i) {
            NBTTagCompound invSlot = list.getCompoundTagAt(i);
            Item item = registry.getItemForId(invSlot.getInteger ("id"));
            invSlot.setInteger("id", Item.getIdFromItem(item));
		}
	}

	public LinkedList<ItemStack> getStacksToDisplay(
			LinkedList<ItemStack> stackConsumed) {

		return stackConsumed;
	}

	/**
	 * Return the stage where this schematic has to be built.
	 */
	public BuildingStage getBuildStage () {
		return BuildingStage.STANDALONE;
	}

	/**
	 * Return the building permission for blueprint containing this schematic.
	 */
	public BuildingPermission getBuildingPermission () {
		return BuildingPermission.ALL;
	}

	/**
	 * Returns the amount of energy required to build this slot, depends on the
	 * stacks selected for the build.
	 */
	public double getEnergyRequirement(LinkedList<ItemStack> stacksUsed) {
		double result = 0;

		for (ItemStack s : stacksUsed) {
			result += s.stackSize * SchematicRegistry.BUILD_ENERGY;
		}

		return result;
	}
}
