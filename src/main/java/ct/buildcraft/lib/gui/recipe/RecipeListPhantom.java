package ct.buildcraft.lib.gui.recipe;

import javax.annotation.Nullable;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.client.gui.screens.recipebook.RecipeCollection;
import net.minecraft.stats.RecipeBook;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.crafting.Recipe;

public class RecipeListPhantom extends RecipeCollection {

    public RecipeListPhantom(RecipeCollection from)/* throws ReflectiveOperationException */{
    	super(from.getRecipes());
/*        Class<?> clazzBitSet = Set.class;
        boolean first = true;
        for (Field fld : RecipeCollection.class.getDeclaredFields()) {
            if (fld.getType() == clazzBitSet) {
                fld.setAccessible(true);
                Object object = fld.get(from);
                if (first) {
                    ((Set<Recipe<?>>) object).addAll(getRecipes());
                }
                fld.set(this, object);
                first = false;
            }
        }*/
        RecipeBook book = new RecipeBook();
        this.getRecipes().forEach(book::add);
        this.updateKnownRecipes(book);
        this.canCraft(new StackedContents() {
        	   public boolean canCraft(Recipe<?> p_36476_, @Nullable IntList p_36477_) {
        		      return true;
        		   }
        }, 65536, 65536, book);
    }

    @Override
    public boolean hasSingleResultItem() {
        // Only called by the draw function -- for some reason this will render a second
        // item beside the first if this returns true and getOrderedRecipes().size() > 1
        return false;
    }

    @Override
    public boolean isCraftable(Recipe<?> recipe) {
        return true;
    }

    @Override
    public boolean hasCraftable() {
        return !getRecipes().isEmpty();
    }
}
