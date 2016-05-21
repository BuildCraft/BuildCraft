package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.world.WorldInfo;

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
