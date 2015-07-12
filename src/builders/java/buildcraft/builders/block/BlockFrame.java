/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumWorldBlockLayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.properties.BuildCraftProperty;
import buildcraft.builders.schematics.SchematicFrame;
import buildcraft.core.lib.block.BlockBuildCraftBase;

public class BlockFrame extends BlockBuildCraftBase {
    private static final Map<EnumFacing[], EFrameConnection> connectionMap = Maps.newHashMap();

    public enum EFrameConnection implements IStringSerializable {
        UP_DOWN(EnumFacing.UP, EnumFacing.DOWN),
        EAST_WEST(EnumFacing.EAST, EnumFacing.WEST),
        NORTH_SOUTH(EnumFacing.NORTH, EnumFacing.SOUTH),

        NORTH_EAST_UP(EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.UP),
        NORTH_EAST_DOWN(EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.DOWN),

        NORTH_WEST_UP(EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.UP),
        NORTH_WEST_DOWN(EnumFacing.NORTH, EnumFacing.WEST, EnumFacing.DOWN),

        SOUTH_EAST_UP(EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.UP),
        SOUTH_EAST_DOWN(EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.DOWN),

        SOUTH_WEST_UP(EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.UP),
        SOUTH_WEST_DOWN(EnumFacing.SOUTH, EnumFacing.WEST, EnumFacing.DOWN);

        final AxisAlignedBB boundingBox;
        private final EnumFacing[] facings;
        private EFrameConnection left;
        private SchematicFrame schematic;

        EFrameConnection(EnumFacing... facings) {
            this.facings = facings;
            AxisAlignedBB bb = new AxisAlignedBB(0.25, 0.25, 0.25, 0.75, 0.75, 0.75);
            for (EnumFacing face : facings) {
                bb = bb.addCoord(face.getFrontOffsetX() * 0.25, face.getFrontOffsetY() * 0.25, face.getFrontOffsetZ() * 0.25);
            }
            boundingBox = bb;
            connectionMap.put(facings, this);
        }

        @Override
        public String getName() {
            return name().toLowerCase(Locale.ROOT);
        }

        public EFrameConnection rotateLeft() {
            if (left != null) {
                return left;
            }
            EnumFacing[] nArray = new EnumFacing[facings.length];
            int i = 0;
            for (EnumFacing face : facings) {
                if (face.getAxis() != Axis.Y) {
                    nArray[i] = face.rotateAround(Axis.Y);
                } else {
                    nArray[i] = face;
                }
                i++;
            }
            left = connectionMap.get(nArray);
            return left;
        }

        public SchematicFrame getSchematic() {
            if (schematic == null) {
                schematic = new SchematicFrame(this);
            }
            return schematic;
        }
    }

    public static final BuildCraftProperty<EFrameConnection> CONNECTIONS = BuildCraftProperty.create("connections", EFrameConnection.class);

    public BlockFrame() {
        super(Material.glass, CONNECTIONS);
        setCreativeTab(null);
        setHardness(0.5F);
        setLightOpacity(0);
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
    public boolean isFullCube() {
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

    @SideOnly(Side.CLIENT)
    public EnumWorldBlockLayer getBlockLayer() {
        return EnumWorldBlockLayer.CUTOUT;
    }
}
