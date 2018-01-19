package buildcraft.lib.client.guide.parts.contents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.api.core.BCLog;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuideChapterWithin;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuidePart.PagePosition;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.parts.contents.ContentsList.Title.SubHeader;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.misc.StringUtilBC;

/** Stores a list of titles. */
public class ContentsList {

    public final GuiGuide gui;
    private final Map<String, Title> titles = new HashMap<>();
    private List<GuideChapter> chapters;
    Title[] sortedTitles = null;
    Title[] visibleTitles = null;

    public ContentsList(GuiGuide gui) {
        this.gui = gui;
    }

    public Title getOrAddTitle(String title) {
        sortedTitles = null;
        return titles.computeIfAbsent(title, Title::new);
    }

    public SubHeader getOrAddSubHeader(String title, String subHeader) {
        sortedTitles = null;
        return getOrAddTitle(title).getOrAddSubHeader(subHeader);
    }

    public void clear() {
        titles.clear();
        chapters = null;
        sortedTitles = null;
        visibleTitles = null;
    }

    public void sortAll() {
        sortedTitles = titles.values().toArray(new Title[0]);
        Arrays.sort(sortedTitles, StringUtilBC.compareBasicReadable(t -> t.title));
        for (Title title : sortedTitles) {
            title.sortHeaders();
        }
        calcVisibility();
    }

    public void setFontRenderer(IFontRenderer fontRenderer) {
        if (sortedTitles == null) {
            sortAll();
        }
        for (Title title : sortedTitles) {
            title.setFontRenderer(fontRenderer);
        }
    }

    public List<GuideChapter> getChapters() {
        if (chapters == null) {
            if (visibleTitles == null) {
                sortAll();
            }
            GuideChapter[] chArray = new GuideChapter[visibleTitles.length];
            for (int i = 0; i < visibleTitles.length; i++) {
                chArray[i] = visibleTitles[i].chapter;
            }
            chapters = Arrays.asList(chArray);
        }
        return chapters;
    }

    public boolean isVisible() {
        return visibleTitles != null && visibleTitles.length != 0;
    }

    private void calcVisibility() {
        List<Title> visible = new ArrayList<>();
        for (Title title : sortedTitles) {
            if (title.isVisible()) {
                visible.add(title);
            }
        }
        visibleTitles = visible.toArray(new Title[0]);
        chapters = null;
    }

    public class Title {
        public final String title;
        public final GuideChapterWithin chapter;
        private final Map<String, SubHeader> subHeaders = new HashMap<>();
        SubHeader[] sortedHeaders = null;
        SubHeader[] visibleHeaders = null;

        public Title(String title) {
            this.title = title;
            chapter = new GuideChapterWithin(gui, title);
        }

        public boolean isVisible() {
            return visibleHeaders != null && visibleHeaders.length != 0;
        }

        public SubHeader getOrAddSubHeader(String subHeader) {
            sortedHeaders = null;
            return subHeaders.computeIfAbsent(subHeader, SubHeader::new);
        }

        private void sortHeaders() {
            sortedHeaders = subHeaders.values().toArray(new SubHeader[0]);
            Arrays.sort(sortedHeaders, StringUtilBC.compareBasicReadable(s -> s.subHeader));
            for (SubHeader header : sortedHeaders) {
                header.sortPages();
            }
            calcVisibility();
        }

        private void setFontRenderer(IFontRenderer fontRenderer) {
            if (sortedHeaders == null) {
                throw new IllegalStateException("Must always call sort() before setFontRenderer!");
            }
            chapter.setFontRenderer(fontRenderer);
            for (SubHeader subHeader : sortedHeaders) {
                subHeader.setFontRenderer(fontRenderer);
            }
        }

        private void calcVisibility() {
            List<SubHeader> visible = new ArrayList<>();
            for (SubHeader page : sortedHeaders) {
                if (page.isVisible()) {
                    visible.add(page);
                }
            }
            visibleHeaders = visible.toArray(new SubHeader[0]);
        }

        public class SubHeader {
            public final String subHeader;
            public final GuideText text;
            final List<PageLink> pages = new ArrayList<>();
            PageLink[] visiblePages = null;

            public SubHeader(String subHeader) {
                this.subHeader = subHeader;
                text = new GuideText(gui, new PageLine(2, subHeader, false));
            }

            public boolean isVisible() {
                return visiblePages != null && visiblePages.length != 0;
            }

            private void sortPages() {
                pages.sort(StringUtilBC.compareBasicReadable(PageLink::getName));
                calcVisibility();
            }

            private void calcVisibility() {
                List<PageLink> visible = new ArrayList<>();
                for (PageLink page : pages) {
                    if (page.isVisible()) {
                        visible.add(page);
                    }
                }
                visiblePages = visible.toArray(new PageLink[0]);
            }

            private void setFontRenderer(IFontRenderer fontRenderer) {
                text.setFontRenderer(fontRenderer);
                for (PageLink pageLink : pages) {
                    pageLink.text.setFontRenderer(fontRenderer);
                }
            }

            public PageLinkNormal addNormalPage(GuideText text, GuidePageFactory factory) {
                PageLinkNormal page = new PageLinkNormal(text, factory);
                pages.add(page);
                return page;
            }

            @Nullable
            public PageLinkGenerated addKnownPage(GuideText text, ItemStack stack) {
                if (stack.isEmpty()) {
                    return null;
                }
                PageLinkGenerated pageLink = new PageLinkGenerated(text, stack, true);
                pages.add(pageLink);
                return pageLink;
            }

            @Nullable
            public PageLinkGenerated addUnknownStack(ItemStack stack) {
                if (stack.isEmpty()) {
                    return null;
                }
                ISimpleDrawable icon = new GuiStack(stack);
                PageLine line = new PageLine(icon, icon, 2, stack.getDisplayName(), true);
                GuideText textEntry = new GuideText(gui, line);
                PageLinkGenerated pageLink = new PageLinkGenerated(textEntry, stack, false);
                pages.add(pageLink);
                return pageLink;
            }

            public abstract class PageLink {
                public final GuideText text;
                public final boolean startVisible;
                private boolean visible;

                PageLink(GuideText text, boolean startVisible) {
                    this.text = text;
                    this.startVisible = startVisible;
                    this.visible = startVisible;
                }

                public boolean isVisible() {
                    return visible;
                }

                public void setVisible(boolean visible) {
                    if (visible == this.visible) {
                        return;
                    }
                    this.visible = visible;
                    SubHeader.this.calcVisibility();
                    Title.this.calcVisibility();
                    ContentsList.this.calcVisibility();
                }

                public final String getName() {
                    return text.text.text;
                }

                public abstract void onClicked();

                public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition pos, int index) {
                    return text.renderIntoArea(x, y, width, height, pos, index);
                }
            }

            public class PageLinkNormal extends PageLink {

                public final GuidePageFactory factory;

                public PageLinkNormal(GuideText text, GuidePageFactory factory) {
                    super(text, true);
                    this.factory = factory;
                }

                @Override
                public void onClicked() {
                    gui.openPage(factory.createNew(gui));
                }
            }

            public class PageLinkTag extends PageLink {
                public final NonNullList<ItemStack> containedStacks;

                PageLinkTag(GuideText text, NonNullList<ItemStack> containedStacks) {
                    super(text, false);
                    this.containedStacks = containedStacks;
                }

                @Override
                public void onClicked() {
                    BCLog.logger.info("[lib.guide] Clicked tag entry! (" + text.text.text + ")");
                    // gui.openPage(null);
                    // TODO!
                }
            }

            public class PageLinkGenerated extends PageLink {
                public final ItemStack stack;
                public final List<String> tooltip;
                public final String joinedTooltip;

                PageLinkGenerated(GuideText text, ItemStack stack, boolean forceVisible) {
                    super(text, forceVisible);
                    this.stack = stack;
                    tooltip = gui.getItemToolTip(stack);
                    joinedTooltip = tooltip.stream().collect(Collectors.joining(" ", "", ""));
                }

                @Override
                public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition pos, int index) {
                    pos = super.renderIntoArea(x, y, width, height, pos, index);
                    if (pos.page == index && text.wasHovered() && tooltip.size() > 1) {
                        gui.tooltipStack = stack;
                    }
                    return pos;
                }

                @Override
                public void onClicked() {
                    gui.openPage(GuideManager.INSTANCE.getPageFor(stack).createNew(gui));
                }
            }
        }
    }
}
