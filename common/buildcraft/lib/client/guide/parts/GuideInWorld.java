package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.world.WorldState;

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
