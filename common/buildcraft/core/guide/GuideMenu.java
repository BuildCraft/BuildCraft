package buildcraft.core.guide;

import java.util.Map;

import com.google.common.collect.Maps;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;
import buildcraft.core.guide.node.NodePageLine;
import buildcraft.core.guide.parts.GuidePageBase;

/** The base menu for showing all the locations. Should never be registered with and guide managers, this is special and
 * controls them all. */
public class GuideMenu extends GuidePageBase {
    /** Map of type (block, item, etc) -> List of pages for each (Quarry, Paintbrush, etc...) */
    private final Map<ResourceLocation, PageMeta> metaMap = Maps.newHashMap();
    private final Map<PageLine, ResourceLocation> pageLinks = Maps.newHashMap();

    private final NodePageLine parentNode = new NodePageLine(null, null);

    public GuideMenu(GuiGuide gui) {
        super(gui);
        Map<String, NodePageLine> chapterNodeMap = Maps.newHashMap();

        for (ResourceLocation location : GuideManager.registeredPages.keySet()) {
            // Split the location to find the type
            String type = location.getResourcePath();
            if (!type.contains("/")) {
                BCLog.logger.warn("The location " + location + " did not contain any folder seperators! This is a bug!");
            } else {
                if (type.startsWith("guide/")) {
                    type = type.substring("guide/".length());
                }
                type = type.substring(0, type.indexOf("/"));
                if (!chapterNodeMap.containsKey(type)) {
                    String translated = I18n.format("buildcraft.guide.chapter." + type);
                    PageLine line = new PageLine(GuiGuide.BOX_MINUS, GuiGuide.BOX_SELECTED_MINUS, 0, translated, false);
                    chapterNodeMap.put(type, parentNode.addChild(line));
                }
                PageMeta meta = GuideManager.getPageMeta(location);
                String[] locations = meta.getLocationArray();
                NodePageLine node = chapterNodeMap.get(type);
                int indent = 1;
                for (int i = 0; i < locations.length; i++) {
                    String loc = locations[i];
                    if (StringUtils.isEmpty(loc)) {
                        continue;
                    }
                    String translated = I18n.format("buildcraft.guide.chapter." + loc);
                    boolean notFound = true;
                    for (NodePageLine line : node.getChildren()) {
                        if (translated.equals(line.pageLine.text)) {
                            node = line;
                            notFound = false;
                            break;
                        }
                    }
                    if (notFound) {
                        node = node.addChild(new PageLine(GuiGuide.BOX_MINUS, GuiGuide.BOX_SELECTED_MINUS, indent, translated, false));
                    }
                    indent++;
                }
                String translated = meta.title;
                PageLine line = new PageLine(indent, translated, true);
                node.addChild(line);
                metaMap.put(location, meta);
                pageLinks.put(line, location);
            }
        }
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
