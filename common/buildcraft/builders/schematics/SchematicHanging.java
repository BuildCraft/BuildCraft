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
import buildcraft.api.blueprints.CoordTransformation;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.core.Position;

public class SchematicHanging extends SchematicEntity {

	@Override
	public void writeToWorld(IBuilderContext context, CoordTransformation transform) {

        /*par1NBTTagCompound.setByte("Direction", (byte)this.hangingDirection);
        par1NBTTagCompound.setInteger("TileX", this.field_146063_b);
        par1NBTTagCompound.setInteger("TileY", this.field_146064_c);
        par1NBTTagCompound.setInteger("TileZ", this.field_146062_d);*/

		Position pos = new Position (cpt.getInteger("TileX"), cpt.getInteger("TileY"), cpt.getInteger("TileZ"));
		pos = transform.transform(pos);
		cpt.setInteger("TileX", (int) pos.x);
		cpt.setInteger("TileY", (int) pos.y);
		cpt.setInteger("TileZ", (int) pos.z);

		super.writeToWorld(context, transform);
	}

	@Override
	public void readFromWorld(IBuilderContext context, Entity entity, CoordTransformation transform) {
		super.readFromWorld(context, entity, transform);

		Position pos = new Position (cpt.getInteger("TileX"), cpt.getInteger("TileY"), cpt.getInteger("TileZ"));
		pos = transform.transform(pos);
		cpt.setInteger("TileX", (int) pos.x);
		cpt.setInteger("TileY", (int) pos.y);
		cpt.setInteger("TileZ", (int) pos.z);
	}
}
