package buildcraft.core.builders;

import java.util.List;

import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.IBuilderContext;

public class BuilderItemMetaPair {
	public Item item;
	public int meta;
	public int position = 0;

	public BuilderItemMetaPair(ItemStack stack) {
		if (stack != null) {
			this.item = stack.getItem();
			this.meta = stack.getItemDamage();
		} else {
			this.item = Item.getItemFromBlock(Blocks.air);
			this.meta = 0;
		}
	}

	public BuilderItemMetaPair(IBuilderContext context, BuildingSlotBlock block) {
		this(findStack(context, block));
	}

	private static ItemStack findStack(IBuilderContext context, BuildingSlotBlock block) {
		List<ItemStack> s = block.getRequirements(context);
		return s.size() > 0 ? s.get(0) : null;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof BuilderItemMetaPair) {
			BuilderItemMetaPair imp = (BuilderItemMetaPair) o;
			return imp.item == item && imp.meta == meta;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Item.getIdFromItem(item) * 17 + meta;
	}
}
