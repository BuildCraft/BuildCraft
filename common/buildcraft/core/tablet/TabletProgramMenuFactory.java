package buildcraft.core.tablet;

import buildcraft.api.tablet.ITablet;
import buildcraft.api.tablet.TabletBitmap;
import buildcraft.api.tablet.TabletProgram;
import buildcraft.api.tablet.TabletProgramFactory;

public class TabletProgramMenuFactory extends TabletProgramFactory {
    @Override
    public TabletProgram create(ITablet tablet) {
        return new TabletProgramMenu(tablet);
    }

    @Override
    public String getName() {
        return "menu";
    }

    @Override
    public TabletBitmap getIcon() {
        // TODO
        return null;
    }
}
