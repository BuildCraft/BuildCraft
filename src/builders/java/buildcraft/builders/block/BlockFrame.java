/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import java.util.List;
import java.util.Locale;
import java.util.Random;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.core.lib.block.BlockBuildCraftBase;

public class BlockFrame extends BlockBuildCraftBase {

    public enum EFrameConnection implements IStringSerializable {
        UP_DOWN(new AxisAlignedBB(0.25, 0, 0.25, 0.75, 1, 0.75)),
        EAST_WEST(new AxisAlignedBB(0, 0.25, 0.25, 1, 0.75, 0.75)),
        NORTH_SOUTH(new AxisAlignedBB(0.25, 0.25, 0, 0.75, 0.75, 1)),

        NORTH_EAST_UP(new AxisAlignedBB(0.25, 0.25, 0.25, 1, 1, 1)),
        NORTH_EAST_DOWN(new AxisAlignedBB(0.25, 0, 0.25, 1, 0.75, 1)),

        NORTH_WEST_UP(new AxisAlignedBB(0, 0.25, 0.25, 0.75, 1, 1)),
        NORTH_WEST_DOWN(new AxisAlignedBB(0, 0, 0.25, 0.75, 0.75, 1)),

        SOUTH_EAST_UP(new AxisAlignedBB(0.25, 0.25, 0.25, 1, 1, 1)),
        SOUTH_EAST_DOWN(new AxisAlignedBB(0.25, 0, 0.25, 1, 0.75, 1)),

        SOUTH_WEST_UP(new AxisAlignedBB(0, 0.25, 0.25, 0.75, 1, 1)),
        SOUTH_WEST_DOWN(new AxisAlignedBB(0, 0, 0.25, 0.75, 0.75, 1));

        final AxisAlignedBB boundingBox;

        EFrameConnection(AxisAlignedBB bb) {
            boundingBox = bb;
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final BuildCraftProperty<EFrameConnection> CONNECTIONS = BuildCraftProperty.create("connections", EFrameConnection.class);

    public BlockFrame() {
        super(Material.glass, CONNECTIONS);
        setHardness(0.5F);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            return;
        } else {
            removeNeighboringFrames(world, pos);
        }
    }

    public void removeNeighboringFrames(World world, BlockPos pos) {
        for (EnumFacing dir : EnumFacing.VALUES) {
            BlockPos nPos = pos.offset(dir);
            Block nBlock = world.getBlockState(nPos).getBlock();
            if (nBlock == this) {
                world.setBlockToAir(nPos);
            }
        }
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return 3;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random random, int fortune) {
        return null;
    }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        return Lists.newArrayList();
    }

    @Override
    public AxisAlignedBB getBox(IBlockAccess world, BlockPos pos, IBlockState state) {
        return CONNECTIONS.getValue(state).boundingBox;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List list) {
        list.add(new ItemStack(this));
    }
    
    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumFacing face, float par7, float par8,
            float par9) {
        world.setBlockState(pos, state.cycleProperty(CONNECTIONS));
        return true;
    }

}
