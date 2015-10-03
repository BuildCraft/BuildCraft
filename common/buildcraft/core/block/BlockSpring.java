/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.block;

import java.util.List;
import java.util.Random;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

import buildcraft.api.enums.EnumSpring;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.lib.block.BlockBuildCraftBase;
import buildcraft.core.lib.utils.XorShift128Random;

public class BlockSpring extends BlockBuildCraftBase {

    public static final XorShift128Random rand = new XorShift128Random();

    public BlockSpring() {
        super(Material.rock, SPRING_TYPE);
        setBlockUnbreakable();
        setResistance(6000000.0F);
        setStepSound(soundTypeStone);

        disableStats();
        setTickRandomly(true);
        setCreativeTab(BCCreativeTab.get("main"));
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        for (EnumSpring type : EnumSpring.VALUES) {
            list.add(new ItemStack(this, 1, type.ordinal()));
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return SPRING_TYPE.getValue(state).ordinal();
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random random) {
        assertSpring(world, pos, state);
    }

    // @Override
    // public void onNeighborBlockChange(World world, BlockPos pos, int blockid) {
    // assertSpring(world, pos);
    // }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);
        world.scheduleUpdate(pos, this, SPRING_TYPE.getValue(state).tickRate);
    }

    private void assertSpring(World world, BlockPos pos, IBlockState state) {
        EnumSpring spring = (EnumSpring) state.getValue(SPRING_TYPE);
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
