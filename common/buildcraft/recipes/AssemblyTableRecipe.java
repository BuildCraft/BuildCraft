package buildcraft.recipes;

import javax.annotation.Nonnull;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.util.JsonUtils;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.JsonContext;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import buildcraft.api.mj.MjAPI;

public class AssemblyTableRecipe extends ShapelessOreRecipe {
    private final long mjCost;

    public AssemblyTableRecipe(ResourceLocation group, NonNullList<Ingredient> input, long mjCost, @Nonnull ItemStack result) {
        super(group, input, result);
        this.mjCost = mjCost;
    }

    @Override
    public boolean matches(@Nonnull InventoryCrafting inventory, @Nonnull World world) {
        return  super.matches(inventory, world);
    }

    public static ShapelessOreRecipe factory(JsonContext context, JsonObject json)
    {
        String group = JsonUtils.getString(json, "group", "");

        NonNullList<Ingredient> ingredients = NonNullList.create();
        for (JsonElement ele : JsonUtils.getJsonArray(json, "ingredients"))
            ingredients.add(CraftingHelper.getIngredient(ele, context));

            JsonElement element = json.get("MJ");
            if (element == null) {
                throw new JsonParseException("Missing MJ powercost!");
            }
            long mjCost = element.getAsLong() * MjAPI.MJ;

            if (mjCost <= 0) {
                throw new JsonParseException("MJ powercost cannot has to be more then 0");
            }



        if (ingredients.isEmpty())
            throw new JsonParseException("No ingredients for shapeless recipe");

        ItemStack result = ShapedRecipes.deserializeItem(JsonUtils.getJsonObject(json, "result"), true);
        return new AssemblyTableRecipe(group.isEmpty() ? null : new ResourceLocation(group), ingredients, mjCost, result);
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
