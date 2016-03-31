package buildcraft.transport.transformer;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.BCCreativeTab;

public class BlockTransformer extends Block implements ITileEntityProvider {
    public enum EnumVoltage implements IStringSerializable {
        LOW_MEDIUM,
        MEDIUM_HIGH,
        HIGH_EXTREME;

        public static final EnumVoltage[] VALUES = values();

        @Override
        public String getName() {
            return name();
        }
    }

    public static final PropertyEnum<EnumVoltage> VOLTAGE = PropertyEnum.create("voltage", EnumVoltage.class);
    public static final PropertyEnum<EnumFacing> DIRECTION = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.VALUES);

    public BlockTransformer(Material material) {
        super(material);
        setCreativeTab(BCCreativeTab.get("main"));
        setDefaultState(getDefaultState().withProperty(VOLTAGE, EnumVoltage.LOW_MEDIUM).withProperty(DIRECTION, EnumFacing.EAST));
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileTransformer) {
            return state.withProperty(DIRECTION, ((TileTransformer) tile).getFacing());
        } else {
            return state;
        }
    }

    @Override
    public boolean rotateBlock(World worldIn, BlockPos pos, EnumFacing axis) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileTransformer) {
            ((TileTransformer) tile).rotateFacing(axis);
            return true;
        }
        return false;
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState current = getStateFromMeta(meta);
        EnumFacing player_facing = placer.getHorizontalFacing().getOpposite();
        System.out.println(meta + " -> " + current);
        System.out.println(player_facing.name());
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileTransformer) {
            ((TileTransformer) tile).setFacing(player_facing);
        }

        return current;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (EnumVoltage volt : EnumVoltage.values()) {
            list.add(new ItemStack(item, 1, getMetaFromState(getDefaultState().withProperty(VOLTAGE, volt))));
        }
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, VOLTAGE, DIRECTION);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(VOLTAGE).ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(VOLTAGE, EnumVoltage.VALUES[meta % 3]);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileTransformer(world);
    }
}
