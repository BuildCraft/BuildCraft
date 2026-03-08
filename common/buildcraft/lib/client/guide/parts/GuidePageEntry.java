package buildcraft.lib.client.guide.parts;

import buildcraft.lib.client.guide.GuiGuide;
import buildcraft.lib.client.guide.GuideManager;
import buildcraft.lib.client.guide.entry.PageEntry;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;

public class GuidePageEntry extends GuidePage {

    public final ResourceLocation name;

    public GuidePageEntry(GuiGuide gui, List<GuidePart> parts, PageEntry<?> entry, ResourceLocation name) {
        super(gui, parts, entry);
        this.name = name;
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
