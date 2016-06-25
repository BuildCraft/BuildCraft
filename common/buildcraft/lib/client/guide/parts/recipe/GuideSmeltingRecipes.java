package buildcraft.lib.client.guide.parts.recipe;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableList;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import buildcraft.lib.client.guide.parts.GuidePartFactory;
import buildcraft.lib.client.guide.parts.GuideSmeltingFactory;

public enum GuideSmeltingRecipes implements IStackRecipes {
    INSTANCE;

    @Override
    public List<GuidePartFactory> getUsages(ItemStack stack) {
        ItemStack result = FurnaceRecipes.instance().getSmeltingResult(stack);

        if (result != null) {
            return ImmutableList.of(new GuideSmeltingFactory(stack, result));
        }

        return null;
    }

    @Override
    public List<GuidePartFactory> getRecipes(ItemStack stack) {
        List<GuidePartFactory> list = new ArrayList<>();

        for (Entry<ItemStack, ItemStack> entry : FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            ItemStack input = entry.getKey();
            ItemStack output = entry.getValue();
            if (ItemStack.areItemsEqual(stack, output)) {
                list.add(new GuideSmeltingFactory(input, output));
            }
        }

        return list;
    }
}
