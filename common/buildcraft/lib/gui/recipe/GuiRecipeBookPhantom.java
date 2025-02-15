package buildcraft.lib.gui.recipe;

import buildcraft.lib.gui.slot.SlotPhantom;
import buildcraft.lib.tile.item.ItemHandlerManager;
import buildcraft.lib.tile.item.ItemHandlerManager.EnumAccess;
import net.minecraft.client.gui.components.StateSwitchingButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;

import java.util.function.Consumer;

/** A {@link RecipeBookComponent} that can always craft things, as it puts the required items into phantom slots (Either
 * {@link SlotPhantom} or {@link ItemHandlerManager} with an argument of {@link EnumAccess#PHANTOM}. */
// TODO Calen: this is not used, because not found how to add recipe book in 1.18.2
//public class GuiRecipeBookPhantom extends GuiRecipeBook
public class GuiRecipeBookPhantom extends RecipeBookComponent {

//    private static final Field FIELD_GUI_BOOK;
//    private static final Field FIELD_BUTTON_TOGGLE;

    // public final Consumer<Recipe> recipeSetter;
    public final Consumer<CraftingRecipe> recipeSetter;
//    private boolean wasFilteringCraftable;

//    // Unfortunately we have to use reflection in order to replace the necessary fields :(
//    static {
//        try {
////            Class<?> clazzGuiRecipeBook = GuiRecipeBook.class;
//            Class<?> clazzGuiRecipeBook = RecipeBookComponent.class;
//            Field fldReipePage = null;
//            Field fldButtonToggle = null;
//
//            for (Field fld : clazzGuiRecipeBook.getDeclaredFields()) {
//                if (fld.getType() == RecipeBookPage.class) {
//                    if (fldReipePage == null)
//                    {
//                        fldReipePage = fld;
//                    }
//                    else
//                    {
//                        throw new IllegalStateException("Found multiple fields!");
//                    }
//                }
////                else if (fld.getType() == GuiButtonToggle.class)
//                else if (fld.getType() == StateSwitchingButton.class)
//                {
//                    if (fldButtonToggle == null) {
//                        fldButtonToggle = fld;
//                    } else {
//                        throw new IllegalStateException("Found multiple fields!");
//                    }
//                }
//            }
//            if (fldReipePage == null || fldButtonToggle == null) {
//                throw new Error("Couldn't find the required fields!");
//            }
//            fldReipePage.setAccessible(true);
//            fldButtonToggle.setAccessible(true);
//            FIELD_GUI_BOOK = fldReipePage;
//            FIELD_BUTTON_TOGGLE = fldButtonToggle;
//        } catch (Throwable roe) {
//            throw new Error(roe);
//        }
//    }

    //        public GuiRecipeBookPhantom(Consumer<Recipe> recipeSetter) throws ReflectiveOperationException
    public GuiRecipeBookPhantom(Consumer<CraftingRecipe> recipeSetter) throws ReflectiveOperationException
//    public GuiRecipeBookPhantom(Consumer<CraftingRecipe> recipeSetter, int width, int height, Minecraft minecraft, boolean widthTooNarrow, RecipeBookMenu<?> container) throws ReflectiveOperationException
    {
        this.recipeSetter = recipeSetter;
//        FIELD_GUI_BOOK.set(this, new RecipeBookPagePhantom(this));
        this.recipeBookPage = new RecipeBookPagePhantom(this);
//        init(width, height, minecraft, widthTooNarrow, container); // Calen: should before recipeBook.isFiltering(this.menu)

//        // Filtering craftable is really strange with phantom inventories
////        RecipeBook recipeBook = Minecraft.getMinecraft().player.getRecipeBook();
//        RecipeBook recipeBook = Minecraft.getInstance().player.getRecipeBook();
////        wasFilteringCraftable = recipeBook.isFilteringCraftable();
//        wasFilteringCraftable = recipeBook.isFiltering(this.menu);
////        recipeBook.setFilteringCraftable(false);
//        recipeBook.setFiltering(this.menu.getRecipeBookType(), false);
    }

    // Calen 1.20.1: no this method
    // 1.18.2: CraftingScreen#removed() overrides AbstractContainerScreen#removed() and called this.recipeBookComponent.removed()
    // 1.20.1: no override
//    @Override
//    public void removed() {
//        super.removed();
////        if (wasFilteringCraftable) {
//////            Minecraft.getMinecraft().player.getRecipeBook().setFilteringCraftable(true);
////            Minecraft.getInstance().player.getRecipeBook().setFiltering(this.menu.getRecipeBookType(), true);
////        }
//    }

    public void initVisuals(boolean someBoolean, CraftingContainer invCrafting) {
        this.initVisuals();
        super.xOffset = super.widthTooNarrow ? 0 : 86;
        invCrafting.fillStackedContents(super.stackedContents);
    }

    @Override
//    public void initVisuals(boolean someBoolean, InventoryCrafting invCrafting)
    public void initVisuals() {
        // Remove the craftable toggle button: we can always craft everything (as we can only create ghosts)
//        super.initVisuals(someBoolean, invCrafting);
        super.initVisuals();
//        try
//        {
//            GuiButtonToggle button = (GuiButtonToggle) FIELD_BUTTON_TOGGLE.get(this);
//            StateSwitchingButton button = (StateSwitchingButton) FIELD_BUTTON_TOGGLE.get(this);
        StateSwitchingButton button = this.filterButton;
        button.setX(-100000);
        button.setY(-100000);
//        }
//        catch (IllegalAccessException e)
//        {
//            e.printStackTrace();
//            throw new Error("Couldn't access the toggle button!");
//        }
    }
}
