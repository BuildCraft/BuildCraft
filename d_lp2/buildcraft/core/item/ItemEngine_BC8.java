package buildcraft.core.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import buildcraft.lib.engine.BlockEngineBase_BC8;

public class ItemEngine_BC8<E extends Enum<E>> extends ItemBlock {
    private final BlockEngineBase_BC8<E> engineBlock;

    public ItemEngine_BC8(Block block) {
        super(block);
        engineBlock = (BlockEngineBase_BC8<E>) block;
        setHasSubtypes(true);
        setMaxDamage(0);
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        IBlockState state = engineBlock.getStateFromMeta(stack == null ? 0 : stack.getItemDamage());
        E engine = state.getValue(engineBlock.getEngineProperty());
        return "item." + engineBlock.getUnlocalizedName(engine);
    }
}
