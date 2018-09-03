package buildcraft.lib.client.guide.parts.contents;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;

public class PageLinkItemStack extends PageLink {

    public final ItemStack stack;
    public final List<String> tooltip;
    public final String searchText;

    public PageLinkItemStack(boolean startVisible, ItemStack stack) {
        super(createPageLine(stack), startVisible);
        this.stack = stack;
        tooltip = GuiUtil.getFormattedTooltip(stack);
        String joinedTooltip = tooltip.stream().collect(Collectors.joining(" ", "", ""));
        searchText = TextFormatting.getTextWithoutFormattingCodes(joinedTooltip).toLowerCase(Locale.ROOT);
    }

    private static PageLine createPageLine(ItemStack stack) {
        ISimpleDrawable icon = new GuiStack(stack);
        return new PageLine(icon, icon, 2, stack.getDisplayName(), true);
    }

    @Override
    public String getSearchName() {
        return searchText;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip.size() == 1 ? null : tooltip;
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        return GuideManager.INSTANCE.getPageFor(stack);
    }
}
