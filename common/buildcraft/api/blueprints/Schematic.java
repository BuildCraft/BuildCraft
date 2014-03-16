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
public class Schematic {

	@SuppressWarnings("unchecked")
	@Override
	public Schematic clone() {
		Schematic obj;

		try {
			obj = (Schematic) super.clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}

		return obj;
	}

	public final LinkedList<ItemStack> getRequirements(IBuilderContext context) {
		LinkedList<ItemStack> res = new LinkedList<ItemStack>();

		addRequirements(context, res);

		return res;
	}


	/**
	 * Returns the requirements needed to build this block. When the
	 * requirements are met, they will be removed all at once from the builder,
	 * before calling buildBlock.
	 */
	public void addRequirements(IBuilderContext context, LinkedList<ItemStack> requirements) {

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
	public ItemStack useItem(IBuilderContext context, ItemStack req, ItemStack stack) {
		ItemStack result = stack.copy();
		if (stack.isItemStackDamageable()) {
			if (req.getItemDamage() + stack.getItemDamage() <= stack.getMaxDamage()) {
				stack.setItemDamage(req.getItemDamage() + stack.getItemDamage());
				result.setItemDamage(req.getItemDamage());
				req.stackSize = 0;
			}

			if (stack.getItemDamage() >= stack.getMaxDamage()) {
				stack.stackSize = 0;
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

			//stack.itemID = container.itemID;
			stack.stackSize = 1;
			stack.setItemDamage(0);
		}
		return result;
	}

	/**
	 * Return true if the block on the world correspond to the block stored in
	 * the blueprint at the location given by the slot. By default, this
	 * subprogram is permissive and doesn't take into account metadata.
	 */
	public boolean isValid(IBuilderContext context, int x, int y, int z) {
		return true;
	}

	/**
	 * Perform a 90 degree rotation to the slot.
	 */
	public void rotateLeft(IBuilderContext context) {

	}

	/**
	 * Places the block in the world, at the location specified in the slot.
	 */
	public void writeToWorld(IBuilderContext context, int x, int y, int z) {

	}

	/**
	 * Return true if the block should not be placed to the world. Requirements
	 * will not be asked on such a block, and building will not be called.
	 */
	public boolean ignoreBuilding() {
		return false;
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
	public void postProcessing(IBuilderContext context) {

	}

	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {

	}

	public void readFromNBT(NBTTagCompound nbt,	MappingRegistry registry) {

	}

}
