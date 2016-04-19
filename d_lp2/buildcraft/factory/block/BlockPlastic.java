package buildcraft.factory.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.block.BlockBuildCraftBase_BC8;

public class BlockPlastic extends BlockBuildCraftBase_BC8 {
    public BlockPlastic(String id) {
        super(Material.IRON, id);
        setDefaultState(getStateFromMeta(0));
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, BuildCraftProperties.BLOCK_COLOR);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumDyeColor colour = state.getValue(BuildCraftProperties.BLOCK_COLOR);
        return colour.getMetadata();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(BuildCraftProperties.BLOCK_COLOR, EnumDyeColor.byMetadata(meta));
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (EnumDyeColor dye : EnumDyeColor.values()) {
            list.add(new ItemStack(item, 1, dye.getMetadata()));
        }
    }
}
