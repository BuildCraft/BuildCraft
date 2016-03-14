package buildcraft.builders.schematics;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SchematicArmorStand extends SchematicEntity {
	@Override
	public void readFromWorld(IBuilderContext context, Entity entity) {
		super.readFromWorld(context, entity);

		List<ItemStack> requirements = new ArrayList<>();
		EntityArmorStand stand = (EntityArmorStand) entity;

		requirements.add(new ItemStack(Items.armor_stand));
		for (int i = 0; i <= 4; i++) {
			ItemStack stack = stand.getEquipmentInSlot(i);
			if (stack != null) {
				requirements.add(stack);
			}
		}

		storedRequirements = requirements.toArray(new ItemStack[requirements.size()]);
	}

}
