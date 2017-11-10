/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.builders;

import java.util.Collections;
import java.util.LinkedList;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingNotFoundException;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.blueprints.SchematicFactory;
import buildcraft.api.core.Position;

public class BuildingSlotEntity extends BuildingSlot {

	public SchematicEntity schematic;

	/**
	 * This value is set by builders to identify in which order entities are
	 * being built. It can be used later for unique identification within a
	 * blueprint.
	 */
	public int sequenceNumber;

	@Override
	public boolean writeToWorld(IBuilderContext context) {
		schematic.writeToWorld(context);
		return true;
	}

	@Override
	public Position getDestination() {
		NBTTagList nbttaglist = schematic.entityNBT.getTagList("Pos", 6);
		Position pos = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));

		return pos;
	}

	@Override
	public LinkedList<ItemStack> getRequirements(IBuilderContext context) {
		LinkedList<ItemStack> results = new LinkedList<ItemStack>();

		Collections.addAll(results, schematic.storedRequirements);

		return results;
	}

	@Override
	public SchematicEntity getSchematic() {
		return schematic;
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context) {
		return schematic.isAlreadyBuilt(context);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		NBTTagCompound schematicNBT = new NBTTagCompound();
		SchematicFactory.getFactory(schematic.getClass())
				.saveSchematicToWorldNBT(schematicNBT, schematic, registry);
		nbt.setTag("schematic", schematicNBT);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt, MappingRegistry registry) throws MappingNotFoundException {
		schematic = (SchematicEntity) SchematicFactory
				.createSchematicFromWorldNBT(nbt.getCompoundTag("schematic"), registry);
	}

	@Override
	public int getEnergyRequirement() {
		return schematic.getEnergyRequirement(stackConsumed);
	}

	@Override
	public int buildTime() {
		return schematic.buildTime();
	}
}
