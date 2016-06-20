package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.node.NodePageLine;

public class GuideText extends GuidePart {
    private final NodePageLine text;

    public GuideText(GuiGuide gui, NodePageLine text) {
        super(gui);
        this.text = text;
    }

    @Override
    public PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index) {
        return renderLines(text.iterateNonNullLines(), current, x, y, width, height, index);
    }

    @Override
    public PagePart handleMouseClick(int x, int y, int width, int height, PagePart current, int index, int mouseX, int mouseY) {
        return renderLines(text.iterateNonNullLines(), current, x, y, width, height, -1);
    }
}
