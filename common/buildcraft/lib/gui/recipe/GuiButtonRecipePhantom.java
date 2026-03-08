package buildcraft.lib.gui.recipe;

import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.gui.recipebook.RecipeWidget;

//public class GuiButtonRecipePhantom extends GuiButtonRecipe
public class GuiButtonRecipePhantom extends RecipeWidget {
    @Override
//    public void init(RecipeList list, RecipeBookPage page, RecipeBook book)
    public void init(RecipeList list, RecipeBookPage page) {
        try {
            list = new RecipeListPhantom(list);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
//        super.init(list, page, book);
        super.init(list, page);
    }
}
