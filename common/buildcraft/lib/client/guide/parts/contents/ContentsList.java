package buildcraft.lib.client.guide.parts.contents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuideChapterWithin;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.parts.contents.ContentsList.Title.SubHeader;

/** Stores a list of titles. */
public class ContentsList {

    private final Map<String, Title> titles = new HashMap<>();
    private Title[] sortedTitles = null;
    boolean anyVisible;

    public Title getOrAddTitle(String title) {
        sortedTitles = null;
        return titles.computeIfAbsent(title, Title::new);
    }

    public SubHeader getOrAddSubHeader(String title, String subHeader) {
        sortedTitles = null;
        return getOrAddTitle(title).getOrAddSubHeader(subHeader);
    }

    public void sortAll() {
        sortedTitles = titles.values().toArray(new Title[0]);
        Arrays.sort(sortedTitles, Comparator.comparing(t -> t.title));
        for (Title title : sortedTitles) {
            title.sortHeaders();
        }
    }

    public void setFontRenderer(IFontRenderer fontRenderer) {
        if (sortedTitles == null) {
            sortAll();
        }
        for (Title title : sortedTitles) {
            title.setFontRenderer(fontRenderer);
        }
    }

    public List<GuideChapter> getChapters(GuiGuide gui) {
        if (sortedTitles == null) {
            sortAll();
        }
        List<GuideChapter> list = new ArrayList<>();
        for (Title title : sortedTitles) {
            list.add(new GuideChapterWithin(gui, chapter));
        }
        return list;
    }

    private void calcVisibility() {
        anyVisible = false;
        for (Title page : sortedTitles) {
            if (page.anyVisible) {
                anyVisible = true;
                break;
            }
        }
    }

    public class Title {
        public final String title;
        private final Map<String, SubHeader> subHeaders = new HashMap<>();
        private SubHeader[] sortedHeaders = null;
        boolean anyVisible;

        public Title(String title) {
            this.title = title;
        }

        public SubHeader getOrAddSubHeader(String subHeader) {
            sortedHeaders = null;
            return subHeaders.computeIfAbsent(subHeader, SubHeader::new);
        }

        private void sortHeaders() {
            sortedHeaders = subHeaders.values().toArray(new SubHeader[0]);
            Arrays.sort(sortedHeaders, Comparator.comparing(s -> s.subHeader));
            for (SubHeader header : sortedHeaders) {
                header.sortPages();
            }
        }

        private void setFontRenderer(IFontRenderer fontRenderer) {
            if (sortedHeaders == null) {
                throw new IllegalStateException("Must always call sort() before setFontRenderer!");
            }
            for (SubHeader subHeader : sortedHeaders) {
                subHeader.setFontRenderer(fontRenderer);
            }
        }

        private void calcVisibility() {
            anyVisible = false;
            for (SubHeader page : sortedHeaders) {
                if (page.anyVisible) {
                    anyVisible = true;
                    break;
                }
            }
            ContentsList.this.calcVisibility();
        }

        public class SubHeader {
            public final String subHeader;
            private final List<PageLink> pages = new ArrayList<>();
            boolean anyVisible;

            public SubHeader(String subHeader) {
                this.subHeader = subHeader;
            }

            private void sortPages() {
                pages.sort(Comparator.naturalOrder());
            }

            private void calcVisibility() {
                anyVisible = false;
                for (PageLink page : pages) {
                    if (page.isVisible()) {
                        anyVisible = true;
                        break;
                    }
                }
                Title.this.calcVisibility();
            }

            private void setFontRenderer(IFontRenderer fontRenderer) {
                for (PageLink pageLink : pages) {
                    pageLink.text.setFontRenderer(fontRenderer);
                }
            }

            public void addNormalPage(GuideText text, GuidePageFactory factory) {
                pages.add(new PageLinkNormal(text, factory));
            }

            public abstract class PageLink implements Comparable<PageLink> {
                public final GuideText text;
                private boolean visible;

                public PageLink(GuideText text) {
                    this.text = text;
                }

                public boolean isVisible() {
                    return visible;
                }

                public void setVisible(boolean visible) {
                    if (visible == this.visible) {
                        return;
                    }
                    if (visible) {
                        this.visible = true;
                        SubHeader.this.anyVisible = true;
                        Title.this.anyVisible = true;
                        ContentsList.this.anyVisible = true;
                    } else {
                        this.visible = false;
                        SubHeader.this.calcVisibility();
                    }
                }

                private final String getName() {
                    return text.text.text;
                }

                public final int compareTo(PageLink o) {
                    return getName().compareTo(o.getName());
                }
            }

            public class PageLinkNormal extends PageLink {

                @Nullable
                public final GuidePageFactory factory;

                public PageLinkNormal(GuideText text, GuidePageFactory factory) {
                    super(text);
                    this.factory = factory;
                }
            }

            public class PageLinkTag extends PageLink {
                public final NonNullList<ItemStack> containedStacks;

                PageLinkTag(GuideText text, NonNullList<ItemStack> containedStacks) {
                    super(text);
                    this.containedStacks = containedStacks;
                }
            }

            public class PageLinkGenerated extends PageLink {
                public final ItemStack stack;

                PageLinkGenerated(GuideText text, ItemStack stack) {
                    super(text);
                    this.stack = stack;
                }
            }
        }
    }
}
