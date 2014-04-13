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
import net.minecraft.entity.EntityHanging;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.MappingRegistry;
import buildcraft.api.blueprints.SchematicEntity;
import buildcraft.api.blueprints.Translation;
import buildcraft.api.core.Position;

public class SchematicHanging extends SchematicEntity {

	private Item baseItem;

	public SchematicHanging (Item baseItem) {
		this.baseItem = baseItem;
	}

	@Override
	public void transformToBlueprint(MappingRegistry registry, Translation transform) {
		super.transformToBlueprint(registry, transform);

		Position pos = new Position (cpt.getInteger("TileX"), cpt.getInteger("TileY"), cpt.getInteger("TileZ"));
		pos = transform.translate(pos);
		cpt.setInteger("TileX", (int) pos.x);
		cpt.setInteger("TileY", (int) pos.y);
		cpt.setInteger("TileZ", (int) pos.z);
	}

	@Override
	public void transformToWorld(MappingRegistry registry, Translation transform) {
		super.transformToWorld(registry, transform);

		Position pos = new Position (cpt.getInteger("TileX"), cpt.getInteger("TileY"), cpt.getInteger("TileZ"));
		pos = transform.translate(pos);
		cpt.setInteger("TileX", (int) pos.x);
		cpt.setInteger("TileY", (int) pos.y);
		cpt.setInteger("TileZ", (int) pos.z);
	}

	@Override
	public void rotateLeft(IBuilderContext context) {
		super.rotateLeft(context);

		Position pos = new Position (cpt.getInteger("TileX"), cpt.getInteger("TileY"), cpt.getInteger("TileZ"));
		pos = context.rotatePositionLeft(pos);
		cpt.setInteger("TileX", (int) pos.x);
		cpt.setInteger("TileY", (int) pos.y);
		cpt.setInteger("TileZ", (int) pos.z);

		int direction = cpt.getByte("Direction");
		direction = direction < 3 ? direction + 1 : 0;
		cpt.setInteger("Direction", direction);
	}

	@Override
	public void writeToWorld(IBuilderContext context) {
		if (baseItem == Items.item_frame) {
			if (cpt.hasKey("Item")) {
				NBTTagCompound tag = cpt.getCompoundTag("Item");
				tag.setInteger("id", Item.itemRegistry.getIDForObject(context
						.getMappingRegistry()
						.getItemForId(tag.getInteger("id"))));
				cpt.setTag("Item", tag);
			}
		}

		super.writeToWorld(context);
	}

	@Override
	public void readFromWorld(IBuilderContext context, Entity entity) {
		super.readFromWorld(context, entity);

		if (baseItem == Items.item_frame) {
			NBTTagCompound tag = cpt.getCompoundTag("Item");
			ItemStack stack = ItemStack.loadItemStackFromNBT(tag);

			if (stack != null) {
				storedRequirements = new ItemStack [2];
				storedRequirements [0] = new ItemStack(baseItem);
				storedRequirements [1] = stack;

				tag.setInteger("id", context.getMappingRegistry().getIdForItem(stack.getItem()));
				cpt.setTag("Item", tag);
			} else {
				storedRequirements = new ItemStack [1];
				storedRequirements [0] = new ItemStack(baseItem);
			}
		} else {
			storedRequirements = new ItemStack [1];
			storedRequirements [0] = new ItemStack(baseItem);
		}
	}

	@Override
	public boolean isAlreadyBuilt(IBuilderContext context) {
		Position newPosition = new Position (cpt.getInteger("TileX"), cpt.getInteger("TileY"), cpt.getInteger("TileZ"));

		int dir = cpt.getInteger("Direction");

		for (Object o : context.world().loadedEntityList) {
			Entity e = (Entity) o;

			if (e instanceof EntityHanging) {
				EntityHanging h = (EntityHanging) e;
				Position existingPositon = new Position(h.field_146063_b, h.field_146064_c, h.field_146062_d);

				if (existingPositon.isClose(newPosition, 0.1F) && dir == ((EntityHanging) e).hangingDirection) {
					return true;
				}
			}
		}

		return false;
	}
}
