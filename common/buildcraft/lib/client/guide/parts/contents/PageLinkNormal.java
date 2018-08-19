package buildcraft.lib.client.guide.parts.contents;

import java.util.List;

import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;

public class PageLinkNormal extends PageLink {

    public final GuidePageFactory factory;
    public final List<String> tooltip;

    public PageLinkNormal(PageLine text, boolean startVisible, List<String> tooltip, GuidePageFactory factory) {
        super(text, startVisible);
        this.factory = factory;
        this.tooltip = tooltip;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        return factory;
    }
}
