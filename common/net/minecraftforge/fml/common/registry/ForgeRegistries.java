// STUB(R.Chen): Forge ForgeRegistries — compile shim.
package net.minecraftforge.fml.common.registry;

import net.minecraft.registry.Registries;
import net.minecraftforge.registries.IForgeRegistry;

public class ForgeRegistries {
    public static final IForgeRegistry<net.minecraft.block.Block> BLOCKS = new IForgeRegistry<net.minecraft.block.Block>() {
        public void register(net.minecraft.block.Block value) {}
        public void registerAll(net.minecraft.block.Block... values) {}
    };
    public static final IForgeRegistry<net.minecraft.item.Item> ITEMS = new IForgeRegistry<net.minecraft.item.Item>() {
        public void register(net.minecraft.item.Item value) {}
        public void registerAll(net.minecraft.item.Item... values) {}
    };

    // STUB: Recipe registry — no direct equivalent in Fabric 1.20.1; returns empty list
    @SuppressWarnings("unchecked")
    public static final Iterable<net.minecraft.recipe.CraftingRecipe> RECIPES = java.util.Collections.emptyList();
}
