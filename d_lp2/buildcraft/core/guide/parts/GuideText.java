package buildcraft.core.guide.parts;

import buildcraft.core.guide.GuiGuide;
import buildcraft.core.guide.node.NodePageLine;

public class GuideText extends GuidePart {
    private final NodePageLine text;

    public GuideText(GuiGuide gui, NodePageLine text) {
        super(gui);
        this.text = text;
    }

    @Override
    public PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index) {
        return renderLines(text.iterateOnlyExpandedLines(), current, x, y, width, height, index);
    }
}
