package buildcraft.lib.recipe;

public interface IRecipeViewable {
    ChangingItemStack[] getRecipeInputs();

    ChangingItemStack getRecipeOutputs();

    public interface IViewableGrid extends IRecipeViewable {
        int getRecipeWidth();

        int getRecipeHeight();
    }

    /** Use this for integration table recipes or assembly table recipes. */
    public interface IRecipePowered extends IRecipeViewable {
        ChangingObject<Long> getMjCost();
    }
}
