package buildcraft.lib.guide.parts;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.ResourceLocation;

import buildcraft.lib.gui.GuiIcon;
import buildcraft.lib.guide.GuiGuide;

public class GuideImage extends GuidePart {
    final ResourceLocation location;
    final GuiIcon icon, fullPicture;
    final int imageWidth, imageHeight;
    final int width, height;

    public GuideImage(GuiGuide gui, ResourceLocation boundLocation, int imageWidth, int imageHeight, int width, int height) {
        super(gui);
        this.location = boundLocation;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        int w = width;
        int h = height;
        if (h <= 0) {
            h = imageHeight;
        }
        if (w <= 0) {
            int sf = GuiGuide.PAGE_LEFT_TEXT.width / imageWidth;
            if (sf == 0) {
                int df = 1 + imageWidth / GuiGuide.PAGE_LEFT_TEXT.width;
                w = imageWidth / df;
                h /= df;
            } else {
                w = imageWidth * sf;
                h *= sf;
            }
        }
        int vDiff = 256;
        if (h > GuiGuide.PAGE_LEFT_TEXT.height) {
            h = GuiGuide.PAGE_LEFT_TEXT.height - 10;
            vDiff = (int) (256 * (double) h / (GuiGuide.PAGE_LEFT_TEXT.height - 10));
        }

        this.width = w;
        this.height = h;
        icon = new GuiIcon(location, 0, 0, 256, vDiff);
        fullPicture = new GuiIcon(location, 0, 0, 256, 256);
    }

    @Override
    public PagePart renderIntoArea(int x, int y, int width, int height, PagePart current, int index) {
        if (height - current.line * LINE_HEIGHT < this.height) {
            current = current.nextPage();
        }
        if (index == current.page) {
            icon.drawScaled(x, y + current.line * LINE_HEIGHT, this.width, this.height);
        }
        return current.nextLine(MathHelper.floor_double(this.height / (double) LINE_HEIGHT) + 1, height / LINE_HEIGHT);
    }
}
