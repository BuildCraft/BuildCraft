package buildcraft.lib.client.guide.parts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.font.IFontRenderer;
import buildcraft.lib.client.resource.GuidePartChapter;
import buildcraft.lib.client.resource.MarkdownResourceHolder;

public class GuidePage extends GuidePageBase {
    @Nullable
    public final MarkdownResourceHolder creator;
    public final ImmutableList<GuidePart> parts;
    public final String title;

    public GuidePage(GuiGuide gui, List<GuidePart> parts, MarkdownResourceHolder creator, String title) {
        super(gui);
        this.creator = creator;
        this.parts = ImmutableList.copyOf(parts);
        this.title = title;
    }

    @Override
    public List<GuidePartChapter> getChapters() {
        List<GuidePartChapter> list = new ArrayList<>();
        for (GuidePart part : parts) {
            if (part instanceof GuidePartChapter) {
                list.add((GuidePartChapter) part);
            }
        }
        return list;

    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        for (GuidePart part : parts) {
            part.setFontRenderer(fontRenderer);
        }
    }

    @Override
    protected void renderPage(int x, int y, int width, int height, int index) {
        super.renderPage(x, y, width, height, index);
        PagePosition part = new PagePosition(0, 0);
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
        super.handleMouseClick(x, y, width, height, mouseX, mouseY, mouseButton, index, isEditing);

        PagePosition part = new PagePosition(0, 0);
        for (GuidePart guidePart : parts) {
            part = guidePart.handleMouseClick(x, y, width, height, part, index, mouseX, mouseY);

            if (numPages != -1 && part.page > index) {
                break;
            }
        }
    }
}
