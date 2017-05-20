package buildcraft.lib.recipe;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.recipes.IIntegrationRecipeProvider;
import buildcraft.api.recipes.IIntegrationRecipeRegistry;
import buildcraft.api.recipes.IntegrationRecipe;

import buildcraft.lib.misc.StackUtil;

public enum IntegrationRecipeRegistry implements IIntegrationRecipeRegistry {
    INSTANCE;
    private final Map<ResourceLocation, IntegrationRecipe> recipes = new HashMap<>();
    private final List<IIntegrationRecipeProvider> providers = new ArrayList<>();

    @Override
    public IntegrationRecipe getRecipeFor(@Nonnull ItemStack target, @Nonnull NonNullList<ItemStack> toIntegrate) {
        for (IntegrationRecipe recipe : recipes.values()) {
            if (matches(recipe, target, toIntegrate)) {
                return recipe;
            }
        }
        for (IIntegrationRecipeProvider provider : providers) {
            IntegrationRecipe possible = provider.getRecipeFor(target, toIntegrate);
            if (possible != null) {
                return possible;
            }
        }
        return null;
    }

    public static boolean matches(IntegrationRecipe recipe, @Nonnull ItemStack target, NonNullList<ItemStack> toIntegrate) {
        if (!StackUtil.contains(recipe.target, target)) {
            return false;
        }
        NonNullList<ItemStack> toIntegrateCopy = toIntegrate.stream().filter(stack -> !stack.isEmpty()).collect(StackUtil.nonNullListCollector());
        boolean stackMatches = recipe.toIntegrate.stream().allMatch((definition) -> {
            boolean matches = false;
            Iterator<ItemStack> iterator = toIntegrateCopy.iterator();
            while (iterator.hasNext()) {
                ItemStack stack = iterator.next();
                if (StackUtil.contains(definition, stack)) {
                    matches = true;
                    iterator.remove();
                    break;
                }
            }
            return matches;
        });
        return stackMatches && toIntegrateCopy.isEmpty();
    }

    @Override
    public void addRecipe(IntegrationRecipe recipe) {
        if (recipes.containsKey(recipe.name)) {
            throw new IllegalStateException("Trying to override integration recipe with name " + recipe.name + ".\n" +
                    "If you want replace recipe remove old one first.");
        }
        recipes.put(recipe.name, recipe);
    }

    @Override
    public void addRecipeProvider(IIntegrationRecipeProvider provider) {
        providers.add(provider);
    }

    @Override
    public Iterable<IntegrationRecipe> getAllRecipes() {
        return recipes.values();
    }

    @Override
    public Iterable<IIntegrationRecipeProvider> getAllRecipeProviders() {
        return providers;
    }

    @Override
    public Optional<IntegrationRecipe> getRecipe(@Nonnull ResourceLocation name, @Nullable NBTTagCompound recipeTag) {
        IntegrationRecipe recipe = recipes.get(name);
        if (recipe != null) return Optional.of(recipe);
        return providers.stream().map(provider -> provider.getRecipe(name, recipeTag).orElse(null))
                .filter(Objects::nonNull).findFirst();
    }
}
