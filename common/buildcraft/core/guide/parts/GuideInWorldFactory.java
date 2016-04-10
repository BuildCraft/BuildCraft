package buildcraft.core.guide.parts;

import buildcraft.core.guide.GuiGuide;
import buildcraft.core.guide.world.WorldInfo;

public class GuideInWorldFactory extends GuidePartFactory<GuideInWorld> {
    private final WorldInfo info;

    public GuideInWorldFactory(WorldInfo info) {
        this.info = info;
    }

    @Override
    public GuideInWorld createNew(GuiGuide gui) {
        return null;
    }
}
