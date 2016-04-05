package buildcraft.lib.engine;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockState;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.enums.EnumEnergyStage;
import buildcraft.api.enums.EnumEngineType;
import buildcraft.api.properties.BuildCraftProperties;
import buildcraft.lib.block.BlockBuildCraftTile_BC8;

public abstract class BlockEngineBase_BC8<E extends Enum<E>> extends BlockBuildCraftTile_BC8 {
    private final Map<E, Supplier<? extends TileEngineBase_BC8>> engineTileConstructors = new EnumMap<>(getEngineProperty().getValueClass());

    public BlockEngineBase_BC8(Material material, String id) {
        super(material, id);
        IBlockState defaultState = getDefaultState();
        defaultState = defaultState.withProperty(BuildCraftProperties.ENGINE_TYPE, EnumEngineType.WOOD);
        defaultState = defaultState.withProperty(BuildCraftProperties.ENERGY_STAGE, EnumEnergyStage.BLUE);
        defaultState = defaultState.withProperty(BuildCraftProperties.BLOCK_FACING_6, EnumFacing.UP);
        setDefaultState(defaultState);
    }

    public void registerEngine(E type, Supplier<? extends TileEngineBase_BC8> constructor) {
        engineTileConstructors.put(type, constructor);
    }

    public abstract IProperty<E> getEngineProperty();

    protected abstract E getEngineType(int meta);

    public abstract String getUnlocalizedName(E engine);

    @Override
    protected BlockState createBlockState() {
        return new BlockState(this, getEngineProperty(), BuildCraftProperties.ENERGY_STAGE, BuildCraftProperties.BLOCK_FACING_6);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        E type = state.getValue(getEngineProperty());
        return type.ordinal();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        E engineType = getEngineType(meta);
        return getDefaultState().withProperty(getEngineProperty(), engineType);
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

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        IBlockState state = getStateFromMeta(meta);
        E engineType = state.getValue(getEngineProperty());
        Supplier<? extends TileEntity> constructor = engineTileConstructors.get(engineType);
        if (constructor == null) return null;
        TileEntity tile = constructor.get();
        tile.setWorldObj(world);
        return tile;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for (E engine : getEngineProperty().getAllowedValues()) {
            if (engineTileConstructors.containsKey(engine)) {
                list.add(new ItemStack(item, 1, engine.ordinal()));
            }
        }
    }
}
