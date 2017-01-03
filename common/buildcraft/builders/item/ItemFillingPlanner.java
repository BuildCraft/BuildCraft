package buildcraft.builders.item;

import buildcraft.builders.addon.AddonFillingPlanner;
import buildcraft.core.marker.volume.Addon;
import buildcraft.core.marker.volume.ItemAddon;

public class ItemFillingPlanner extends ItemAddon {
    public ItemFillingPlanner(String id) {
        super(id);
    }

    @Override
    public Addon createAddon() {
        return new AddonFillingPlanner();
    }
}
