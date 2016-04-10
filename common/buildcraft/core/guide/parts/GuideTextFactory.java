package buildcraft.core.guide.parts;

import buildcraft.core.guide.GuiGuide;
import buildcraft.core.guide.node.NodePageLine;

public class GuideTextFactory extends GuidePartFactory<GuideText> {
    private final NodePageLine text;

    public GuideTextFactory(NodePageLine text) {
        this.text = text;
    }

    @Override
    public GuideText createNew(GuiGuide gui) {
        return new GuideText(gui, text);
    }
}
