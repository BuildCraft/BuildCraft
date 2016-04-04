package a.buildcraft.lib;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent;
import net.minecraftforge.fml.common.event.FMLMissingMappingsEvent.MissingMapping;
import net.minecraftforge.fml.common.registry.GameRegistry.Type;

public enum MigrationManager {
    INSTANCE;

    private final Map<String, Item> itemMigrations = new HashMap<>();
    private final Map<String, Block> blockMigrations = new HashMap<>();

    public void addItemMigration(Item to, String... oldNames) {
        // If we mistakenly try to migrate null then it must have been disabled.
        if (to == null || Item.itemRegistry.getNameForObject(to) == null) return;
        for (String old : oldNames) {
            String oldLowerCase = old.toLowerCase(Locale.ROOT);
            if (itemMigrations.containsKey(oldLowerCase)) throw new IllegalArgumentException("Already registered item migration \"" + oldLowerCase + "\"!");
            itemMigrations.put(oldLowerCase, to);
        }
    }

    public void addBlockMigration(Block to, String... oldNames) {
        // If we mistakenly try to migrate null then it must have been disabled.
        if (to == null || Block.blockRegistry.getNameForObject(to) == null) return;
        for (String old : oldNames) {
            String oldLowerCase = old.toLowerCase(Locale.ROOT);
            if (blockMigrations.containsKey(oldLowerCase)) throw new IllegalArgumentException("Already registered block migration \"" + oldLowerCase + "\"!");
            blockMigrations.put(oldLowerCase, to);
        }
    }

    public void missingMappingEvent(FMLMissingMappingsEvent missing) {
        for (MissingMapping mapping : missing.getAll()) {
            ResourceLocation loc = mapping.resourceLocation;
            String domain = loc.getResourceDomain();
            String path = loc.getResourcePath().toLowerCase(Locale.ROOT);
            // TECHNICALLY this can pick up non-bc mods, but generally only addons
            if (!domain.startsWith("buildcraft")) continue;
            if (mapping.type == Type.ITEM) {
                if (itemMigrations.containsKey(path)) {
                    Item to = itemMigrations.get(path);
                    mapping.remap(to);
                }
            } else if (mapping.type == Type.BLOCK) {
                if (blockMigrations.containsKey(path)) {
                    Block to = blockMigrations.get(path);
                    mapping.remap(to);
                }
            }
        }
    }
}
