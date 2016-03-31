package buildcraft.transport.ic2;

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
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.core.BCCreativeTab;

public class BlockIc2Transformer extends Block implements ITileEntityProvider {
    public enum EnumVoltage implements IStringSerializable {
        LOW_MEDIUM,
        MEDIUM_HIGH,
        HIGH_EXTREME;

        private static final EnumVoltage[] VALUES = values();

        @Override
        public String getName() {
            return name();
        }
    }

    public static final PropertyEnum<EnumVoltage> VOLTAGE = PropertyEnum.create("voltage", EnumVoltage.class);
    public static final PropertyEnum<EnumFacing> DIRECTION = PropertyEnum.create("facing", EnumFacing.class, EnumFacing.HORIZONTALS);

    public BlockIc2Transformer(Material material) {
        super(material);
        setCreativeTab(BCCreativeTab.get("main"));
        setDefaultState(getDefaultState().withProperty(VOLTAGE, EnumVoltage.LOW_MEDIUM).withProperty(DIRECTION, EnumFacing.EAST));

        for (EnumVoltage v : EnumVoltage.VALUES) {
            for (EnumFacing f : EnumFacing.HORIZONTALS) {
                IBlockState state = getDefaultState().withProperty(VOLTAGE, v).withProperty(DIRECTION, f);
                int meta = getMetaFromState(state);
                System.out.println(meta + " = " + state);
                IBlockState other = getStateFromMeta(meta);
                System.out.println("  -> " + other);
            }
        }
    }

    @Override
    public IBlockState onBlockPlaced(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        IBlockState current = getStateFromMeta(meta);
        EnumFacing player_facing = placer.getHorizontalFacing();
        System.out.println(meta + " -> " + current);
        return current.withProperty(DIRECTION, player_facing);
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
        int v = (state.getValue(VOLTAGE).ordinal() << 2);
        int f = state.getValue(DIRECTION).getHorizontalIndex();
        return v + f;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumVoltage v = EnumVoltage.VALUES[(meta >> 2) & 3];
        EnumFacing f = EnumFacing.getHorizontal(meta & 3);
        return getDefaultState().withProperty(VOLTAGE, v).withProperty(DIRECTION, f);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        IBlockState state = getStateFromMeta(meta);
        EnumVoltage volts = state.getValue(VOLTAGE);
        return new TileIc2Transformer(world, volts);
    }
}
