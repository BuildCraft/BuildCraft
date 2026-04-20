package ct.buildcraft.lib.gui.recipe;

import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;

public class GuiButtonRecipePhantom extends RecipeButton {
    @Override
    public void init(RecipeCollection list, RecipeBookPage page) {
//        try {
            list = new RecipeListPhantom(list);
//        } catch (ReflectiveOperationException e) {
 //           throw new IllegalStateException(e);
//        }
        super.init(list, page);
    }
}
