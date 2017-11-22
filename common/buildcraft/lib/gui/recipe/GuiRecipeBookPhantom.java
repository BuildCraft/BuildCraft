package buildcraft.lib.gui.recipe;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;

import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;

/** A {@link GuiRecipeBook} that can always craft things, as it puts the required items into phantom slots (Either
 * {@link SlotPhantom} or {@link ItemHandlerManager} with an argument of {@link EnumAccess#PHANTOM}. */
public class GuiRecipeBookPhantom extends GuiRecipeBook {

    private static final Field FIELD_GUI_BOOK;
    private static final Field FIELD_BUTTON_TOGGLE;

    public final Consumer<IRecipe> recipeSetter;
    private boolean wasFilteringCraftable;

    // Unfortunately we have to use reflection in order to replace the necessary fields :(
    static {
        try {
            Class<?> clazzGuiRecipeBook = GuiRecipeBook.class;
            Field fldReipePage = null;
            Field fldButtonToggle = null;

            for (Field fld : clazzGuiRecipeBook.getDeclaredFields()) {
                if (fld.getType() == RecipeBookPage.class) {
                    if (fldReipePage == null) {
                        fldReipePage = fld;
                    } else {
                        throw new IllegalStateException("Found multiple fields!");
                    }
                } else if (fld.getType() == GuiButtonToggle.class) {
                    if (fldButtonToggle == null) {
                        fldButtonToggle = fld;
                    } else {
                        throw new IllegalStateException("Found multiple fields!");
                    }
                }
            }
            if (fldReipePage == null || fldButtonToggle == null) {
                throw new Error("Couldn't find the required fields!");
            }
            fldReipePage.setAccessible(true);
            fldButtonToggle.setAccessible(true);
            FIELD_GUI_BOOK = fldReipePage;
            FIELD_BUTTON_TOGGLE = fldButtonToggle;
        } catch (Throwable roe) {
            throw new Error(roe);
        }
    }

    public GuiRecipeBookPhantom(Consumer<IRecipe> recipeSetter) throws ReflectiveOperationException {
        this.recipeSetter = recipeSetter;
        FIELD_GUI_BOOK.set(this, new RecipeBookPagePhantom(this));
        // Filtering craftable is really strange with phantom inventories
        RecipeBook recipeBook = Minecraft.getMinecraft().player.getRecipeBook();
        wasFilteringCraftable = recipeBook.isFilteringCraftable();
        recipeBook.setFilteringCraftable(false);
    }

    @Override
    public void removed() {
        super.removed();
        if (wasFilteringCraftable) {
            Minecraft.getMinecraft().player.getRecipeBook().setFilteringCraftable(true);
        }
    }

    @Override
    public void initVisuals(boolean someBoolean, InventoryCrafting invCrafting) {
        // Remove the craftable toggle button: we can always craft everything (as we can only create ghosts)
        super.initVisuals(someBoolean, invCrafting);
        try {
            GuiButtonToggle button = (GuiButtonToggle) FIELD_BUTTON_TOGGLE.get(this);
            button.x = -100000;
            button.y = -100000;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new Error("Couldn't access the toggle button!");
        }
    }
}
