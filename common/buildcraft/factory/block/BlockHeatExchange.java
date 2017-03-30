package buildcraft.factory.block;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.lib.block.BlockBCTile_Neptune;

public class BlockHeatExchange extends BlockBCTile_Neptune {
    public enum Part {
        START(PROP_FACING),
        END(PROP_FACING),
        MIDDLE(PROP_AXIS) {
            @Override
            int getMeta(IBlockState state) {
                Axis axis = state.getValue(PROP_AXIS);
                return axis == Axis.X ? 0 : 1;
            }

            @Override
            IBlockState getState(IBlockState defaultState, int meta) {
                return defaultState.withProperty(PROP_AXIS, (meta & 1) == 0 ? Axis.X : Axis.Z);
            }

            @Override
            IBlockState getPlacement(IBlockState state, EnumFacing playerFacing) {
                return state.withProperty(PROP_AXIS, playerFacing.rotateY().getAxis());
            }

            @Override
            Axis getAxis(IBlockState state) {
                return state.getValue(PROP_AXIS);
            }
        };

        final IProperty<?> rotateProp;

        private Part(IProperty<?> rotateProp) {
            this.rotateProp = rotateProp;
        }

        int getMeta(IBlockState state) {
            EnumFacing face = state.getValue(PROP_FACING);
            return face.getHorizontalIndex();
        }

        IBlockState getState(IBlockState defaultState, int meta) {
            return defaultState.withProperty(PROP_FACING, EnumFacing.getHorizontal(meta & 3));
        }

        IBlockState getPlacement(IBlockState state, EnumFacing playerFacing) {
            EnumFacing face = playerFacing;
            if (this == END) face = face.getOpposite();
            return state.withProperty(PROP_FACING, face.rotateY());
        }

        /** @return The axis That fluids flow through (horizontally) */
        Axis getAxis(IBlockState state) {
            return state.getValue(PROP_FACING).getAxis();
        }
    }

    public static final IProperty<Axis> PROP_AXIS = PropertyEnum.create("axis", Axis.class, Axis.X, Axis.Z);

    private static Part currentInitPart = null;
    public final Part part;

    public BlockHeatExchange(Material material, String id, Part part) {
        // Java doesn't allow setting static variables here, so call a static method that does it inside super()
        super(material, setCurrentPart(id, part));
        currentInitPart = null;
        this.part = part;
    }

    private static String setCurrentPart(String id, Part part) {
        currentInitPart = part;
        return id;
    }

    @Override
    protected void addProperties(List<IProperty<?>> properties) {
        // This is called BEFORE part is set, so we have to use a static property instead
        properties.add(currentInitPart.rotateProp);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return part.getMeta(state);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return part.getState(getDefaultState(), meta);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return part.getPlacement(getDefaultState(), placer.getHorizontalFacing());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return null;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }
}
