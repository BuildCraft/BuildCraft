package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.PageLine;
import buildcraft.lib.client.guide.parts.GuideChapterWithin;
import buildcraft.lib.client.guide.parts.GuidePart;
import buildcraft.lib.client.guide.parts.GuideText;
import buildcraft.lib.misc.StringUtilBC;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

import javax.annotation.Nullable;
import java.util.*;

public class ContentsNode implements IContentsNode {

    // public final String title;
    public final String titleKey;
    public final ITextComponent title;
    public final int indent;
    private final Map<String, IContentsNode> nodes = new HashMap<>();
    // private final Map<Object, IContentsNode> nodes = new HashMap<>();
    private IContentsNode[] sortedNodes = new IContentsNode[0];
    IContentsNode[] visibleNodes = new IContentsNode[0];
    private boolean needsSorting = false;

    // public ContentsNode(String title, int indent)
    public ContentsNode(String titleKey, ITextComponent title, int indent) {
        this.titleKey = titleKey;
        this.title = title;
        this.indent = indent;
    }

    @Override
//    public String getSearchName()
    public ITextComponent getSearchName() {
        return title;
    }
    // Calen


    @Override
    public String getKey() {
        return this.titleKey;
    }

    @Override
    public GuidePart createGuidePart(GuiGuide gui) {
        if (indent == 0) {
//            return new GuideChapterWithin(gui, TextFormatting.UNDERLINE + title);
            return new GuideChapterWithin(gui, TextFormatting.UNDERLINE + titleKey, new StringTextComponent(TextFormatting.UNDERLINE.toString()).append(title));
        } else {
//            return new GuideText(gui, new PageLine(indent + 1, TextFormatting.UNDERLINE + title, false));
            return new GuideText(gui, new PageLine(indent + 1, TextFormatting.UNDERLINE + titleKey, new StringTextComponent(TextFormatting.UNDERLINE.toString()).append(title), false));
        }
    }

    @Nullable
    public IContentsNode getChild(String childKey) {
        return nodes.get(childKey);
    }

    @Override
    public void addChild(IContentsNode node) {
//        nodes.put(node.getSearchName(), node);
        nodes.put(node.getKey(), node);
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
//        Arrays.sort(sortedNodes, StringUtilBC.compareBasicReadable(IContentsNode::getSearchName));
        Arrays.sort(sortedNodes, StringUtilBC.compareBasicReadable(IContentsNode::getKey));
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
