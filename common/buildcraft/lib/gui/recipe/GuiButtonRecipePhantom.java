package buildcraft.lib.gui.recipe;

import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;

//public class GuiButtonRecipePhantom extends GuiButtonRecipe
public class GuiButtonRecipePhantom extends RecipeButton {
    @Override
//    public void init(RecipeList list, RecipeBookPage page, RecipeBook book)
    public void init(RecipeCollection list, RecipeBookPage page) {
        try {
            list = new RecipeListPhantom(list);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
//        super.init(list, page, book);
        super.init(list, page);
    }
}
