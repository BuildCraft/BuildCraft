package buildcraft.lib.client.guide.parts.contents;

import java.util.List;
import java.util.Locale;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;
import net.minecraft.util.text.TextFormatting;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;

public final class PageLinkItemStack extends PageLink {

    public final ItemStack stack;
    public final List<String> tooltip;
    public final String searchText;

    public static PageLinkItemStack create(boolean startVisible, ItemStack stack, Profiler prof) {
        prof.startSection("create_page_link");
        prof.startSection("get_tooltip");
        List<String> tooltip = GuiUtil.getUnFormattedTooltip(stack);
        prof.endStartSection("join_tooltip");
        StringBuilder joiner = new StringBuilder();
        for (int i = 0; i < tooltip.size(); i++) {
            String s = tooltip.get(i);
            s = TextFormatting.getTextWithoutFormattingCodes(s);
            if (s == null) {
                continue;
            }
            tooltip.set(i, s);
            if (i > 0) {
                joiner.append('\n');
            }
            joiner.append(s.toLowerCase(Locale.ROOT));
        }
        String searchText = joiner.toString();
        prof.endStartSection("create_line");
        ISimpleDrawable icon = new GuiStack(stack);
        PageLine text = new PageLine(icon, icon, 2, tooltip.get(0), true);
        prof.endSection();
        PageLinkItemStack page = new PageLinkItemStack(text, startVisible, stack, tooltip, searchText);
        prof.endSection();
        return page;
    }

    private PageLinkItemStack(PageLine text, boolean startVisible, ItemStack stack, List<String> tooltip,
        String searchText) {
        super(text, startVisible);
        this.stack = stack;
        this.tooltip = tooltip;
        this.searchText = searchText;
    }

    private PageLinkItemStack(boolean startVisible, ItemStack stack, Profiler prof) {
        super(createPageLine(stack, prof), startVisible);
        this.stack = stack;
        prof.startSection("get_tooltip");
        tooltip = GuiUtil.getUnFormattedTooltip(stack);
        prof.endStartSection("join_tooltip");
        StringBuilder joiner = new StringBuilder();
        for (String s : tooltip) {
            s = TextFormatting.getTextWithoutFormattingCodes(s);
            if (s == null) {
                continue;
            }
            joiner.append(s.toLowerCase(Locale.ROOT));
            joiner.append('\n');
        }
        searchText = joiner.toString();
        prof.endSection();
    }

    private static PageLine createPageLine(ItemStack stack, Profiler prof) {
        prof.startSection("create_line");
        ISimpleDrawable icon = new GuiStack(stack);
        prof.startSection("get_display_name");
        String title = stack.getDisplayName();
        prof.endSection();
        if (title == null) {
            // Temp workaround for headcrumbs
            // TODO: Remove this after https://github.com/BuildCraft/BuildCraft/issues/4268 is fixed from their side! */
            Item item = stack.getItem();
            String info = item.getRegistryName() + " " + item.getClass() + " (" + stack.serializeNBT() + ")";
            BCLog.logger.warn("[lib.guide] Found null display name! " + info);
            title = "!!NULL stack.getDisplayName(): " + info;
        }
        PageLine line = new PageLine(icon, icon, 2, title, true);
        prof.endSection();
        return line;
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
