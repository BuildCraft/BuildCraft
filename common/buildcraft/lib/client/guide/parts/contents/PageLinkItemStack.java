package buildcraft.lib.client.guide.parts.contents;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.GuiGuide;
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
        String title = stack.getDisplayName();
        if (title == null) {
            // Temp workaround for headcrumbs
            // TODO: Remove this after https://github.com/BuildCraft/BuildCraft/issues/4268 is fixed from their side! */
            Item item = stack.getItem();
            String info = item.getRegistryName() + " " + item.getClass() + " (" + stack.serializeNBT() + ")";
            BCLog.logger.warn("[lib.guide] Found null display name! " + info);
            title = "!!NULL stack.getDisplayName(): " + info;
        }
        return new PageLine(icon, icon, 2, title, true);
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
    public void appendTooltip(GuiGuide gui) {
        if (tooltip.size() > 1) {
            gui.tooltipStack = stack;
        }
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        return GuideManager.INSTANCE.getPageFor(stack);
    }
}
