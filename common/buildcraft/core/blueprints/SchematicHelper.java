package buildcraft.core.blueprints;

import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.ISchematicHelper;
import buildcraft.core.lib.inventory.StackHelper;

public final class SchematicHelper implements ISchematicHelper {
	public static final SchematicHelper INSTANCE = new SchematicHelper();

	private SchematicHelper() {

	}

	@Override
	public boolean isEqualItem(ItemStack a, ItemStack b) {
		return StackHelper.isEqualItem(a, b);
	}
}
