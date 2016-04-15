package buildcraft.lib.guide.parts;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.guide.GuiGuide;

public class GuideImageFactory implements GuidePartFactory<GuideImage> {
    private final ResourceLocation location;
    private final int imageWidth, imageHeight;
    private final int width, height;

    public GuideImageFactory(ResourceLocation location, int imageWidth, int imageHeight, int width, int height) {
        this.location = location;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.width = width;
        this.height = height;
    }

    @Override
    public GuideImage createNew(GuiGuide gui) {
        return new GuideImage(gui, location, imageWidth, imageHeight, width, height);
    }
}
