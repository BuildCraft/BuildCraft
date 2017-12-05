/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLib;
import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.PageEntry;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.TypeOrder;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.loader.XmlPageLoader;
import buildcraft.lib.client.guide.node.NodePageLine;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;
import buildcraft.lib.gui.pos.GuiRectangle;
import buildcraft.lib.misc.LocaleUtil;

/** The base menu for showing all the locations. Should never be registered with and guide managers, this is special and
 * controls them all. */
public class GuidePageContents extends GuidePageBase {
    private static final int ORDER_OFFSET_X = -50;
    private static final int ORDER_OFFSET_Y = 14;

    private final Map<GuidePart, PageEntry> pageEntries = new HashMap<>();

    private NodePageLine parentNode;

    public GuidePageContents(GuiGuide gui) {
        super(gui);
        loadMainGui();
    }

    public void loadMainGui() {
        parentNode = new NodePageLine(null, null);
        TypeOrder order = GuiGuide.SORTING_TYPES[gui.sortingOrderIndex];

        for (PageEntry entry : GuideManager.INSTANCE.getAllEntries()) {
            if (GuideManager.INSTANCE.getFactoryFor(entry) == null) {
                continue;
            }
            String[] ordered = entry.typeTags.getOrdered(order);

            NodePageLine node = parentNode;
            int indent = 1;
            for (String line : ordered) {
                String translated = TextFormatting.UNDERLINE + I18n.format(line);
                boolean notFound = true;
                for (NodePageLine childNode : node.getChildren()) {
                    if (childNode.part instanceof GuideChapter) {
                        if (translated.equals(((GuideChapter) childNode.part).chapter.text)) {
                            node = childNode;
                            notFound = false;
                            break;
                        }
                    }
                }
                if (notFound) {
                    node = node.addChild(new GuideChapterWithin(gui, indent, translated));
                }
                indent++;
            }

            String translatedTitle = I18n.format(entry.title);
            ItemStack stack = entry.getItemStack();
            ISimpleDrawable icon = null;
            if (!stack.isEmpty()) {
                icon = new GuiStack(stack);
            }
            PageLine line = new PageLine(icon, icon, indent, translatedTitle, true);
            GuideText text = new GuideText(gui, line);
            node.addChild(text);
            pageEntries.put(text, entry);
        }

        parentNode.sortChildrenRecursively();
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        parentNode.setFontRenderer(fontRenderer);
    }

    @Override
    public List<GuideChapter> getChapters() {
        List<GuideChapter> list = new ArrayList<>();
        for (GuidePart part : parentNode.iterateNonNullLines()) {
            if (part instanceof GuideChapter && ((GuideChapter) part).chapter.indent == 1) {
                list.add((GuideChapter) part);
            }
        }
        return list;
    }

    @Override
    public String getTitle() {
        return null;
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
                _height ++;
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
        PagePosition pos = new PagePosition(2, 0);
        for (GuidePart part : parentNode.iterateNonNullLines()) {
            pos = part.renderIntoArea(x, y, width, height, pos, index);
        }
        if (numPages == -1) {
            numPages = pos.page + 1;
        }
        // renderLines(parentNode.iterateNonNullLines(), x, y, width, height, index);
        // if (numPages == -1) {
        // PagePosition part = new PagePosition(0, 0);
        // for (PageLine line : parentNode.iterateNonNullLines()) {
        // part = renderLine(part, line, x, y, width, height, index);
        // if (part.page > index) {
        // break;
        // }
        // }
        // numPages = part.page + 1;
        // }
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
                    parentNode.setFontRenderer(getFontRenderer());
                    return;
                }
                oY += 14;
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
        GuidePart part = getClicked(parentNode.iterateNonNullLines(), x, y, width, height, mouseX, mouseY, index - 2);
        if (part != null) {
            PageEntry entry = pageEntries.get(part);
            if (entry != null) {
                GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(entry);
                if (factory != null) {
                    gui.openPage(factory.createNew(gui));
                } else {
                    BCLog.logger
                        .warn("Somehow encountered a null link factory! (line = " + part + ", link = " + entry + ")");
                }
            } else {
                BCLog.logger.warn("Somehow encountered a null link! (line = " + part + ")");
            }
        }
    }
}
