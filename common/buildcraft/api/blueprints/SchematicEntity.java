/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import buildcraft.api.core.Position;

public class SchematicEntity {

	public Class <? extends Entity> entity;

	public NBTTagCompound cpt = new NBTTagCompound();

	public void writeToWorld(IBuilderContext context, CoordTransformation transform) {
		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);
		Position pos = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));
		pos = transform.translate(pos);
		cpt.setTag("Pos", this.newDoubleNBTList(new double[] {pos.x, pos.y, pos.z}));

		Entity e = EntityList.createEntityFromNBT(cpt, context.world());
		context.world().spawnEntityInWorld(e);
	}

	public void readFromWorld(IBuilderContext context, Entity entity, CoordTransformation transform) {
		entity.writeToNBTOptional(cpt);

		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);
		Position pos = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));
		pos = transform.translate(pos);

		cpt.setTag("Pos", this.newDoubleNBTList(new double[] {pos.x, pos.y, pos.z}));
	}

	public void rotateLeft(IBuilderContext context) {
		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);
		Position pos = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));
		pos = context.rotatePositionLeft(pos);
		cpt.setTag("Pos", this.newDoubleNBTList(new double[] {pos.x, pos.y, pos.z}));

		nbttaglist = cpt.getTagList("Rotation", 5);
		float yaw = nbttaglist.func_150308_e (0);
		yaw += 90;
		cpt.setTag("Rotation", this.newFloatNBTList(new float[] {yaw, nbttaglist.func_150308_e (1)}));
	}

	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		nbt.setInteger ("entityId", registry.getIdForEntity(entity));
		nbt.setTag("entity", cpt);
	}

	public void readFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
		cpt = nbt.getCompoundTag("entity");
	}

	protected NBTTagList newDoubleNBTList(double... par1ArrayOfDouble) {
		NBTTagList nbttaglist = new NBTTagList();
		double[] adouble = par1ArrayOfDouble;
		int i = par1ArrayOfDouble.length;

		for (int j = 0; j < i; ++j) {
			double d1 = adouble[j];
			nbttaglist.appendTag(new NBTTagDouble(d1));
		}

		return nbttaglist;
	}

	protected NBTTagList newFloatNBTList(float... par1ArrayOfFloat) {
		NBTTagList nbttaglist = new NBTTagList();
		float[] afloat = par1ArrayOfFloat;
		int i = par1ArrayOfFloat.length;

		for (int j = 0; j < i; ++j) {
			float f1 = afloat[j];
			nbttaglist.appendTag(new NBTTagFloat(f1));
		}

		return nbttaglist;
	}
}
