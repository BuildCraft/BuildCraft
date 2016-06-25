package buildcraft.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.loader.MarkdownPageLoader;

public class GuidePageStandInRecipes extends GuidePage {
    public GuidePageStandInRecipes(GuiGuide gui, List<GuidePart> parts, ItemStack stack) {
        super(gui, parts, stack.getDisplayName());
    }

    @Nonnull
    public static GuidePageFactory createFactory(ItemStack stack) {
        List<GuidePartFactory> factories = MarkdownPageLoader.loadAllCrafting(stack);
        return (gui) -> {
            List<GuidePart> parts = new ArrayList<>();
            for (GuidePartFactory factory : factories) {
                parts.add(factory.createNew(gui));
            }
            return new GuidePageStandInRecipes(gui, parts, stack);
        };
    }

    @Override
    public boolean shouldPersistHistory() {
        return false;
    }
}
