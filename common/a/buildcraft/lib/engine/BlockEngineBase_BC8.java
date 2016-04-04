package a.buildcraft.lib.engine;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;

import a.buildcraft.lib.block.BlockBuildCraftTile_BC8;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.properties.BuildCraftProperties;

public abstract class BlockEngineBase_BC8 extends BlockBuildCraftTile_BC8 {
    public BlockEngineBase_BC8(Material material, String id) {
        super(material, id);
        IBlockState defaultState = getDefaultState();
        defaultState = defaultState.withProperty(BuildCraftProperties.ENGINE_TYPE, EnumEngineType.WOOD);
        defaultState = defaultState.withProperty(BuildCraftProperties.ENERGY_STAGE, EnumEnergyStage.BLUE);
        defaultState = defaultState.withProperty(BuildCraftProperties.BLOCK_FACING_6, EnumFacing.UP);
        setDefaultState(defaultState);
    }

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, BuildCraftProperties.ENGINE_TYPE, BuildCraftProperties.ENERGY_STAGE, BuildCraftProperties.BLOCK_FACING_6);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumEngineType face = state.getValue(BuildCraftProperties.ENGINE_TYPE);
        return face.ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumEngineType engineType = EnumEngineType.fromMeta(meta);
        return getDefaultState().withProperty(BuildCraftProperties.ENGINE_TYPE, engineType);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
            state = state.withProperty(BuildCraftProperties.ENERGY_STAGE, engine.getEnergyStage());
            state = state.withProperty(BuildCraftProperties.BLOCK_FACING_6, engine.getCurrentDirection());
        }
        return state;
    }
}
