/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.util.SuffixArray;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;

import net.minecraftforge.fml.common.registry.ForgeRegistries;

import buildcraft.lib.BCLib;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageEntry;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.TypeOrder;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.guide.parts.GuidePageFactory;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.client.guide.parts.contents.ContentsList.Title;
import buildcraft.lib.client.guide.parts.contents.ContentsList.Title.SubHeader;
import buildcraft.lib.client.guide.parts.contents.ContentsList.Title.SubHeader.PageLink;
import buildcraft.lib.client.guide.parts.contents.ContentsList.Title.SubHeader.PageLinkGenerated;
import buildcraft.lib.client.guide.parts.contents.ContentsList.Title.SubHeader.PageLinkNormal;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;

/** The base menu for showing all the locations. Should never be registered with and guide managers, this is special and
 * controls them all. */
public class GuidePageContents extends GuidePageBase {
    private static final int ORDER_OFFSET_X = -50;
    private static final int ORDER_OFFSET_Y = 14;

    private final GuiTextField searchText;
    private String lastSearchText = "";
    private SuffixArray<PageLink> quickSearcher;

    private final ContentsList contents;

    public GuidePageContents(GuiGuide gui) {
        super(gui);
        contents = new ContentsList(gui);
        loadMainGui();
        FontRenderer fr = gui.mc.fontRenderer;
        searchText = new GuiTextField(0, fr, 0, 0, 80, fr.FONT_HEIGHT + 5);
        searchText.setEnableBackgroundDrawing(false);
    }

    public void loadMainGui() {
        TypeOrder order = GuiGuide.SORTING_TYPES[gui.sortingOrderIndex];
        contents.clear();
        quickSearcher = new SuffixArray<>();

        Set<Item> itemsAdded = new HashSet<>();

        final String underline = TextFormatting.UNDERLINE.toString();
        for (PageEntry entry : GuideManager.INSTANCE.getAllEntries()) {
            GuidePageFactory entryFactory = GuideManager.INSTANCE.getFactoryFor(entry);

            String[] ordered = entry.typeTags.getOrdered(order);

            String header = underline + LocaleUtil.localize(ordered[0]);
            String subHeader = underline + LocaleUtil.localize(ordered[1]);

            String translatedTitle = LocaleUtil.localize(entry.title);
            ItemStack stack = entry.getItemStack();
            ISimpleDrawable icon = null;
            if (!stack.isEmpty()) {
                icon = new GuiStack(stack);
                itemsAdded.add(stack.getItem());
            }
            PageLine line = new PageLine(icon, icon, 2, translatedTitle, true);
            GuideText text = new GuideText(gui, line);
            SubHeader pageHolder = contents.getOrAddSubHeader(header, subHeader);
            if (entryFactory == null) {
                PageLinkGenerated pageLink = pageHolder.addKnownPage(text, entry.getItemStack());
                if (pageLink != null) {
                    quickSearcher.add(pageLink, pageLink.joinedTooltip.toLowerCase(Locale.ROOT));
                }
            } else {
                PageLinkNormal pageLink = pageHolder.addNormalPage(text, entryFactory);
                quickSearcher.add(pageLink, pageLink.getName().toLowerCase(Locale.ROOT));
            }
        }

        String localizedGroup = underline + "\u0379" + LocaleUtil.localize("buildcraft.guide.contents.all_group");
        String localizedItems = underline + LocaleUtil.localize("buildcraft.guide.contents.item_stacks");

        SubHeader allHolder = contents.getOrAddSubHeader(localizedGroup, localizedItems);
        for (Item item : ForgeRegistries.ITEMS) {
            if (itemsAdded.contains(item)) {
                continue;
            }
            NonNullList<ItemStack> stacks = NonNullList.create();
            item.getSubItems(CreativeTabs.SEARCH, stacks);
            for (int i = 0; i < stacks.size(); i++) {
                ItemStack stack = stacks.get(i);
                PageLinkGenerated pageLink = allHolder.addUnknownStack(stack);
                if (pageLink != null) {
                    quickSearcher.add(pageLink, pageLink.joinedTooltip.toLowerCase(Locale.ROOT));
                }
                if (i > 50) {
                    // Woah there, lets not fill up entire pages with what is
                    // most likely the same item
                    break;
                }
            }
        }

        quickSearcher.generate();
        contents.sortAll();
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        contents.setFontRenderer(fontRenderer);
    }

    @Override
    public List<GuideChapter> getChapters() {
        return contents.getChapters();
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        searchText.updateCursorCounter();
        if (!lastSearchText.equals(searchText.getText())) {
            lastSearchText = searchText.getText().toLowerCase(Locale.ROOT);
            numPages = -1;
            if (lastSearchText.isEmpty()) {
                for (Title title : contents.sortedTitles) {
                    for (SubHeader subHeader : title.sortedHeaders) {
                        for (PageLink page : subHeader.pages) {
                            page.setVisible(page.startVisible);
                        }
                    }
                }
            } else {
                Set<PageLink> matches = new HashSet<>(quickSearcher.search(lastSearchText));
                for (Title title : contents.sortedTitles) {
                    for (SubHeader subHeader : title.sortedHeaders) {
                        for (PageLink page : subHeader.pages) {
                            page.setVisible(matches.contains(page));
                        }
                    }
                }
                if (contents.isVisible()) {
                    searchText.setTextColor(0xFF_FF_FF_FF);
                } else {
                    searchText.setTextColor(0xFF_FF_00_00);
                }
            }
            gui.refreshChapters();
        }
    }

    @Override
    protected void renderPage(int x, int y, int width, int height, int index) {
        IFontRenderer f = getFontRenderer();
        if (index == 0) {
            String text = "BuildCraft";
            float scale = 3;
            int fWidth = (int) (f.getStringWidth(text) * scale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            f.drawString(text, (int) ((x + (width - fWidth) / 2) / scale), (int) ((y + height / 2 - 62) / scale), 0);
            GlStateManager.popMatrix();

            text = "v" + BCLib.VERSION;
            fWidth = f.getStringWidth(text);
            f.drawString(text, x + (width - fWidth) / 2, y + height / 2 - 36, 0);

            scale = 1.5f;
            text = LocaleUtil.localize("options.title");
            fWidth = (int) (f.getStringWidth(text) * scale);
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
            f.drawString(text, (int) ((x + (width - fWidth) / 2) / scale), (int) ((y + height / 2 - 4) / scale), 0);
            GlStateManager.popMatrix();

            text = XmlPageLoader.SHOW_LORE ? "Show Lore [x]" : "Show Lore [ ]";
            fWidth = f.getStringWidth(text);
            f.drawString(text, x + (width - fWidth) / 2, y + height / 2 + 12, 0);

            text = XmlPageLoader.SHOW_HINTS ? "Show Hints [x]" : "Show Hints [ ]";
            fWidth = f.getStringWidth(text);
            f.drawString(text, x + (width - fWidth) / 2, y + height / 2 + 26, 0);
        } else if (index == 1) {
            int _height = GuideManager.loadedMods.size() + 1;
            if (GuideManager.loadedOther.size() > 0) {
                _height++;
                _height += GuideManager.loadedOther.size();
            }
            int perLineHeight = f.getFontHeight("Ly") + 3;
            _height *= perLineHeight;
            int _y = y + (height - _height) / 2;

            drawCenteredText(TextFormatting.BOLD + "Loaded Mods:", x, _y, width);
            _y += perLineHeight;
            for (String text : GuideManager.loadedMods) {
                drawCenteredText(text, x, _y, width);
                _y += perLineHeight;
            }
            if (GuideManager.loadedOther.size() > 0) {
                drawCenteredText(TextFormatting.BOLD + "Loaded Resource Packs:", x, _y, width);
                _y += perLineHeight;
                for (String text : GuideManager.loadedOther) {
                    drawCenteredText(text, x, _y, width);
                    _y += perLineHeight;
                }
            }
        }

        if (index % 2 == 0) {
            searchText.x = x + 10;
            searchText.y = y - 30;
            searchText.drawTextBox();
        }
        RenderUtil.setGLColorFromInt(-1);
        PagePosition pos = new PagePosition(2, 0);
        for (Title title : contents.visibleTitles) {
            SubHeader firstHeader = title.visibleHeaders[0];
            PageLink firstPage = firstHeader.visiblePages[0];
            pos = pos.guaranteeSpace(32 + gui.getCurrentFont().getFontHeight(firstPage.text.text.text), height);
            pos = title.chapter.renderIntoArea(x, y, width, height, pos, index);
            for (SubHeader header : title.visibleHeaders) {
                firstPage = header.visiblePages[0];
                pos = pos.guaranteeSpace(gui.getCurrentFont().getFontHeight(firstPage.text.text.text), height);
                pos = header.text.renderIntoArea(x, y, width, height, pos, index);
                for (PageLink page : header.visiblePages) {
                    pos = page.renderIntoArea(x, y, width, height, pos, index);
                }
            }
        }
        if (numPages == -1) {
            numPages = pos.page + 1;
        }
        super.renderPage(x, y, width, height, index);
        if (index % 2 == 0) {
            int oX = x + ORDER_OFFSET_X;
            int oY = y + ORDER_OFFSET_Y;
            int i = 0;
            for (GuiIcon icon : GuiGuide.ORDERS) {
                if (gui.sortingOrderIndex == i) {
                    icon = icon.offset(0, 14);
                }
                icon.drawAt(oX, oY);
                oY += 14;
                i++;
            }
        }
    }

    private void drawCenteredText(String text, int x, int y, int width) {
        IFontRenderer f = getFontRenderer();
        int fWidth = f.getStringWidth(text);
        f.drawString(text, (x + (width - fWidth) / 2), y, 0);
    }

    private void drawScaledCenteredText(float scale, String text, int x, int y, int width) {
        IFontRenderer f = getFontRenderer();
        int fWidth = (int) (f.getStringWidth(text) * scale);
        if (scale != 1) {
            GlStateManager.pushMatrix();
            GlStateManager.scale(scale, scale, 1);
        }
        f.drawString(text, (int) ((x + (width - fWidth) / 2) / scale), (int) (y / scale), 0);
        if (scale != 1) {
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton,
        int index, boolean isEditing) {
        super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index, isEditing);
        if (index % 2 == 0) {
            int oX = x + ORDER_OFFSET_X;
            int oY = y + ORDER_OFFSET_Y;
            for (int i = 0; i < GuiGuide.ORDERS.length; i++) {
                GuiRectangle rect = new GuiRectangle(oX, oY, 14, 14);
                if (rect.contains(gui.mouse)) {
                    gui.sortingOrderIndex = i;
                    loadMainGui();
                    gui.refreshChapters();
                    contents.setFontRenderer(getFontRenderer());
                    return;
                }
                oY += 14;
            }
            searchText.mouseClicked(mouseX, mouseY, mouseButton);
            if (mouseButton == 1 && mouseX >= searchText.x && mouseX < searchText.x + searchText.width
                && mouseY >= searchText.y && mouseY < searchText.y + searchText.height) {
                searchText.setText("");
            }
        }
        if (mouseButton == 0) {
            if (index == 0) {
                IFontRenderer f = getFontRenderer();
                String text = XmlPageLoader.SHOW_LORE ? "Show Lore [x]" : "Show Lore [ ]";
                int fWidth = f.getStringWidth(text);
                GuiRectangle rect;
                rect = new GuiRectangle(x + (width - fWidth) / 2, y + height / 2 + 12, fWidth, f.getFontHeight(text));
                if (rect.contains(mouseX, mouseY)) {
                    XmlPageLoader.SHOW_LORE = !XmlPageLoader.SHOW_LORE;
                }

                text = XmlPageLoader.SHOW_HINTS ? "Show Hints [x]" : "Show Hints [ ]";
                fWidth = f.getStringWidth(text);
                rect = new GuiRectangle(x + (width - fWidth) / 2, y + height / 2 + 26, fWidth, f.getFontHeight(text));
                if (rect.contains(mouseX, mouseY)) {
                    XmlPageLoader.SHOW_HINTS = !XmlPageLoader.SHOW_HINTS;
                }
            }
        }

        if (new GuiRectangle(x, y, width, height).contains(mouseX, mouseY)) {
            PagePosition pos = new PagePosition(2, 0);
            search: for (Title title : contents.visibleTitles) {
                SubHeader firstHeader = title.visibleHeaders[0];
                PageLink firstPage = firstHeader.visiblePages[0];
                pos = pos.guaranteeSpace(32 + gui.getCurrentFont().getFontHeight(firstPage.text.text.text), height);
                pos = title.chapter.renderIntoArea(x, y, width, height, pos, -1);
                for (SubHeader header : title.visibleHeaders) {
                    firstPage = header.visiblePages[0];
                    pos = pos.guaranteeSpace(gui.getCurrentFont().getFontHeight(firstPage.text.text.text), height);
                    pos = header.text.renderIntoArea(x, y, width, height, pos, -1);
                    for (PageLink page : header.visiblePages) {
                        pos = page.text.renderIntoArea(x, y, width, height, pos, -1);
                        if (pos.page == index && page.text.wasHovered()) {
                            page.onClicked();
                            break search;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) throws IOException {
        return searchText.textboxKeyTyped(typedChar, keyCode);
    }
}
