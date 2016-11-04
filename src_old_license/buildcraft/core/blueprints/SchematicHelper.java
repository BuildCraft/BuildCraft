package buildcraft.core.blueprints;

import net.minecraft.item.ItemStack;

import buildcraft.api.blueprints.ISchematicHelper;

import buildcraft.lib.misc.StackUtil;

public final class SchematicHelper implements ISchematicHelper {
    public static final SchematicHelper INSTANCE = new SchematicHelper();

    private SchematicHelper() {

    }

    @Override
    public boolean isEqualItem(ItemStack a, ItemStack b) {
        return StackUtil.isEqualItem(a, b);
    }
}
