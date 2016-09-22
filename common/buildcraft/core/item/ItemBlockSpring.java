package buildcraft.core.item;

import buildcraft.core.block.BlockSpring;
import buildcraft.lib.item.ItemBlockBCMulti;

public class ItemBlockSpring extends ItemBlockBCMulti {
    private static final String[] NAMES = { "water", "oil" };

    public ItemBlockSpring(BlockSpring block) {
        super(block, NAMES);
    }

}
