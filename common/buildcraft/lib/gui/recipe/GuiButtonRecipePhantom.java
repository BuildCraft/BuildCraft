package buildcraft.lib.gui.recipe;

import net.minecraft.client.gui.recipebook.GuiButtonRecipe;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.stats.RecipeBook;

public class GuiButtonRecipePhantom extends GuiButtonRecipe {
    @Override
    public void init(RecipeList list, RecipeBookPage page, RecipeBook book) {
        try {
            list = new RecipeListPhantom(list);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        super.init(list, page, book);
    }
}
