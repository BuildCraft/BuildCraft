package buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.ShapedOreRecipe;

import buildcraft.api.core.BCLog;

import buildcraft.lib.misc.StackUtil;

import gnu.trove.map.hash.TCharObjectHashMap;

public class RecipeBuilderShaped {
    private @Nonnull ItemStack result;
    private final List<String> shape = new ArrayList<>();
    private final TCharObjectHashMap<Object> objects = new TCharObjectHashMap<>();

    public RecipeBuilderShaped() {
        this(StackUtil.EMPTY);
    }

    public RecipeBuilderShaped(@Nonnull ItemStack result) {
        this.result = result;
    }

    public RecipeBuilderShaped add(String row) {
        if (shape.size() > 0 && shape.get(0).length() != row.length()) {
            throw new IllegalArgumentException("Badly sized row!");
        }
        shape.add(row);
        return this;
    }

    // map(char, object)
    // Straightforward mapping of a char to a non-null object

    public RecipeBuilderShaped map(char c, Item item) {
        if (item == null) {
            throw new NullPointerException("Tried to map a null item!");
        }
        objects.put(c, item);
        return this;
    }

    public RecipeBuilderShaped map(char c, Block block) {
        if (block == null) {
            throw new NullPointerException("Tried to map a null block!");
        }
        objects.put(c, block);
        return this;
    }

    public RecipeBuilderShaped map(char c, ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            throw new NullPointerException("Tried to map a null (or empty) stack!");
        }
        objects.put(c, stack);
        return this;
    }

    public RecipeBuilderShaped map(char c, String oreDict) {
        if (oreDict == null) {
            throw new NullPointerException("Tried to map a null oreDict!");
        }
        objects.put(c, oreDict);
        return this;
    }

    public RecipeBuilderShaped map(char c, Object val) {
        if (val == null) {
            throw new NullPointerException("Tried to map to a null object!");
        }
        if (val instanceof Item || val instanceof Block || val instanceof ItemStack || val instanceof String) {
            objects.put(c, val);
            return this;
        }
        throw new IllegalArgumentException("Invalid value " + val.getClass());
    }

    // map(char, toTry, fallback)
    // Maps char to firsnNonNull(toTry, fallback)

    public RecipeBuilderShaped map(char c, Item toTry, Item fallback) {
        if (toTry != null) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    public RecipeBuilderShaped map(char c, Item toTry, ItemStack fallback) {
        if (toTry != null) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    public RecipeBuilderShaped map(char c, Item toTry, String fallback) {
        if (toTry != null) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    public RecipeBuilderShaped map(char c, Block toTry, Block fallback) {
        if (toTry != null) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    public RecipeBuilderShaped map(char c, Block toTry, ItemStack fallback) {
        if (toTry != null) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    public RecipeBuilderShaped map(char c, Block toTry, String fallback) {
        if (toTry != null) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    public RecipeBuilderShaped map(char c, ItemStack toTry, Item fallback) {
        if (toTry != null) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    public RecipeBuilderShaped map(char c, ItemStack toTry, Block fallback) {
        if (!toTry.isEmpty()) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    public RecipeBuilderShaped map(char c, ItemStack toTry, ItemStack fallback) {
        if (!toTry.isEmpty()) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    public RecipeBuilderShaped map(char c, ItemStack toTry, String fallback) {
        if (!toTry.isEmpty()) {
            return map(c, toTry);
        }
        return map(c, fallback);
    }

    // Util

    public Object[] createRecipeObjectArray() {
        Object[] objs = new Object[shape.size() + objects.size() * 2];
        int offset = 0;
        for (String s : shape) {
            objs[offset++] = s;
        }
        for (char c : objects.keys()) {
            objs[offset++] = Character.valueOf(c);
            objs[offset++] = objects.get(c);
        }
        return objs;
    }

    public void setResult(@Nonnull ItemStack result) {
        this.result = result;
    }

    public ShapedOreRecipe build() {
        if (result.isEmpty()) {
            throw new IllegalStateException("Tried to build without setting the result!");
        }
        return build(result);
    }

    public ShapedOreRecipe build(@Nonnull ItemStack resultStack) {
        if (resultStack.isEmpty()) {
            throw new IllegalArgumentException("Provided an empty resultStack!");
        }
        return new ShapedOreRecipe(resultStack, createRecipeObjectArray());
    }

    public NBTAwareShapedOreRecipe buildNbtAware() {
        if (result.isEmpty()) {
            throw new IllegalStateException("Tried to build without setting the result!");
        }
        return buildNbtAware(result);
    }

    public NBTAwareShapedOreRecipe buildNbtAware(@Nonnull ItemStack resultStack) {
        if (resultStack.isEmpty()) {
            throw new IllegalArgumentException("Provided an empty resultStack!");
        }
        return new NBTAwareShapedOreRecipe(resultStack, createRecipeObjectArray());
    }

    public ShapedOreRecipe buildRotated() {
        int fromRows = shape.size();
        int toRows = shape.get(0).length();
        StringBuilder[] strings = new StringBuilder[toRows];
        for (int toRow = 0; toRow < toRows; toRow++) {
            strings[toRow] = new StringBuilder();
        }
        for (int fromRow = 0; fromRow < fromRows; fromRow++) {
            String toAdd = shape.get(fromRow);
            for (int toRow = 0; toRow < toRows; toRow++) {
                strings[toRow].append(toAdd.charAt(toRow));
            }
        }
        Object[] objs = new Object[toRows + objects.size() * 2];
        int offset = 0;
        for (int i = 0; i < strings.length; i++) {
            objs[offset++] = strings[i].toString();
        }
        for (char c : objects.keys()) {
            objs[offset++] = Character.valueOf(c);
            objs[offset++] = objects.get(c);
        }
        return new ShapedOreRecipe(result, objs);
    }
}
