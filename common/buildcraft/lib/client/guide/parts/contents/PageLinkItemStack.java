package buildcraft.lib.client.guide.parts.contents;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.minecraft.item.ItemStack;
import net.minecraft.profiler.Profiler;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;

public final class PageLinkItemStack extends PageLink {

    private static final boolean FULL_TOOLITP = true;

    public final ItemStack stack;
    public final List<String> tooltip;
    public final String searchText;

    public static PageLinkItemStack create(boolean startVisible, ItemStack stack, Profiler prof) {
        prof.startSection("create_page_link");
        prof.startSection("get_tooltip");
        List<String> tooltip = getTooltip(stack);
        prof.endStartSection("join_tooltip");
        String searchText = joinTooltip(tooltip);
        prof.endStartSection("create_line");
        ISimpleDrawable icon = new GuiStack(stack);
        PageLine text = new PageLine(icon, icon, 2, tooltip.get(0), true);
        prof.endSection();
        PageLinkItemStack page = new PageLinkItemStack(text, startVisible, stack, tooltip, searchText);
        prof.endSection();
        return page;
    }

    private static List<String> getTooltip(ItemStack stack) {
        if (FULL_TOOLITP) {
            return GuiUtil.getUnFormattedTooltip(stack);
        }
        return Collections.singletonList(GuiUtil.getStackDisplayName(stack));
    }

    private static String joinTooltip(final List<String> tooltip) {
        StringBuilder joiner = new StringBuilder();
        joinTooltipLine(tooltip, joiner, 0);
        for (int i = 1; i < tooltip.size(); i++) {
            joiner.append('\n');
            joinTooltipLine(tooltip, joiner, i);
        }
        return joiner.toString();
    }

    private static void joinTooltipLine(final List<String> tooltip, StringBuilder joiner, int i) {
        joiner.append(removeFormatting(tooltip.get(i)).toLowerCase(Locale.ROOT));
    }

    private static String removeFormatting(String s) {
        char[] to = new char[s.length()];
        int len = 0;
        for (int ci = 0; ci < s.length(); ci++) {
            char c = s.charAt(ci);
            if (c == '§') {
                ci++;
                continue;
            }
            to[len++] = c;
        }
        return new String(to, 0, len);
    }

    private PageLinkItemStack(
        PageLine text, boolean startVisible, ItemStack stack, List<String> tooltip, String searchText
    ) {
        super(text, startVisible);
        this.stack = stack;
        this.tooltip = tooltip;
        this.searchText = searchText;
    }

    private PageLinkItemStack(boolean startVisible, ItemStack stack, Profiler prof) {
        super(createPageLine(stack, prof), startVisible);
        this.stack = stack;
        prof.startSection("get_tooltip");
        tooltip = getTooltip(stack);
        prof.endStartSection("join_tooltip");
        searchText = joinTooltip(tooltip);
        prof.endSection();
    }

    private static PageLine createPageLine(ItemStack stack, Profiler prof) {
        prof.startSection("create_line");
        ISimpleDrawable icon = new GuiStack(stack);
        prof.startSection("get_display_name");
        String title = GuiUtil.getStackDisplayName(stack);
        prof.endSection();
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
