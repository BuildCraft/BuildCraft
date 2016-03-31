package buildcraft.transport.ic2;

import java.util.Locale;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import buildcraft.transport.ic2.BlockIc2Transformer.EnumVoltage;

public class ItemIc2Transformer extends ItemBlock {
    public ItemIc2Transformer(Block block) {
        super(block);
        setHasSubtypes(true);
    }

    @Override
    public String getUnlocalizedName(ItemStack stack) {
        int meta = stack.getItemDamage();
        IBlockState state = block.getStateFromMeta(meta);
        EnumVoltage volts = state.getValue(BlockIc2Transformer.VOLTAGE);
        return super.getUnlocalizedName() + "." + volts.getName().toLowerCase(Locale.ROOT);
    }
    
    @Override
    public int getMetadata(int damage) {
        return damage;
    }
}
