package buildcraft.lib.item;

import net.minecraft.item.ItemBlock;

import buildcraft.lib.block.BlockBuildCraftBase_BC8;

public class ItemBlockBuildCraft_BC8 extends ItemBlock implements IItemBuildCraft {
    public final String id;

    public ItemBlockBuildCraft_BC8(BlockBuildCraftBase_BC8 block) {
        super(block);
        this.id = "item." + block.id;
        init();
    }

    @Override
    public String id() {
        return id;
    }
}
