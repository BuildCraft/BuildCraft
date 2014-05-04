/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.api.blueprints;

import java.util.ArrayList;
import java.util.LinkedList;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;

import buildcraft.api.core.Position;

public class SchematicEntity extends Schematic {

	public Class<? extends Entity> entity;

	public NBTTagCompound cpt = new NBTTagCompound();

	/**
	 * This field contains requirements for a given block when stored in the
	 * blueprint. Modders can either rely on this list or compute their own int
	 * Schematic.
	 */
	public ItemStack[] storedRequirements = new ItemStack[0];

	@Override
	public void writeRequirementsToBuilder(IBuilderContext context, LinkedList<ItemStack> requirements) {
		for (ItemStack s : storedRequirements) {
			requirements.add(s);
		}
	}

	public void writeToWorld(IBuilderContext context) {
		Entity e = EntityList.createEntityFromNBT(cpt, context.world());
		context.world().spawnEntityInWorld(e);
	}

	public void readFromWorld(IBuilderContext context, Entity entity) {
		entity.writeToNBTOptional(cpt);
	}

	@Override
	public void transformToBlueprint(MappingRegistry registry,
			Translation transform) {
		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);
		Position pos = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));
		pos = transform.translate(pos);

		cpt.setTag("Pos",
				this.newDoubleNBTList(pos.x, pos.y, pos.z));

		inventorySlotsToBlueprint(registry, cpt);
	}

	@Override
	public void transformToWorld(MappingRegistry registry, Translation transform) {
		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);
		Position pos = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));
		pos = transform.translate(pos);

		cpt.setTag("Pos",
				this.newDoubleNBTList(pos.x, pos.y, pos.z));

		inventorySlotsToWorld(registry, cpt);
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);
		Position pos = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));
		pos = context.rotatePositionLeft(pos);
		cpt.setTag("Pos",
				this.newDoubleNBTList(pos.x, pos.y, pos.z));

		nbttaglist = cpt.getTagList("Rotation", 5);
		float yaw = nbttaglist.func_150308_e(0);
		yaw += 90;
		cpt.setTag(
				"Rotation",
				this.newFloatNBTList(yaw,
						nbttaglist.func_150308_e(1)));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt, MappingRegistry registry) {
		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);

		nbt.setInteger("entityId", registry.getIdForEntity(entity));
		nbt.setTag("entity", cpt);

		NBTTagList rq = new NBTTagList();

		for (ItemStack stack : storedRequirements) {
			NBTTagCompound sub = new NBTTagCompound();
			stack.writeToNBT(stack.writeToNBT(sub));
			sub.setInteger("id", registry.getIdForItem(stack.getItem()));
			rq.appendTag(sub);
		}

		nbt.setTag("rq", rq);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt, MappingRegistry registry) {
		cpt = nbt.getCompoundTag("entity");

		NBTTagList rq = nbt.getTagList("rq",
				Constants.NBT.TAG_COMPOUND);

		ArrayList<ItemStack> rqs = new ArrayList<ItemStack>();

		for (int i = 0; i < rq.tagCount(); ++i) {
			try {
				NBTTagCompound sub = rq.getCompoundTagAt(i);

				if (sub.getInteger("id") >= 0) {
					// Maps the id in the blueprint to the id in the world
					sub.setInteger("id", Item.itemRegistry
							.getIDForObject(registry.getItemForId(sub
									.getInteger("id"))));

					rqs.add(ItemStack.loadItemStackFromNBT(sub));
				} else {
					// TODO: requirement can't be retreived, this blueprint is
					// only useable in creative
				}
			} catch (Throwable t) {
				t.printStackTrace();
				// TODO: requirement can't be retreived, this blueprint is
				// only useable in creative
			}
		}

		storedRequirements = rqs.toArray(new ItemStack[rqs.size()]);
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

	public boolean isAlreadyBuilt(IBuilderContext context) {
		NBTTagList nbttaglist = cpt.getTagList("Pos", 6);
		Position newPosition = new Position(nbttaglist.func_150309_d(0),
				nbttaglist.func_150309_d(1), nbttaglist.func_150309_d(2));

		for (Object o : context.world().loadedEntityList) {
			Entity e = (Entity) o;

			Position existingPositon = new Position(e.posX, e.posY, e.posZ);

			if (existingPositon.isClose(newPosition, 0.1F)) {
				return true;
			}
		}

		return false;
	}

}
