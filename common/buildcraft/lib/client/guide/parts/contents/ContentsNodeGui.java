package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuidePart.PagePosition;
import buildcraft.lib.misc.ArrayUtil;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class ContentsNodeGui {
    public final GuiGuide gui;
    public final ContentsNode node;

    private IFontRenderer fontRenderer;
    private List<GuideChapter> chapters;
    private GuidePart[] parts;
    private PageLink[] links;

    public ContentsNodeGui(GuiGuide gui, ContentsNode node) {
        this.gui = gui;
        this.node = node;
    }

    public List<GuideChapter> getChapters() {
        if (populate() || chapters == null) {
            chapters = new ArrayList<>();
            for (GuidePart part : parts) {
                if (part instanceof GuideChapter) {
                    chapters.add((GuideChapter) part);
                }
            }
        }
        return chapters;
    }

    public void setFontRenderer(IFontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
        if (parts != null) {
            for (GuidePart part : parts) {
                part.setFontRenderer(fontRenderer);
            }
        }
    }

    public void invalidate() {
        parts = null;
        links = null;
        chapters = null;
    }

    private boolean populate() {
        if (parts == null) {
            List<GuidePart> allText = new ArrayList<>();
            List<PageLink> allLinks = new ArrayList<>();

            // Depth first search
            Deque<IContentsNode> queue = new ArrayDeque<>();
            ArrayUtil.addAllReversed(queue, node.getVisibleChildren());
            while (!queue.isEmpty()) {
                IContentsNode next = queue.removeLast();
                GuidePart part = next.createGuidePart(gui);
                if (fontRenderer != null) {
                    part.setFontRenderer(fontRenderer);
                }
                allText.add(part);
                if (next instanceof PageLink) {
                    allLinks.add((PageLink) next);
                } else {
                    allLinks.add(null);
                }
                ArrayUtil.addAllReversed(queue, next.getVisibleChildren());
            }
            parts = allText.toArray(new GuidePart[0]);
            links = allLinks.toArray(new PageLink[0]);
            return true;
        }
        return false;
    }

    public PagePosition render(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index) {
        return iterate(current, height, (pos, part, link) ->
        {
            return part.renderIntoArea(guiGraphics, x, y, width, height, pos, index);
        });
    }

    public void onClicked(GuiGraphics guiGraphics, int x, int y, int width, int height, PagePosition current, int index) {
        iterate(current, height, (pos, part, link) ->
        {
            pos = part.renderIntoArea(guiGraphics, x, y, width, height, pos, -1);
            if (pos.page == index && part.wasHovered()) {
                if (link != null) {
                    GuidePageFactory factory = link.getFactoryLink();
                    GuidePageBase page = factory.createNew(gui);
                    if (page != null) {
                        gui.openPage(page);
                        return null;
                    }
                }
            }
            return pos;
        });
    }

    @FunctionalInterface
    private interface IGuideBitIter {
        @Nullable
        PagePosition iterate(PagePosition pos, GuidePart part, PageLink link);
    }

    @Nullable
    private PagePosition iterate(PagePosition pos, int height, IGuideBitIter iter) {
        populate();
        for (int i = 0; i < links.length; i++) {
            GuidePart part = parts[i];
            PageLink link = links[i];

            int space = 16;// gui.getCurrentFont().getFontHeight("EXAMPLE");
            if (link == null) {
                for (int j = i; j < links.length; j++) {
                    if (links[j] != null) {
                        pos = pos.guaranteeSpace(space * (1 + j - i), height);
                        break;
                    }
                }
            }

            pos = iter.iterate(pos, part, link);
            if (pos == null) {
                return null;
            }
        }
        return pos;
    }
}
