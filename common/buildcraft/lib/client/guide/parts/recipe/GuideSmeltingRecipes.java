package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import net.minecraftforge.oredict.OreDictionary;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.misc.StackUtil;

public enum GuideSmeltingRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(ItemStack stack) {

        if (stack.getMetadata() == OreDictionary.WILDCARD_VALUE) {
            List<GuidePartFactory> list = new ArrayList<>();
            for (Entry<ItemStack, ItemStack> recipe : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
                if (StackUtil.doesEitherStackMatch(stack, StackUtil.asNonNull(recipe.getValue()))//
                    || StackUtil.doesEitherStackMatch(stack, StackUtil.asNonNull(recipe.getKey()))) {
                    list.add(new GuideSmeltingFactory(recipe.getKey(), recipe.getValue()));
                }
            }
            return list;
        }

        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);

        if (!result.isEmpty()) {
            return ImmutableList.of(new GuideSmeltingFactory(stack, result));
        }

        return null;
    }

    @Override
    public List<GuidePartFactory> getRecipes(ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();

        for (Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            ItemStack input = StackUtil.asNonNull(entry.getKey());
            ItemStack output =  StackUtil.asNonNull(entry.getValue());
            if (StackUtil.doesEitherStackMatch(stack, output)) {
                list.add(new GuideSmeltingFactory(input, output));
            }
        }

        return list;
    }
}
