package buildcraft.lib.client.guide.parts.recipe;

import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.parts.GuidePartFactory;

/** Defines a stack recipe lookup - implementations should register with {@link RecipeLookupHelper} to be used by the
 * guide for usages and recipes. */
public interface IStackRecipes {
    List<GuidePartFactory> getUsages(@Nonnull ItemStack stack);

    List<GuidePartFactory> getRecipes(@Nonnull ItemStack stack);
}
