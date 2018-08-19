package buildcraft.lib.client.guide.parts.contents;

import java.util.Set;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;

public interface IContentsNode {

    String getSearchName();

    boolean isVisible();

    void calcVisibility();

    void resetVisibility();

    /** Should set the visibility to true if this node is contained in the given set. */
    void setVisible(Set<PageLink> matches);

    void sort();

    IContentsNode[] getVisibleChildren();

    void addChild(IContentsNode node);

    GuidePart createGuidePart(GuiGuide gui);
}
