package buildcraft.lib.guide.parts;

import buildcraft.lib.guide.GuiGuide;
import buildcraft.lib.guide.world.WorldInfo;

public class GuideInWorldFactory implements GuidePartFactory<GuideInWorld> {
    private final WorldInfo info;

    public GuideInWorldFactory(WorldInfo info) {
        this.info = info;
    }

    @Override
    public GuideInWorld createNew(GuiGuide gui) {
        return null;
    }
}
