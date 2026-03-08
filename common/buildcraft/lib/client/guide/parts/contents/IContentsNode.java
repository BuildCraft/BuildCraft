package buildcraft.lib.client.guide.parts.contents;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.parts.GuidePart;
import net.minecraft.util.text.ITextComponent;

import java.util.Set;

public interface IContentsNode {

    // String getSearchName();
    ITextComponent getSearchName();

    // Calen
    String getKey();

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
