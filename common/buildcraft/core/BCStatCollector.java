package buildcraft.core;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public enum BCStatCollector {
    INSTANCE;

    private final Map<Block, StatCrafting> blockMined = new IdentityHashMap<>();
    private final Map<Item, StatCrafting> itemCrafted = new IdentityHashMap<>();
    private final Map<Item, StatCrafting> itemUsed = new IdentityHashMap<>();

    public static void registerStats(Block block) {
        if (!block.getEnableStats()) return;

        Item item = Item.getItemFromBlock(block);

        if (item == null) return;

        String statName = toStatName(item);
        StatCrafting mineBlock = createCrafting("stat.mineBlock", statName, new ItemStack(block));

        StatList.MINE_BLOCK_STATS.add(mineBlock);
        INSTANCE.blockMined.put(block, mineBlock);

        registerStats(item);
    }

    public static void registerStats(Item item) {
        String statName = toStatName(item);
        StatCrafting used = createCrafting("stat.useItem", statName, new ItemStack(item));
        StatCrafting craft = createCrafting("stat.craftItem", statName, new ItemStack(item));

        if (!(item instanceof ItemBlock)) {
            StatList.USE_ITEM_STATS.add(used);
        }

        INSTANCE.itemUsed.put(item, used);
        INSTANCE.itemCrafted.put(item, craft);
    }

    public static StatCrafting createCrafting(String start, String statName, ItemStack stack) {
        Object[] translation = { stack.getTextComponent() };
        StatCrafting stat = new StatCrafting(start + ".", statName, new TextComponentTranslation(start, translation), stack.getItem());
        stat.registerStat();
        return stat;
    }

    private static String toStatName(Item item) {
        ResourceLocation resourcelocation = Item.REGISTRY.getNameForObject(item);
        return resourcelocation != null ? resourcelocation.toString().replace(':', '.') : null;
    }

    public void serverStarting() {
        StatBase[] objUse = ObfuscationReflectionHelper.getPrivateValue(StatList.class, null, "OBJECT_USE_STATS", "");
        /* We know that the registry has been frozen at this point so it is safe to fill up the arrays */
        for (Entry<Item, StatCrafting> crafted : itemUsed.entrySet()) {
                objUse[Item.getIdFromItem(crafted.getKey())] = crafted.getValue();
        }
        StatBase[] objCraft = ObfuscationReflectionHelper.getPrivateValue(StatList.class, null, "OBJECT_CRAFT_STATS", "");
        for (Entry<Item, StatCrafting> crafted : itemCrafted.entrySet()) {
            objCraft[Item.getIdFromItem(crafted.getKey())] = crafted.getValue();
        }
    }
}
