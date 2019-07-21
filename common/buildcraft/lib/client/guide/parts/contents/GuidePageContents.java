/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts.contents;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextFormatting;

import buildcraft.lib.BCLib;
import buildcraft.lib.BCLibConfig;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.TypeOrder;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.parts.GuideChapter;
import buildcraft.lib.client.guide.parts.GuidePageBase;
import buildcraft.lib.client.render.font.ConfigurableFontRenderer;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.GuiUtil;
import buildcraft.lib.misc.GuiUtil.WrappedTextData;
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;
import buildcraft.lib.misc.search.ISuffixArray.SearchResult;

/** The base menu for showing all the locations. Should never be registered with and guide managers, this is special and
 * controls them all. */
public class GuidePageContents extends GuidePageBase {
    private static final int ORDER_OFFSET_X = -10;
    private static final int ORDER_OFFSET_Y = -10;

    private ContentsNodeGui contents;
    private final GuiTextField searchText;
    private String lastSearchText = "";
    /** -1 if all of the results can be displayed or the actual number of results if it's too many. */
    private int realResultCount = -1;

    public GuidePageContents(GuiGuide gui) {
        super(gui);
        loadMainGui();
        FontRenderer fr = new ConfigurableFontRenderer(gui.mc.fontRenderer).disableShadow();
        searchText = new GuiTextField(0, fr, 0, 0, 80, fr.FONT_HEIGHT + 5);
        searchText.setEnableBackgroundDrawing(false);
        searchText.setTextColor(0xFF_00_00_00);
        setupChapters();
    }

    @Override
    public GuidePageBase createReloaded() {
        GuidePageContents newPage = new GuidePageContents(gui);
        newPage.searchText.setText(searchText.getText());
        newPage.searchText.setCursorPosition(searchText.getCursorPosition());
        newPage.searchText.setFocused(searchText.isFocused());
        newPage.searchText.setSelectionPos(searchText.getSelectionEnd());
        newPage.numPages = numPages;
        newPage.goToPage(getIndex());
        return newPage;
    }

    public void loadMainGui() {
        contents = GuideManager.INSTANCE.getGuiContents(gui, this, gui.sortingOrder);
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
        if (lastSearchText.equals(searchText.getText())) {
            if (numPages >= 3 && getPage() >= numPages) {
                goToPage(numPages);
            }
        } else {
            lastSearchText = searchText.getText();
            numPages = -1;
            if (lastSearchText.isEmpty()) {
                realResultCount = -1;
                contents.node.resetVisibility();
                contents.invalidate();
                setupChapters();
            } else {
                String text = lastSearchText.toLowerCase(Locale.ROOT);
                SearchResult<PageLink> ret = GuideManager.INSTANCE.quickSearcher.search(
                    text, BCLibConfig.maxGuideSearchCount
                );
                realResultCount = ret.hasAllResults() ? -1 : ret.realResultCount;
                Set<PageLink> matches = new HashSet<>(ret.results);
                contents.node.setVisible(matches);
                contents.invalidate();

                if (contents.node.isVisible()) {
                    searchText.setTextColor(0xFF_00_00_00);
                } else {
                    searchText.setTextColor(0xFF_FF_00_00);
                }
                if (getPage() < 2) {
                    goToPage(2);
                }
                setupChapters();
            }
            gui.refreshChapters();
        }
    }

    @Override
    protected void renderPage(int x, int y, int width, int height, int index) {
        IFontRenderer f = getFontRenderer();
        if (index == 0) {
            int xMiddle = x + width / 2;
            int _y = y;
            String text = gui.book == null ? "Everything" : gui.book.title.getUnformattedText();
            WrappedTextData wrapped = GuiUtil.getWrappedTextData(text, f, width, false, 3f);
            wrapped.drawAt(xMiddle, _y, 0, true);
            _y += wrapped.height;
            if (true) {
                f.drawString("v" + BCLib.VERSION, xMiddle, _y, 0, false, true);
            }
            _y = y + height - 80;
            f.drawString(LocaleUtil.localize("options.title"), xMiddle, _y, 0, false, true, 2f);
            _y += 28;
            f.drawString("Show Lore " + (XmlPageLoader.SHOW_LORE ? "[x]" : "[ ]"), xMiddle, _y, 0, false, true);
            _y += 14;
            f.drawString("Show Hints " + (XmlPageLoader.SHOW_HINTS ? "[x]" : "[ ]"), xMiddle, _y, 0, false, true);
        } else if (index == 1) {
            int _height = gui.bookData.loadedMods.size() + 1;
            if (gui.bookData.loadedOther.size() > 0) {
                _height++;
                _height += gui.bookData.loadedOther.size();
            }
            int perLineHeight = f.getFontHeight("Ly") + 3;
            _height *= perLineHeight;
            int _y = y + (height - _height) / 2;

            if (gui.bookData.loadedMods.size() > 0) {
                drawCenteredText(TextFormatting.BOLD + "Loaded Mods:", x, _y, width);
                _y += perLineHeight;
                for (String text : gui.bookData.loadedMods) {
                    drawCenteredText(text, x, _y, width);
                    _y += perLineHeight;
                }
            }
            if (gui.bookData.loadedOther.size() > 0) {
                drawCenteredText(TextFormatting.BOLD + "Loaded Resource Packs:", x, _y, width);
                _y += perLineHeight;
                for (String text : gui.bookData.loadedOther) {
                    drawCenteredText(text, x, _y, width);
                    _y += perLineHeight;
                }
            }
        }
        if (index % 2 == 0) {
            searchText.x = x + 23;
            searchText.y = y - 16;
            if (!searchText.isFocused() && searchText.getText().isEmpty()) {
                GuiGuide.SEARCH_TAB_CLOSED.drawAt(x + 8, y - 20);
                GuiGuide.SEARCH_ICON.drawAt(x + 8, y - 19);
            } else {
                GuiGuide.SEARCH_TAB_OPEN.drawAt(x - 2, y - 22);
                GuiGuide.SEARCH_ICON.drawAt(x + 8, y - 18);
            }
            searchText.drawTextBox();
            if (realResultCount >= 0) {
                String text = LocaleUtil.localize("buildcraft.guide.too_many_results", realResultCount);
                getFontRenderer().drawString(text, x + 105, y - 23, -1);
            }
        }
        RenderUtil.setGLColorFromInt(-1);
        PagePosition pos = new PagePosition(2, 0);

        pos = contents.render(x, y, width, height, pos, index);

        if (numPages == -1) {
            numPages = pos.page + 1;
        }
        super.renderPage(x, y, width, height, index);
        if (index != 0 && index % 2 == 0) {
            int oX = x + ORDER_OFFSET_X;
            int oY = y + ORDER_OFFSET_Y;
            for (int j = 0; j < GuiGuide.ORDERS.length; j++) {
                GuiIcon icon = GuiGuide.ORDERS[j];
                TypeOrder typeOrder = GuiGuide.SORTING_TYPES[j];
                if (gui.sortingOrder == typeOrder) {
                    icon = icon.offset(0, 14);
                }
                if (icon.containsGuiPos(oX, oY, gui.mouse)) {
                    icon = icon.offset(0, 28);
                    gui.tooltips.add(Collections.singletonList(LocaleUtil.localize(typeOrder.localeKey)));
                }
                icon.drawAt(oX, oY);
                oY += 14;
            }
        }
    }

    private void drawCenteredText(String text, int x, int y, int width) {
        IFontRenderer f = getFontRenderer();
        int fWidth = f.getStringWidth(text);
        f.drawString(text, (x + (width - fWidth) / 2), y, 0);
    }

    @Override
    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton,
        int index, boolean isEditing) {
        super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index, isEditing);
        if (index % 2 == 0) {
            int oX = x + ORDER_OFFSET_X;
            int oY = y + ORDER_OFFSET_Y;
            for (TypeOrder order : GuiGuide.SORTING_TYPES) {
                GuiRectangle rect = new GuiRectangle(oX, oY, 14, 14);
                if (rect.contains(gui.mouse)) {
                    gui.sortingOrder = order;
                    loadMainGui();
                    lastSearchText = "@@@@INVALID@@@";
                    gui.refreshChapters();
                    contents.setFontRenderer(getFontRenderer());
                    return;
                }
                oY += 14;
            }
            if (!searchText.mouseClicked(mouseX, mouseY, mouseButton) && !searchText.isFocused()
                && new GuiRectangle(x - 2, y - 34, 40, 34).contains(mouseX, mouseY)) {
                searchText.setFocused(true);
            }
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
                rect = new GuiRectangle(x + (width - fWidth) / 2, y + height - 52, fWidth, f.getFontHeight(text));
                if (rect.contains(mouseX, mouseY)) {
                    XmlPageLoader.SHOW_LORE = !XmlPageLoader.SHOW_LORE;
                }

                text = XmlPageLoader.SHOW_HINTS ? "Show Hints [x]" : "Show Hints [ ]";
                fWidth = f.getStringWidth(text);
                rect = new GuiRectangle(x + (width - fWidth) / 2, y + height - 38, fWidth, f.getFontHeight(text));
                if (rect.contains(mouseX, mouseY)) {
                    XmlPageLoader.SHOW_HINTS = !XmlPageLoader.SHOW_HINTS;
                }
            }
        }

        if (new GuiRectangle(x, y, width, height).contains(mouseX, mouseY)) {
            contents.onClicked(x, y, width, height, new PagePosition(2, 0), index);
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) throws IOException {
        return searchText.textboxKeyTyped(typedChar, keyCode);
    }
}
