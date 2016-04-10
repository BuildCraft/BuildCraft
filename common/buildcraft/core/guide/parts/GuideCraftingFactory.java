package buildcraft.core.guide.parts;

import java.lang.reflect.Field;
import java.util.List;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import buildcraft.api.core.BCLog;
import buildcraft.core.guide.GuiGuide;

public class GuideCraftingFactory extends GuidePartFactory<GuideCrafting> {
    private static final Field SHAPED_ORE_RECIPE___WIDTH;
    private static final Field SHAPED_ORE_RECIPE___HEIGHT;

    private final ItemStack[][] input;
    private final ItemStack output;

    public GuideCraftingFactory(ItemStack[][] input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    static {
        try {
            SHAPED_ORE_RECIPE___WIDTH = ShapedOreRecipe.class.getDeclaredField("width");
            SHAPED_ORE_RECIPE___WIDTH.setAccessible(true);

            SHAPED_ORE_RECIPE___HEIGHT = ShapedOreRecipe.class.getDeclaredField("height");
            SHAPED_ORE_RECIPE___HEIGHT.setAccessible(true);
        } catch (Throwable t) {
            throw new RuntimeException("Could not find the width field!");
        }
    }

    @SuppressWarnings("unchecked")
    public static GuideCraftingFactory create(ItemStack stack) {
        for (IRecipe recipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList()) {
            if (ItemStack.areItemsEqual(stack, recipe.getRecipeOutput())) {
                if (recipe instanceof ShapedRecipes) {
                    ItemStack[] input = ((ShapedRecipes) recipe).recipeItems;
                    ItemStack[][] dimInput = new ItemStack[((ShapedRecipes) recipe).recipeWidth][((ShapedRecipes) recipe).recipeHeight];
                    for (int x = 0; x < dimInput.length; x++) {
                        for (int y = 0; y < dimInput[x].length; y++) {
                            dimInput[x][y] = ItemStack.copyItemStack(input[x + y * dimInput.length]);
                        }
                    }
                    return new GuideCraftingFactory(dimInput, stack);
                } else if (recipe instanceof ShapedOreRecipe) {
                    Object[] input = ((ShapedOreRecipe) recipe).getInput();
                    ItemStack[][] dimInput = getStackSizeArray(recipe);
                    for (int x = 0; x < dimInput.length; x++) {
                        for (int y = 0; y < dimInput[x].length; y++) {
                            dimInput[x][y] = oreConvert(input[x + y * dimInput.length]);
                        }
                    }
                    return new GuideCraftingFactory(dimInput, stack);
                } else {
                    BCLog.logger.info("Found a matching recipe, but of an unknown class (" + recipe.getClass() + ") for " + stack.getDisplayName());
                }
            }
        }
        return null;
    }

    private static ItemStack[][] getStackSizeArray(IRecipe recipe) {
        if (recipe instanceof ShapedRecipes) {
            return new ItemStack[((ShapedRecipes) recipe).recipeWidth][((ShapedRecipes) recipe).recipeHeight];
        } else if (recipe instanceof ShapedOreRecipe) {
            // YAAAAY REFLECTION :(
            int width = 3;
            int height = 3;
            try {
                width = SHAPED_ORE_RECIPE___WIDTH.getInt(recipe);
                height = SHAPED_ORE_RECIPE___HEIGHT.getInt(recipe);
            } catch (Throwable t) {
                BCLog.logger.error("Could not access the required shaped ore recipe fields!", t);
            }
            return new ItemStack[width][height];
        }
        return new ItemStack[3][3];
    }

    private static ItemStack oreConvert(Object object) {
        if (object == null) {
            return null;
        }
        if (object instanceof ItemStack) {
            return (ItemStack) object;
        }
        if (object instanceof String) {
            List<ItemStack> stacks = OreDictionary.getOres((String) object);
            // It will be sorted out below
            object = stacks;
        }
        if (object instanceof List<?>) {
            List<?> list = (List<?>) object;
            if (list.isEmpty()) {
                return null;
            }
            Object first = list.get(0);
            if (first == null) {
                return null;
            }
            if (first instanceof ItemStack) {
                // Technically a safe cast as the first one WAS an Item Stack and we never add to the list
                @SuppressWarnings("unchecked")
                List<ItemStack> stacks = (List<ItemStack>) list;
                if (stacks.size() == 0) {
                    return null;
                }
                ItemStack best = stacks.get(0);
                for (ItemStack stack : stacks) {
                    stack.getItem();
                    best.getItem();
                    // The lower the ID of an item, the closer it is to minecraft. Hmmm.
                    if (Item.getIdFromItem(stack.getItem()) < Item.getIdFromItem(best.getItem())) {
                        best = stack;
                    }
                }
                return best;
            }
            BCLog.logger.warn("Found a list with unknown contents! " + first.getClass());
        }
        BCLog.logger.warn("Found an ore with an unknown " + object.getClass());
        return null;
    }

    public static GuideCraftingFactory create(Item output) {
        return create(new ItemStack(output));
    }

    @Override
    public GuideCrafting createNew(GuiGuide gui) {
        return new GuideCrafting(gui, input, output);
    }

}
