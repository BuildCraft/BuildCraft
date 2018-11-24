package buildcraft.lib.client.guide.parts;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.util.ResourceLocation;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.entry.PageEntry;

public class GuidePageEntry extends GuidePage {

    public final ResourceLocation name;
    public final PageEntry<?> entry;

    public GuidePageEntry(GuiGuide gui, List<GuidePart> parts, ResourceLocation name, PageEntry<?> entry) {
        super(gui, parts, entry.title.getFormattedText());
        this.name = name;
        this.entry = entry;
    }

    @Override
    @Nullable
    public GuidePageBase createReloaded() {
        GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(name);
        if (factory == null) {
            return null;
        }
        GuidePageBase page = factory.createNew(gui);
        page.goToPage(getPage());
        return page;
    }
}
