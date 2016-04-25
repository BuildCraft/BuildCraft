package buildcraft.lib.list;

import net.minecraft.item.ItemFood;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.api.lists.ListRegistry;

public class VanillaListHandlers {
    public static void fmlInit() {
        ListRegistry.registerHandler(new ListMatchHandlerClass());
        ListRegistry.registerHandler(new ListMatchHandlerFluid());
        ListRegistry.registerHandler(new ListMatchHandlerTools());
        ListRegistry.registerHandler(new ListMatchHandlerArmor());
        ListRegistry.itemClassAsType.add(ItemFood.class);
    }

    public static void fmlPostInit() {
        for (String s : OreDictionary.getOreNames()) {
            ListOreDictionaryCache.INSTANCE.registerName(s);
        }
        ListRegistry.registerHandler(new ListMatchHandlerOreDictionary());
    }
}
