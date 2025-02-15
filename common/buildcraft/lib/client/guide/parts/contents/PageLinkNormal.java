package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public class PageLinkNormal extends PageLink {

    public final GuidePageFactory factory;
    public final List<Component> tooltip;

    public PageLinkNormal(PageLine text, boolean startVisible, List<Component> tooltip, GuidePageFactory factory) {
        super(text, startVisible);
        this.factory = factory;
        this.tooltip =
                (tooltip == null || tooltip.size() != 1 || strip(tooltip.get(0).getString()).equals(strip(text.text.getString()))) ? null : tooltip;
    }

    private static String strip(String text) {
        return ChatFormatting.stripFormatting(text.trim());
    }

    @Override
    public List<Component> getTooltip() {
        return tooltip;
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        return factory;
    }
}
