package buildcraft.lib.guide.parts;

import buildcraft.lib.guide.GuiGuide;
import buildcraft.lib.guide.world.WorldState;

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
