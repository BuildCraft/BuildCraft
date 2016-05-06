package buildcraft.lib.item;

import net.minecraft.item.ItemBlock;

import buildcraft.lib.block.BlockBCBase_Neptune;

public class ItemBlockBuildCraft_BC8 extends ItemBlock implements IItemBuildCraft {
    public final String id;

    public ItemBlockBuildCraft_BC8(BlockBCBase_Neptune block) {
        super(block);
        this.id = "item." + block.id;
        init();
    }

    @Override
    public String id() {
        return id;
    }
}
