package buildcraft.core.guide.parts;

import buildcraft.core.guide.GuiGuide;
import buildcraft.core.guide.world.WorldState;

public class GuideInWorld extends GuidePart {
    private boolean fullscreen = false;
    private final WorldState state;

    public GuideInWorld(GuiGuide gui, boolean fullscreen, WorldState state) {
        super(gui);
        this.fullscreen = fullscreen;
        this.state = state;
    }

    @Override
    public PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index) {
        // TODO Auto-generated method stub
        return null;
    }
}
