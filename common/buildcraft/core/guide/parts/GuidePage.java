package buildcraft.core.guide.parts;

import java.util.List;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.gui.FontRenderer;

import buildcraft.core.guide.GuiGuide;

public class GuidePage extends GuidePageBase {
    private final ImmutableList<GuidePart> parts;

    public GuidePage(GuiGuide gui, List<GuidePart> parts) {
        super(gui);
        this.parts = ImmutableList.copyOf(parts);
    }

    @Override
    public void setSpecifics(FontRenderer fontRenderer, int mouseX, int mouseY) {
        super.setSpecifics(fontRenderer, mouseX, mouseY);
        for (GuidePart part : parts) {
            part.setSpecifics(fontRenderer, mouseX, mouseY);
        }
    }

    @Override
    protected void renderPage(int x, int y, int width, int height, int index) {
        super.renderPage(x, y, width, height, index);
        PagePart part = new PagePart(0, 0);
        for (GuidePart guidePart : parts) {
            part = guidePart.renderIntoArea(x, y, width, height, part, index);
            if (numPages != -1 && part.page > index) {
                break;
            }
        }
        if (numPages == -1) {
            numPages = part.newPage().page;
        }
    }

    @Override
    public void handleMouseClick(int x, int y, int width, int height, int mouseX, int mouseY, int mouseButton, int index, boolean isEditing) {
        // TODO Auto-generated method stub
        super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index, isEditing);
    }
}
