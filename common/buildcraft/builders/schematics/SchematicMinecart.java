/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.builders.schematics;

import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import buildcraft.api.blueprints.CoordTransformation;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.Position;

public class SchematicMinecart extends SchematicEntity {

	private Item baseItem;

	public SchematicMinecart (Item baseItem) {
		this.baseItem = baseItem;
	}

	@Override
	public void writeToWorld(IBuilderContext context, CoordTransformation transform) {
		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);
		Position pos = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));
		pos.x += 0.5;
		pos.z += 0.5;
		cpt.setTag("Pos", this.newDoubleNBTList(new double[] {pos.x, pos.y, pos.z}));

		super.writeToWorld(context, transform);
	}

	@Override
	public void readFromWorld(IBuilderContext context, Entity entity, CoordTransformation transform) {
		super.readFromWorld(context, entity, transform);

		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);
		Position pos = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));
		pos.x -= 0.5;
		pos.z -= 0.5;
		cpt.setTag("Pos", this.newDoubleNBTList(new double[] {pos.x, pos.y, pos.z}));

		storedRequirements = new ItemStack [1];
		storedRequirements [0] = new ItemStack(baseItem);
	}
}
