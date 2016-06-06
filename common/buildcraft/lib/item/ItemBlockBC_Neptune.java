package buildcraft.lib.item;

import net.minecraft.item.ItemBlock;

import buildcraft.lib.block.BlockBCBase_Neptune;

public class ItemBlockBC_Neptune extends ItemBlock implements IItemBuildCraft {
    public final String id;

    public ItemBlockBC_Neptune(BlockBCBase_Neptune block) {
        super(block);
        this.id = "item." + block.id;
        init();
    }

    @Override
    public String id() {
        return id;
    }
}
