package buildcraft.builders;

import buildcraft.builders.item.ItemSchematicSingle;
import buildcraft.lib.item.ItemManager;

public class BCBuildersItems {

    public static ItemSchematicSingle schematicSingle;

    public static void preInit() {
        schematicSingle = ItemManager.register(new ItemSchematicSingle("item.schematic.single"));
    }
}
