package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

public class PageLinkNormal extends PageLink {

    public final GuidePageFactory factory;
    public final List<ITextComponent> tooltip;

    public PageLinkNormal(PageLine text, boolean startVisible, List<ITextComponent> tooltip, GuidePageFactory factory) {
        super(text, startVisible);
        this.factory = factory;
        this.tooltip =
                (tooltip == null || tooltip.size() != 1 || strip(tooltip.get(0).getString()).equals(strip(text.text.getString()))) ? null : tooltip;
    }

    private static String strip(String text) {
        return TextFormatting.stripFormatting(text.trim());
    }

    @Override
    public List<ITextComponent> getTooltip() {
        return tooltip;
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        return factory;
    }
}
