package buildcraft.lib.item;

import net.minecraft.item.Item;
import net.minecraft.item.Rarity;

public class ItemPropertiesCreator {
    public static Item.Properties common64() {
        return new Item.Properties()
                .stacksTo(64)
                .rarity(Rarity.COMMON);
    }

    public static Item.Properties common1() {
        return new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.COMMON);
    }

    public static Item.Properties common16() {
        return new Item.Properties()
                .stacksTo(16)
                .rarity(Rarity.COMMON);
    }

    public static Item.Properties rare1() {
        return new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE);
    }

    public static Item.Properties epic64() {
        return new Item.Properties()
                .stacksTo(64)
                .rarity(Rarity.EPIC);
    }

    public static Item.Properties blockItem() {
        return new Item.Properties();
    }
}
