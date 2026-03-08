package buildcraft.lib.gui.recipe;

import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeWidget;
import net.minecraft.item.crafting.ICraftingRecipe;
import net.minecraft.item.crafting.IRecipe;

import java.lang.reflect.Field;
import java.util.List;

public class RecipeBookPagePhantom extends RecipeBookPage {

    public final GuiRecipeBookPhantom gui;

    public RecipeBookPagePhantom(GuiRecipeBookPhantom gui) throws ReflectiveOperationException {
        super();
        this.gui = gui;

        for (Field fld : RecipeBookPage.class.getDeclaredFields()) {
            if (fld.getType() == List.class) {
                fld.setAccessible(true);
                List list = (List) fld.get(this);
                if (list == null || list.isEmpty()) {
                    continue;
                }
                Object first = list.get(0);
//                if (first.getClass() == GuiButtonRecipe.class)
                if (first.getClass() == RecipeWidget.class) {
                    for (int i = 0; i < list.size(); i++) {
                        list.set(i, new GuiButtonRecipePhantom());
                    }
                }
            }
        }
    }

    @Override
//    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, int p_194196_4_, int p_194196_5_, int p_194196_6_, int p_194196_7_)
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton, int p_194196_4_, int p_194196_5_, int p_194196_6_, int p_194196_7_) {
        if (super.mouseClicked(mouseX, mouseY, mouseButton, p_194196_4_, p_194196_5_, p_194196_6_, p_194196_7_)) {
//            IRecipe recipe = getLastClickedRecipe();
            IRecipe<?> recipe = getLastClickedRecipe();
            if (recipe != null && recipe instanceof ICraftingRecipe) {
                ICraftingRecipe craftingRecipe = (ICraftingRecipe) recipe;
//                gui.recipeSetter.accept(recipe);
                gui.recipeSetter.accept(craftingRecipe);
            }
            return true;
        }
        return false;
    }
}
