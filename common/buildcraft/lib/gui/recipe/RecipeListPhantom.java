package buildcraft.lib.gui.recipe;

import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.item.crafting.IRecipe;

import java.lang.reflect.Field;
import java.util.Set;

//public class RecipeListPhantom extends RecipeList
public class RecipeListPhantom extends RecipeList {

    // public RecipeListPhantom(RecipeList from) throws ReflectiveOperationException
    public RecipeListPhantom(RecipeList from) throws ReflectiveOperationException {
//        getRecipes().addAll(from.getRecipes());
        super(from.getRecipes());
//        Class<?> clazzBitSet = BitSet.class;
        Class<?> clazzBitSet = Set.class;
        boolean first = true;
//        for (Field fld : RecipeList.class.getDeclaredFields())
        for (Field fld : RecipeList.class.getDeclaredFields()) {
            if (fld.getType() == clazzBitSet) {
                fld.setAccessible(true);
                Object object = fld.get(from);
                if (first) {
                    // TODO Calen which field?
////                    ((BitSet) object).set(0, getRecipes().size());
//                    ((Set) object).set(0, getRecipes().size());
                }
                fld.set(this, object);
                first = false;
            }
        }
    }

    @Override
    public boolean hasSingleResultItem() {
        // Only called by the draw function -- for some reason this will render a second
        // item beside the first if this returns true and getOrderedRecipes().size() > 1
        return false;
    }

    @Override
    public boolean isCraftable(IRecipe recipe) {
        return true;
    }

    @Override
//    public boolean containsCraftableRecipes()
    public boolean hasCraftable() {
        return !getRecipes().isEmpty();
    }
}
