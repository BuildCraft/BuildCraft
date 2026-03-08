package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuideText;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public abstract class PageLink implements IContentsLeaf {

    public final PageLine text;
    public final String textKey;
    public final boolean startVisible;
    // private final String lowerCaseName;
    private final ITextComponent lowerCaseName;
    private boolean visible;

    public PageLink(PageLine text, boolean startVisible) {
        this.text = text;
        this.startVisible = startVisible;
//        lowerCaseName = text.text.toLowerCase(Locale.ROOT);
        lowerCaseName = text.text;
        textKey = text.textKey.toLowerCase(Locale.ROOT);
        visible = startVisible;
    }

    @Override
//    public String getSearchName()
    public ITextComponent getSearchName() {
        return lowerCaseName;
    }

    @Override
    public String getKey() {
        return textKey;
    }

    /** @return The tooltip to be shown if it is different to the search name and displayed text. */
    @Nullable
    protected List<ITextComponent> getTooltip() {
        return null;
    }

    public void appendTooltip(GuiGuide gui) {
        List<ITextComponent> tooltip = getTooltip();
        if (tooltip != null && !tooltip.isEmpty()) {
            gui.tooltips.add(tooltip);
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(Set<PageLink> matches) {
        visible = matches.contains(this);
    }

    @Override
    public void resetVisibility() {
        visible = startVisible;
    }

    @Override
    public GuidePart createGuidePart(GuiGuide gui) {
        return new GuideText(gui, text) {
            @Override
            protected void renderTooltip() {
                appendTooltip(gui);
            }
        };
    }

    public abstract GuidePageFactory getFactoryLink();
}
