package buildcraft.lib.recipe;

import buildcraft.api.core.BCLog;
import gnu.trove.map.hash.TCharObjectHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class RecipeBuilderShaped {
    private ItemStack result;
    private final List<String> shape = new ArrayList<>();
    private final TCharObjectHashMap<Object> objects = new TCharObjectHashMap<>();

    public RecipeBuilderShaped add(String row) {
        if (shape.size() > 0 && shape.get(0).length() != row.length()) {
            throw new IllegalArgumentException("Badly sized row!");
        }
        shape.add(row);
        return this;
    }

    public RecipeBuilderShaped map(char c, Object... vals) {
        Arrays.stream(vals)
                .filter(val ->
                        !(val == null ||
                                val instanceof Item ||
                                val instanceof Block ||
                                val instanceof ItemStack ||
                                val instanceof String)
                )
                .findFirst()
                .ifPresent(val -> {
                    throw new IllegalArgumentException("Invalid value " + val.getClass());
                });
        objects.put(c, Arrays.stream(vals).filter(Objects::nonNull).findFirst().orElse(null));
        return this;
    }

    public RecipeBuilderShaped setResult(ItemStack result) {
        this.result = result;
        return this;
    }

    public Object[] createRecipeObjectArray() {
        Object[] objs = new Object[shape.size() + objects.size() * 2];
        int offset = 0;
        for (String s : shape) {
            objs[offset++] = s;
        }
        for (char c : objects.keys()) {
            objs[offset++] = c;
            objs[offset++] = objects.get(c);
        }
        return objs;
    }

    public boolean isValid() {
        return result != null && objects.valueCollection().stream().allMatch(Objects::nonNull);
    }

    public ShapedOreRecipe build() {
        return isValid() ? new ShapedOreRecipe(result, createRecipeObjectArray()) : null;
    }

    public NBTAwareShapedOreRecipe buildNbtAware() {
        return isValid() ? new NBTAwareShapedOreRecipe(result, createRecipeObjectArray()) : null;
    }

    public ShapedOreRecipe buildRotated() {
        if (!isValid()) {
            return null;
        }

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
            objs[offset++] = c;
            objs[offset++] = objects.get(c);
        }
        BCLog.logger.info("Rotated from " + Arrays.toString(createRecipeObjectArray()) + " to " + Arrays.toString(objs));
        return new ShapedOreRecipe(result, objs);
    }

    public void register() {
        IRecipe recipe = build();
        if (recipe != null) {
            GameRegistry.addRecipe(recipe);
        }
    }

    public void registerNbtAware() {
        IRecipe recipe = buildNbtAware();
        if (recipe != null) {
            GameRegistry.addRecipe(recipe);
        }
    }

    public void registerRotated() {
        IRecipe recipe = buildRotated();
        if (recipe != null) {
            GameRegistry.addRecipe(recipe);
        }
    }
}
