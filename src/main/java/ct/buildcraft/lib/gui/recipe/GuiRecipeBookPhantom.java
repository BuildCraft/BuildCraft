package ct.buildcraft.lib.gui.recipe;

import java.lang.reflect.Field;
import java.util.function.Consumer;

import ct.buildcraft.lib.gui.slot.SlotPhantom;
import ct.buildcraft.lib.tile.item.ItemHandlerManager;
import ct.buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.inventory.RecipeBookType;
import net.minecraft.world.item.crafting.Recipe;

/** A {@link RecipeBookComponent} that can always craft things, as it puts the required items into phantom slots (Either
 * {@link SlotPhantom} or {@link ItemHandlerManager} with an argument of {@link EnumAccess#PHANTOM}. */
public class GuiRecipeBookPhantom  extends RecipeBookComponent {

    private static final Field FIELD_GUI_BOOK;
    private static final Field FIELD_BUTTON_TOGGLE;

    public final Consumer<Recipe<?>> recipeSetter;
    private boolean wasFilteringCraftable;

    // Unfortunately we have to use reflection in order to replace the necessary fields :(
    static {
        try {
            Class<?> clazzGuiRecipeBook = RecipeBookComponent.class;
            Field fldReipePage = null;
            Field fldButtonToggle = null;

            for (Field fld : clazzGuiRecipeBook.getDeclaredFields()) {
                if (fld.getType() == RecipeBookPage.class) {
                    if (fldReipePage == null) {
                        fldReipePage = fld;
                    } else {
                        throw new IllegalStateException("Found multiple fields!");
                    }
                } else if (fld.getType() == StateSwitchingButton.class) {
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

    public GuiRecipeBookPhantom(Consumer<Recipe<?>> recipeSetter) throws ReflectiveOperationException {
        this.recipeSetter = recipeSetter;
        FIELD_GUI_BOOK.set(this, new RecipeBookPagePhantom(this));
        // Filtering craftable is really strange with phantom inventories
        RecipeBook recipeBook = Minecraft.getInstance().player.getRecipeBook();
        wasFilteringCraftable = recipeBook.isFiltering(RecipeBookType.CRAFTING);//TODO
        recipeBook.setFiltering(RecipeBookType.CRAFTING, false);
    }

    @Override
    public void removed() {
        super.removed();
        if (wasFilteringCraftable) {
            Minecraft.getInstance().player.getRecipeBook().setFiltering(RecipeBookType.CRAFTING, true);
        }
    }
    
    

    @Override
    public void initVisuals() {
        // Remove the craftable toggle button: we can always craft everything (as we can only create ghosts)
        super.initVisuals();
        try {
        	StateSwitchingButton button = (StateSwitchingButton) FIELD_BUTTON_TOGGLE.get(this);
            button.x = -100000;
            button.y = -100000;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new Error("Couldn't access the toggle button!");
        }
    }
}
