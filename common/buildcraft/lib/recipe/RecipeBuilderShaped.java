package buildcraft.lib.recipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import net.minecraftforge.oredict.ShapedOreRecipe;

import buildcraft.api.core.BCLog;

import gnu.trove.map.hash.TCharObjectHashMap;

public class RecipeBuilderShaped {
    private final ItemStack result;
    private final List<String> shape = new ArrayList<>();
    private final TCharObjectHashMap<Object> objects = new TCharObjectHashMap<>();

    public RecipeBuilderShaped(ItemStack result) {
        this.result = result;
    }

    public RecipeBuilderShaped add(String row) {
        if (shape.size() > 0 && shape.get(0).length() != row.length()) {
            throw new IllegalArgumentException("Badly sized row!");
        }
        shape.add(row);
        return this;
    }

    public RecipeBuilderShaped map(char c, Item item) {
        objects.put(c, item);
        return this;
    }

    public RecipeBuilderShaped map(char c, Block block) {
        objects.put(c, block);
        return this;
    }

    public RecipeBuilderShaped map(char c, ItemStack stack) {
        objects.put(c, stack);
        return this;
    }

    public RecipeBuilderShaped map(char c, String oreDict) {
        objects.put(c, oreDict);
        return this;
    }

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

    public ShapedOreRecipe build() {
        return new ShapedOreRecipe(result, createRecipeObjectArray());
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
        BCLog.logger.info("Rotated from " + shape + " to " + Arrays.toString(objs));
        return new ShapedOreRecipe(result, objs);
    }
}
