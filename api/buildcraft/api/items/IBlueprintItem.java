package buildcraft.api.items;

import net.minecraft.item.ItemStack;

public interface IBlueprintItem extends INamedItem {
	public enum Type {
		TEMPLATE, BLUEPRINT;
	}

	Type getType(ItemStack stack);
}
