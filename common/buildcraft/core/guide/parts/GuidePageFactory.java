package buildcraft.core.guide.parts;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import buildcraft.core.guide.GuiGuide;

public class GuidePageFactory extends GuidePartFactory<GuidePageBase> {
    private final ImmutableList<GuidePartFactory<?>> parts;

    public GuidePageFactory(List<GuidePartFactory<?>> parts) {
        this.parts = ImmutableList.copyOf(parts);
    }

    @Override
    public GuidePage createNew(GuiGuide gui) {
        List<GuidePart> parts = Lists.newArrayList();
        for (GuidePartFactory<?> factory : this.parts) {
            parts.add(factory.createNew(gui));
        }
        return new GuidePage(gui, parts);
    }
}
