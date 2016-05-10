package buildcraft.lib.client.guide;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;
import buildcraft.lib.client.guide.PageMeta.TypeOrder;
import buildcraft.lib.client.guide.node.NodePageLine;
import buildcraft.lib.client.guide.parts.GuidePageBase;

/** The base menu for showing all the locations. Should never be registered with and guide managers, this is special and
 * controls them all. */
public class GuideMenu extends GuidePageBase {
    /** Map of type (block, item, etc) -> List of pages for each (Quarry, Paintbrush, etc...) */
    private final Map<ResourceLocation, PageMeta> metaMap = Maps.newHashMap();
    private final Map<PageLine, ResourceLocation> pageLinks = Maps.newHashMap();

    private NodePageLine parentNode;

    public GuideMenu(GuiGuide gui) {
        super(gui);
        loadMainGui();
    }

    public void loadMainGui() {
        parentNode = new NodePageLine(null, null);
        TypeOrder order = GuiGuide.SORTING_TYPES[gui.sortingOrderIndex];

        for (ResourceLocation location : GuideManager.registeredPages.keySet()) {
            PageMeta meta = GuideManager.getPageMeta(location);
            IComparableLine[] locations = meta.getLocationArray(order);
            NodePageLine node = parentNode;
            int indent = 1;
            for (int i = 0; i < locations.length; i++) {
                IComparableLine line = locations[i];
                String translated = I18n.format(line.getText());
                boolean notFound = true;
                for (NodePageLine childNode : node.getChildren()) {
                    if (translated.equals(childNode.pageLine.text)) {
                        node = childNode;
                        notFound = false;
                        break;
                    }
                }
                if (notFound) {
                    node = node.addChild(new PageLine(GuiGuide.BOX_MINUS, GuiGuide.BOX_SELECTED_MINUS, indent, translated, line, false));
                }
                indent++;
            }
            String translatedTitle = I18n.format(meta.title);
            PageLine line = new PageLine(indent, translatedTitle, true);
            node.addChild(line);
            metaMap.put(location, meta);
            pageLinks.put(line, location);
        }
        parentNode.sortChildrenRecursivly();
    }

    @Override
    protected void renderPage(int x, int y, int width, int height, int index) {
        renderLines(parentNode.iterateOnlyExpandedLines(), x, y, width, height, index);
        if (numPages == -1) {
            PagePart part = new PagePart(0, 0);
            for (PageLine line : parentNode.iterateOnlyExpandedLines()) {
                part = renderLine(part, part, line, x, y, width, height, index);
            }
            numPages = part.page + 1;
        }
        super.renderPage(x, y, width, height, index);
    }

    @Override
    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton, int index, boolean isEditing) {
        super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index, isEditing);
        int wY = gui.sortingOrderIndex * 10;
        if (mouseX > 0 && mouseX < 10 && mouseY > wY && mouseY < wY + 10) {
            gui.sortingOrderIndex++;
            if (gui.sortingOrderIndex >= GuiGuide.SORTING_TYPES.length) {
                gui.sortingOrderIndex = 0;
            }
            loadMainGui();
        } else {
            PageLine line = getClicked(parentNode.iterateOnlyExpandedLines(), x, y, width, height, mouseX, mouseY, index);
            if (line != null) {
                ResourceLocation location = pageLinks.get(line);
                if (location != null) {
                    BCLog.logger.info("Opening " + location);
                    gui.openPage(GuideManager.getPage(location, gui));
                } else {
                    BCLog.logger.warn("Somehow encountered a null link! (line = " + line + ")");
                }
            }
            // because its impossible to click both the text and the icon
            else {
                PageLine iconLine = getIconClicked(parentNode.iterateOnlyExpandedLines(), x, y, width, height, mouseX, mouseY, index);
                if (iconLine != null) {
                    NodePageLine node = parentNode.getChildNode(iconLine);
                    if (node != null) {
                        if (node.expanded) {
                            iconLine.startIcon = GuiGuide.BOX_PLUS;
                            iconLine.startIconHovered = GuiGuide.BOX_SELECTED_PLUS;
                        } else {
                            iconLine.startIcon = GuiGuide.BOX_MINUS;
                            iconLine.startIconHovered = GuiGuide.BOX_SELECTED_MINUS;
                        }
                        // Make it recalculate the number of pages
                        numPages = -1;
                        node.expanded = !node.expanded;
                    }
                }
            }
        }
    }
}
