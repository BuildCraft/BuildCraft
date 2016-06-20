package buildcraft.lib.client.guide.parts.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

public interface IStackRecipes {
    List<GuidePartFactory<?>> getUsages(ItemStack stack);

    List<GuidePartFactory<?>> getRecipes(ItemStack stack);
}
