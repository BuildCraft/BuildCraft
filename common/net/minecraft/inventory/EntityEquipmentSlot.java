// STUB(R.Chen): EntityEquipmentSlot → net.minecraft.entity.EquipmentSlot shim for 1.12.2 compat
package net.minecraft.inventory;

import net.minecraft.entity.EquipmentSlot;

public enum EntityEquipmentSlot {
    HEAD, CHEST, LEGS, FEET, MAINHAND, OFFHAND;

    public enum Type { ARMOR, HAND }

    public Type getSlotType() {
        if (this == MAINHAND || this == OFFHAND) return Type.HAND;
        return Type.ARMOR;
    }

    public EquipmentSlot toFabric() {
        switch (this) {
            case HEAD: return EquipmentSlot.HEAD;
            case CHEST: return EquipmentSlot.CHEST;
            case LEGS: return EquipmentSlot.LEGS;
            case FEET: return EquipmentSlot.FEET;
            case MAINHAND: return EquipmentSlot.MAINHAND;
            case OFFHAND: return EquipmentSlot.OFFHAND;
            default: throw new IllegalStateException();
        }
    }
}
