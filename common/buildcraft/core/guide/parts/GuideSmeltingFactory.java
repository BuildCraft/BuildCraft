package buildcraft.core.guide.parts;

import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;

import buildcraft.core.guide.GuiGuide;

public class GuideSmeltingFactory extends GuidePartFactory<GuideSmelting> {
    private final ItemStack input, output;

    public GuideSmeltingFactory(ItemStack input, ItemStack output) {
        this.input = input;
        this.output = output;
    }

    @SuppressWarnings("unchecked")
    public static GuideSmeltingFactory create(ItemStack stack) {
        for (Entry<ItemStack, ItemStack> entry : (Set<Entry<ItemStack, ItemStack>>) FurnaceRecipes.instance().getSmeltingList().entrySet()) {
            if (ItemStack.areItemsEqual(stack, entry.getValue())) {
                return new GuideSmeltingFactory(entry.getKey(), stack);
            }
        }
        return null;
    }

    public static GuideSmeltingFactory create(Item output) {
        return create(new ItemStack(output));
    }

    @Override
    public GuideSmelting createNew(GuiGuide gui) {
        return new GuideSmelting(gui, input, output);
    }
}
