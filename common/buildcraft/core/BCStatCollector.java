package buildcraft.core;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatCrafting;
import net.minecraft.stats.StatList;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ResourceLocation;

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
        Object[] translation = { (new ItemStack(block)).getChatComponent() };

        StatCrafting mineBlock = new StatCrafting("stat.mineBlock.", statName, new ChatComponentTranslation("stat.mineBlock", translation), item);
        mineBlock.registerStat();

        StatList.objectMineStats.add(mineBlock);
        INSTANCE.blockMined.put(block, mineBlock);

        registerStats(item);
    }

    public static void registerStats(Item item) {
        String statName = toStatName(item);
        Object[] translation = { (new ItemStack(item)).getChatComponent() };

        StatCrafting used = new StatCrafting("stat.useItem.", statName, new ChatComponentTranslation("stat.useItem", translation), item);
        StatCrafting craft = new StatCrafting("stat.craftItem.", statName, new ChatComponentTranslation("stat.craftItem", translation), item);
        used.registerStat();
        craft.registerStat();

        if (!(item instanceof ItemBlock)) {
            StatList.itemStats.add(used);
        }

        INSTANCE.itemUsed.put(item, used);
        INSTANCE.itemCrafted.put(item, craft);
    }

    private static String toStatName(Item item) {
        ResourceLocation resourcelocation = Item.itemRegistry.getNameForObject(item);
        return resourcelocation != null ? resourcelocation.toString().replace(':', '.') : null;
    }

    public void serverStarting() {
        /* We know that the registry has been frozen at this point so it is safe to fill up the arrays */
        for (Entry<Item, StatCrafting> crafted : itemUsed.entrySet()) {
            StatList.objectUseStats[Item.getIdFromItem(crafted.getKey())] = crafted.getValue();
        }
        for (Entry<Item, StatCrafting> crafted : itemCrafted.entrySet()) {
            StatList.objectCraftStats[Item.getIdFromItem(crafted.getKey())] = crafted.getValue();
        }
    }
}
