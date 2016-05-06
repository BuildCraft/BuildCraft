package buildcraft.lib.item;

import java.util.function.Function;

import net.minecraft.item.ItemMultiTexture;
import net.minecraft.item.ItemStack;

import buildcraft.lib.block.BlockBCBase_Neptune;

/** Basically a copy of {@link ItemMultiTexture}, but extends {@link ItemBuildCraft_BC8} */
public class ItemBuildCraftBlockMulti extends ItemBlockBuildCraft_BC8 {
    protected final Function<ItemStack, String> nameFunction;

    public ItemBuildCraftBlockMulti(BlockBCBase_Neptune block, Function<ItemStack, String> nameFunction) {
        super(block);
        this.nameFunction = nameFunction;
    }

    public ItemBuildCraftBlockMulti(BlockBCBase_Neptune block, final String[] namesByMeta) {
        this(block, stack -> {
            int meta = stack.getMetadata();
            if (meta < 0 || meta >= namesByMeta.length) meta = 0;
            return namesByMeta[meta];
        });
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        return super.getUnlocalizedName() + "." + this.nameFunction.apply(stack);
    }
}
