package buildcraft.lib.client.guide.parts.contents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import net.minecraft.util.text.TextFormatting;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuideChapterWithin;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.misc.StringUtilBC;

public class ContentsNode implements IContentsNode {

    public final String title;
    public final int indent;
    private final Map<String, IContentsNode> nodes = new HashMap<>();
    private IContentsNode[] sortedNodes = new IContentsNode[0];
    IContentsNode[] visibleNodes = new IContentsNode[0];
    private boolean needsSorting = false;

    public ContentsNode(String title, int indent) {
        this.title = title;
        this.indent = indent;
    }

    @Override
    public String getSearchName() {
        return title;
    }

    @Override
    public GuidePart createGuidePart(GuiGuide gui) {
        if (indent == 0) {
            return new GuideChapterWithin(gui, TextFormatting.UNDERLINE + title);
        } else {
            return new GuideText(gui, new PageLine(indent + 1, TextFormatting.UNDERLINE + title, false));
        }
    }

    @Nullable
    public IContentsNode getChild(String childKey) {
        return nodes.get(childKey);
    }

    @Override
    public void addChild(IContentsNode node) {
        nodes.put(node.getSearchName(), node);
        needsSorting = true;
    }

    @Override
    public IContentsNode[] getVisibleChildren() {
        return visibleNodes;
    }

    @Override
    public boolean isVisible() {
        return visibleNodes.length != 0;
    }

    @Override
    public void sort() {
        if (!needsSorting) {
            return;
        }
        needsSorting = false;
        sortedNodes = nodes.values().toArray(new IContentsNode[0]);
        Arrays.sort(sortedNodes, StringUtilBC.compareBasicReadable(IContentsNode::getSearchName));
        for (IContentsNode node : sortedNodes) {
            node.sort();
        }
        calcVisibility();
    }

    @Override
    public void calcVisibility() {
        List<IContentsNode> visible = new ArrayList<>();
        for (IContentsNode node : sortedNodes) {
            if (node.isVisible()) {
                visible.add(node);
            }
        }
        visibleNodes = visible.toArray(new IContentsNode[0]);
    }

    @Override
    public void resetVisibility() {
        for (IContentsNode node : sortedNodes) {
            node.resetVisibility();
        }
        calcVisibility();
    }

    @Override
    public void setVisible(Set<PageLink> matches) {
        for (IContentsNode node : sortedNodes) {
            node.setVisible(matches);
        }
        calcVisibility();
    }
}
