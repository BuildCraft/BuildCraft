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

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextFormatting;

import buildcraft.api.BCModules;

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
import buildcraft.lib.misc.LocaleUtil;
import buildcraft.lib.misc.RenderUtil;

/** The base menu for showing all the locations. Should never be registered with and guide managers, this is special and
 * controls them all. */
public class GuidePageContents extends GuidePageBase {
    private static final int ORDER_OFFSET_X = -50;
    private static final int ORDER_OFFSET_Y = 14;

    private ContentsNodeGui contents;
    private final GuiTextField searchText;
    private String lastSearchText = "";
    private int realResultCount = -1;

    public GuidePageContents(GuiGuide gui) {
        super(gui);
        loadMainGui();
        FontRenderer fr = new ConfigurableFontRenderer(gui.mc.fontRenderer).disableShadow();
        searchText = new GuiTextField(0, fr, 0, 0, 80, fr.FONT_HEIGHT + 5);
        searchText.setEnableBackgroundDrawing(false);
        searchText.setTextColor(0xFF_00_00_00);
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
            if (numPages > 3 && getPage() > numPages) {
                goToPage(numPages);
            } else if (getPage() < 2) {
                searchText.setFocused(false);
            }
        } else {
            lastSearchText = searchText.getText();
            numPages = -1;
            if (lastSearchText.isEmpty()) {
                contents.node.resetVisibility();
                contents.invalidate();
            } else {
                String text = lastSearchText.toLowerCase(Locale.ROOT);
                List<PageLink> ret = GuideManager.INSTANCE.quickSearcher.search(text);
                if (ret.size() > BCLibConfig.maxGuideSearchCount) {
                    realResultCount = ret.size();
                    ret.subList(BCLibConfig.maxGuideSearchCount, ret.size()).clear();
                } else {
                    realResultCount = -1;
                }
                Set<PageLink> matches = new HashSet<>(ret);
                contents.node.setVisible(matches);
                contents.invalidate();

                if (contents.node.isVisible()) {
                    searchText.setTextColor(0xFF_00_00_00);
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
            boolean first = true;
            for (String text : GuideManager.loadedMods) {

                if (first && text.contains("modules")) {
                    // BuildCraft
                    double mouseX = gui.mouse.getX();
                    double mouseY = gui.mouse.getY();
                    if (mouseX >= x + width / 2 && mouseX <= x + width * 7 / 8//
                        && mouseY >= _y && mouseY <= _y + perLineHeight) {
                        gui.tooltip.add(LocaleUtil.localize("buildcraft.guide.contents.loaded_modules"));
                        for (BCModules module : BCModules.getLoadedModules()) {
                            gui.tooltip.add((GuideManager.buildCraftModules.contains(module) ? " + " : " - ")
                                + StringUtils.capitalize(module.lowerCaseName));
                        }
                        if (BCModules.getMissingModules().length > 0) {
                            gui.tooltip.add(LocaleUtil.localize("buildcraft.guide.contents.missing_modules"));
                            for (BCModules module : BCModules.getMissingModules()) {
                                gui.tooltip.add(" - " + StringUtils.capitalize(module.lowerCaseName));
                            }
                        }
                    }
                }

                drawCenteredText(text, x, _y, width);
                _y += perLineHeight;
                first = false;
            }
            if (GuideManager.loadedOther.size() > 0) {
                drawCenteredText(TextFormatting.BOLD + "Loaded Resource Packs:", x, _y, width);
                _y += perLineHeight;
                for (String text : GuideManager.loadedOther) {
                    drawCenteredText(text, x, _y, width);
                    _y += perLineHeight;
                }
            }
        } else if (index % 2 == 0) {
            searchText.x = x + 23;
            searchText.y = y - 23;
            if (!searchText.isFocused() && searchText.getText().isEmpty()) {
                GuiGuide.SEARCH_TAB_CLOSED.drawAt(x + 8, y - 20);
                GuiGuide.SEARCH_ICON.drawAt(x + 8, y - 19);
            } else {
                GuiGuide.SEARCH_TAB_OPEN.drawAt(x - 2, y - 29);
                GuiGuide.SEARCH_ICON.drawAt(x + 8, y - 25);
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
        if (index % 2 == 0) {
            int oX = x + ORDER_OFFSET_X;
            int oY = y + ORDER_OFFSET_Y;
            int i = 0;
            for (int j = 0; j < GuiGuide.ORDERS.length; j++) {
                GuiIcon icon = GuiGuide.ORDERS[j];
                if (gui.sortingOrder == GuiGuide.SORTING_TYPES[j]) {
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
            for (TypeOrder order : GuiGuide.SORTING_TYPES) {
                GuiRectangle rect = new GuiRectangle(oX, oY, 14, 14);
                if (rect.contains(gui.mouse)) {
                    gui.sortingOrder = order;
                    loadMainGui();
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
            contents.onClicked(x, y, width, height, new PagePosition(2, 0), index);
        }
    }

    @Override
    public boolean keyTyped(char typedChar, int keyCode) throws IOException {
        return searchText.textboxKeyTyped(typedChar, keyCode);
    }
}
