package buildcraft.lib.recipe;

public interface IRecipeViewable {
    ChangingItemStack[] getRecipeInputs();

    ChangingItemStack getRecipeOutputs();

    public interface IViewableGrid extends IRecipeViewable {
        int getRecipeWidth();

        int getRecipeHeight();
    }
}
