package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.GuiUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class PageLinkItemStack extends PageLink {

    private static final boolean FULL_TOOLITP = true;

    public final ItemStack stack;
    public final List<Component> tooltip;
    // public final String searchText;
    public final Component searchText;

    public static PageLinkItemStack create(boolean startVisible, ItemStack stack, ProfilerFiller prof) {
        prof.push("create_page_link");
        prof.push("get_tooltip");
        List<Component> tooltip = getTooltip(stack);
        prof.popPush("join_tooltip");
        String searchText = joinTooltip(tooltip);
        prof.popPush("create_line");
        ISimpleDrawable icon = new GuiStack(stack);
//        PageLinkItemStack page = new PageLinkItemStack(text, startVisible, stack, tooltip, searchText);
        PageLine text = new PageLine(icon, icon, 2, tooltip.get(0).getString(), tooltip.get(0), true);
        prof.pop();
        PageLinkItemStack page = new PageLinkItemStack(text, startVisible, stack, tooltip, searchText);
        prof.pop();
        return page;
    }

    private static List<Component> getTooltip(ItemStack stack) {
        if (FULL_TOOLITP) {
            return GuiUtil.getUnFormattedTooltip(stack);
        }
        return Collections.singletonList(GuiUtil.getStackDisplayName(stack));
    }

    private static String joinTooltip(final List<Component> tooltip) {
        StringBuilder joiner = new StringBuilder();
        joinTooltipLine(tooltip, joiner, 0);
        for (int i = 1; i < tooltip.size(); i++) {
            joiner.append('\n');
            joinTooltipLine(tooltip, joiner, i);
        }
        return joiner.toString();
    }

    private static void joinTooltipLine(final List<Component> tooltip, StringBuilder joiner, int i) {
        joiner.append(removeFormatting(tooltip.get(i).getString()).toLowerCase(Locale.ROOT));
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
            PageLine text, boolean startVisible, ItemStack stack, List<Component> tooltip, String searchText
    ) {
        super(text, startVisible);
        this.stack = stack;
        this.tooltip = tooltip;
//        this.searchText = searchText;
        this.searchText = new TextComponent(searchText);
    }

    // private PageLinkItemStack(boolean startVisible, ItemStack stack, Profiler prof)
    private PageLinkItemStack(boolean startVisible, ItemStack stack, ProfilerFiller prof) {
        super(createPageLine(stack, prof), startVisible);
        this.stack = stack;
        prof.push("get_tooltip");
        tooltip = getTooltip(stack);
        prof.popPush("join_tooltip");
//        searchText = joinTooltip(tooltip);
        searchText = new TextComponent(joinTooltip(tooltip));
        prof.pop();
    }

    // private static PageLine createPageLine(ItemStack stack, Profiler prof)
    private static PageLine createPageLine(ItemStack stack, ProfilerFiller prof) {
        prof.push("create_line");
        ISimpleDrawable icon = new GuiStack(stack);
        prof.push("get_display_name");
        Component title = GuiUtil.getStackDisplayName(stack);
        prof.pop();
        PageLine line = new PageLine(icon, icon, 2, title.getString(), title, true);
        prof.pop();
        return line;
    }

    @Override
//    public String getSearchName()
    public Component getSearchName() {
        return searchText;
    }

    @Override
    public List<Component> getTooltip() {
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
