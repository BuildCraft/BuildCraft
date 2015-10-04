package buildcraft.core;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;

import buildcraft.api.enums.EnumDecoratedBlock;
import buildcraft.core.lib.block.BlockBuildCraftBase;

public class BlockDecoration extends BlockBuildCraftBase {
    public BlockDecoration() {
        super(Material.iron, DECORATED_TYPE);
        setCreativeTab(null);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (EnumDecoratedBlock type : EnumDecoratedBlock.values()) {
            list.add(new ItemStack(this, 1, type.ordinal()));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return DECORATED_TYPE.getValue(state).ordinal();
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() == this) {
            EnumDecoratedBlock type = DECORATED_TYPE.getValue(state);
            return type.lightValue;
        }
        return super.getLightValue(world, pos);
    }
}
