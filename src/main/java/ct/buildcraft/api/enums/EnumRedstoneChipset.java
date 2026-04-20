package ct.buildcraft.api.enums;

import java.util.Locale;

import ct.buildcraft.silicon.BCSiliconItems;
import ct.buildcraft.silicon.item.ItemRedstoneChipset;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum EnumRedstoneChipset implements StringRepresentable {
    RED,
    IRON,
    GOLD,
    QUARTZ,
    DIAMOND;

    private final String name = name().toLowerCase(Locale.ROOT);
    
    public ItemStack getStack(int stackSize) {
        Item chipset = BCSiliconItems.REDSTONE_CHIPSET_ITEMS.get(this);//BCItems.Silicon.REDSTONE_CHIPSET; TODO
        if (chipset == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(chipset, stackSize);
    }

    public ItemStack getStack() {
        return getStack(1);
    }

    public static EnumRedstoneChipset fromStack(ItemStack stack) {
        if (stack == null||stack.isEmpty()) {
            return RED;
        }
        if(stack.getItem() instanceof ItemRedstoneChipset chip)
        	return chip.getType();
        return RED;
    }

    public static EnumRedstoneChipset fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= values().length) {
            return RED;
        }
        return values()[ordinal];
    }

	@Override
	public String getSerializedName() {
		return name;
	}
}
