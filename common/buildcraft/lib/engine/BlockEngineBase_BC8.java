package buildcraft.lib.engine;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.properties.BuildCraftProperties;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.misc.EntityUtil;

public abstract class BlockEngineBase_BC8<E extends Enum<E>> extends BlockBCTile_Neptune implements ICustomRotationHandler {
    private final Map<E, Supplier<? extends TileEngineBase_BC8>> engineTileConstructors = new EnumMap<>(getEngineProperty().getValueClass());

    public BlockEngineBase_BC8(Material material, String id) {
        super(material, id);
    }

    // Engine directly related methods

    public void registerEngine(E type, Supplier<? extends TileEngineBase_BC8> constructor) {
        engineTileConstructors.put(type, constructor);
    }

    public abstract IProperty<E> getEngineProperty();

    public abstract E getEngineType(int meta);

    public abstract String getUnlocalizedName(E engine);

    // BlockState

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getEngineProperty(), BuildCraftProperties.ENERGY_STAGE, BuildCraftProperties.BLOCK_FACING_6);
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

    // Misc Block Overrides

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullBlock(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        IBlockState state = getStateFromMeta(meta);
        E engineType = state.getValue(getEngineProperty());
        Supplier<? extends TileEngineBase_BC8> constructor = engineTileConstructors.get(engineType);
        if (constructor == null) {
            return null;
        }
        TileEngineBase_BC8 tile = constructor.get();
        tile.setWorld(world);
        return tile;
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, NonNullList<ItemStack> list) {
        for (E engine : getEngineProperty().getAllowedValues()) {
            if (engineTileConstructors.containsKey(engine)) {
                list.add(new ItemStack(item, 1, engine.ordinal()));
            }
        }
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        if (EntityUtil.getWrenchHand(player) != null) {
            return false;
        }
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
            return engine.onActivated(player, hand, side, hitX, hitY, hitZ);
        }
        return false;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this, 1, state.getValue(getEngineProperty()).ordinal());
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (world.isRemote) return;
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
            engine.rotateIfInvalid();
        }
    }

    // ICustomRotationHandler

    @Override
    public EnumActionResult attemptRotation(World world, BlockPos pos, IBlockState state, EnumFacing sideWrenched) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
            return engine.attemptRotation();
        }
        return EnumActionResult.FAIL;
    }
}
