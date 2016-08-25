package buildcraft.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.*;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.guide.node.NodePageLine;
import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.gui.GuiRectangle;
import buildcraft.lib.gui.GuiStack;
import buildcraft.lib.gui.ISimpleDrawable;

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
            String[] ordered = entry.typeTags.getOrdered(order);

            NodePageLine node = parentNode;
            int indent = 1;
            for (int i = 0; i < ordered.length; i++) {
                String line = ordered[i];
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
                    GuideChapter text = new GuideChapterWithin(gui, indent, translated);
                    node = node.addChild(text);
                }
                indent++;
            }

            String translatedTitle = I18n.format(entry.title);
            ItemStack stack = entry.getItemStack();
            ISimpleDrawable icon = null;
            if (stack != null) {
                icon = new GuiStack(stack);
            }
            PageLine line = new PageLine(icon, icon, indent, translatedTitle, true);
            GuideText text = new GuideText(gui, line);
            node.addChild(text);
            pageEntries.put(text, entry);
        }

        parentNode.sortChildrenRecursivly();
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
            if (part instanceof GuideChapter) {
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
        PagePosition pos = new PagePosition(0, 0);
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

    @Override
    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton, int index, boolean isEditing) {
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
                    return;
                }
                oY += 14;
            }
        }
        GuidePart part = getClicked(parentNode.iterateNonNullLines(), x, y, width, height, mouseX, mouseY, index);
        if (part != null) {
            PageEntry entry = pageEntries.get(part);
            if (entry != null) {
                GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(entry);
                if (factory != null) {
                    gui.openPage(factory.createNew(gui));
                } else {
                    BCLog.logger.warn("Somehow encountered a null link factory! (line = " + part + ", link = " + entry + ")");
                }
            } else {
                BCLog.logger.warn("Somehow encountered a null link! (line = " + part + ")");
            }
        }
    }
}
