/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.block;

import java.util.Random;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.enums.EnumSpring;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.api.properties.BuildCraftProperty;

import buildcraft.lib.block.BlockBCBase_Neptune;
import buildcraft.lib.misc.data.XorShift128Random;

public class BlockSpring extends BlockBCBase_Neptune {
    public static final BuildCraftProperty<EnumSpring> SPRING_TYPE = BuildCraftProperties.SPRING_TYPE;

    public static final XorShift128Random rand = new XorShift128Random();

    public BlockSpring(String id) {
        super(Material.ROCK, id);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setSoundType(SoundType.STONE);

        disableStats();
        setTickRandomly(true);
        setDefaultState(getDefaultState().withProperty(SPRING_TYPE, EnumSpring.WATER));
    }

    // BlockState

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, SPRING_TYPE);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(SPRING_TYPE).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta == EnumSpring.OIL.ordinal()) {
            return getDefaultState().withProperty(SPRING_TYPE, EnumSpring.OIL);
        } else {
            return getDefaultState().withProperty(SPRING_TYPE, EnumSpring.WATER);
        }
    }

    // Other

    @Override
    public void getSubBlocks(Item itemIn, CreativeTabs tab, NonNullList<ItemStack> list) {
        for (EnumSpring type : EnumSpring.VALUES) {
            list.add(new ItemStack(this, 1, type.ordinal()));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(SPRING_TYPE).ordinal();
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        generateSpringBlock(world, pos, state);
    }

    // @Override
    // public void onNeighborBlockChange(World world, int x, int y, int z, int blockid) {
    // assertSpring(world, x, y, z);
    // }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        world.scheduleUpdate(pos, this, state.getValue(SPRING_TYPE).tickRate);
    }

    private void generateSpringBlock(World world, BlockPos pos, IBlockState state) {
        EnumSpring spring = state.getValue(SPRING_TYPE);
        world.scheduleUpdate(pos, this, spring.tickRate);
        if (!spring.canGen || spring.liquidBlock == null) {
            return;
        }
        if (!world.isAirBlock(pos.up())) {
            return;
        }
        if (spring.chance != -1 && rand.nextInt(spring.chance) != 0) {
            return;
        }
        world.setBlockState(pos.up(), spring.liquidBlock);
    }

    // Prevents updates on chunk generation
    // @Override
    // public boolean func_149698_L() {
    // return false;
    // }
}
