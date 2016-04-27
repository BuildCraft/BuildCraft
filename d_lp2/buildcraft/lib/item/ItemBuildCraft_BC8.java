package buildcraft.lib.item;

import net.minecraft.item.Item;

import buildcraft.lib.TagManager;

public class ItemBuildCraft_BC8 extends Item implements IItemBuildCraft {
    /** The tag used to identify this in the {@link TagManager} */
    public final String id;

    public ItemBuildCraft_BC8(String id) {
        this.id = id;
        init();
    }

    @Override
    public String id() {
        return id;
    }
}
