package buildcraft.lib.client.guide.parts;

import net.minecraft.item.crafting.IRecipe;

// TODO: Move into API
public interface IRecipeViewable extends IRecipe {
    /** @return An array of all the possible input stacks. Note that both of the array dimensions should be <=3 */
    ChangingItemStack[][] getRecipeInputs();

    ChangingItemStack getRecipeOutputs();
}
