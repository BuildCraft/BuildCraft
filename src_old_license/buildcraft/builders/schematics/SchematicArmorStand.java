package buildcraft.builders.schematics;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicEntity;

public class SchematicArmorStand extends SchematicEntity {
    @Override
    public void readFromWorld(IBuilderContext context, Entity entity) {
        super.readFromWorld(context, entity);

        List<ItemStack> requirements = new ArrayList<>();
        EntityArmorStand stand = (EntityArmorStand) entity;

        requirements.add(new ItemStack(Items.ARMOR_STAND));
        for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            ItemStack stack = stand.getItemStackFromSlot(slot);
            if (stack != null) {
                requirements.add(stack);
            }
        }

        storedRequirements = requirements.toArray(new ItemStack[requirements.size()]);
    }

}
