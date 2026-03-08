/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.engine;


import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.core.IEngineType;
import buildcraft.core.BCCoreBlocks;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.tile.TileBC_Neptune;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public abstract class BlockEngineBase_BC8<E extends Enum<E> & IEngineType> extends BlockBCTile_Neptune<TileEngineBase_BC8> implements ICustomRotationHandler, IBlockWithTickableTE<TileEngineBase_BC8> {
    public final E engineType;

    // Calen: moved to BCCoreBlocks.engineTileConstructors
//    private final Map<E, Supplier<? extends TileEngineBase_BC8>> engineTileConstructors = new EnumMap<>(getEngineProperty().getValueClass());

    public BlockEngineBase_BC8(String idBC, AbstractBlock.Properties props, E type) {
        super(idBC, props);
        this.engineType = type;
    }

    // Engine directly related methods

//    public void registerEngine(E type, Supplier<? extends TileEngineBase_BC8> constructor) {
//        if (RegistryConfig.isEnabled("engines", getRegistryName() + "/" + type.name().toLowerCase(Locale.ROOT), getUnlocalizedName(type))) {
//            engineTileConstructors.put(type, constructor);
//        }
//    }

//    public boolean isRegistered(E type) {
//        return engineTileConstructors.containsKey(type);
//    }

    @Nonnull
//    public ItemStack getStack(E type)
    public ItemStack getStack() {
//        return new ItemStack(this, 1, type.ordinal());
        return new ItemStack(this, 1);
    }

//    public abstract Property<E> getEngineProperty();

//    public abstract EnumEngineType getEngineType(int meta);

//    public abstract String getUnlocalizedName();

    // BlockState

//    @Override
////    protected BlockStateContainer createBlockState()
//    protected void createBlockStateDefinition(@Nonnull StateContainer.IBuilder<Block, BlockState> builder) {
////        super.createBlockStateDefinition(builder);
////        builder.add(getEngineProperty());
//    }

//    @Override
//    public int getMetaFromState(BlockState state) {
//        E type = state.getValue(getEngineProperty());
//        return type.ordinal();
//    }

//    @Override
//    public BlockState getStateFromMeta(int meta) {
//        E engineType = getEngineType(meta);
//        return this.defaultBlockState().setValue(getEngineProperty(), engineType);
//    }

    // Misc Block Overrides

//    @Override
//    public boolean isOpaqueCube(BlockState state) {
//        return false;
//    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, IBlockReader world, BlockPos pos) {
        return true;
    }

    @Override
    public float getShadeBrightness(BlockState state, IBlockReader world, BlockPos pos) {
        return 1.0F;
    }

//    @Override
//    public boolean isFullBlock(BlockState state) {
//        return false;
//    }

//    @Override
//    public boolean isFullCube(BlockState state) {
//        return false;
//    }

//    @Override
//    public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing side) {
//        TileEntity tile = world.getTileEntity(pos);
//        if (tile instanceof TileEngineBase_BC8) {
//            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
//            if (side == engine.currentDirection.getOpposite()) {
//                return BlockFaceShape.SOLID;
//            } else {
//                return BlockFaceShape.UNDEFINED;
//            }
//        }
//        return BlockFaceShape.UNDEFINED;
//    }

//    @Override
//    public boolean isSideSolid(BlockState base_state, IWorld world, BlockPos pos, Direction side) {
//        TileEntity tile = world.getBlockEntity(pos);
//        if (tile instanceof TileEngineBase_BC8) {
//            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
//            return side == engine.currentDirection.getOpposite();
//        }
//        return false;
//    }

    @Override
//    public EnumBlockRenderType getRenderType(BlockState state)
    public BlockRenderType getRenderShape(BlockState state) {
//        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
        return BlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    // Calen
    private static final VoxelShape BASE_U = Block.box(0, 0, 0, 16, 4, 16);
    private static final VoxelShape TRUNK_U = Block.box(4, 4, 4, 12, 16, 12);
    private static final VoxelShape UP = VoxelShapes.or(BASE_U, TRUNK_U);
    private static final VoxelShape BASE_D = Block.box(0, 12, 0, 16, 16, 16);
    private static final VoxelShape TRUNK_D = Block.box(4, 0, 4, 12, 12, 12);
    private static final VoxelShape DOWN = VoxelShapes.or(BASE_D, TRUNK_D);
    private static final VoxelShape BASE_E = Block.box(0, 0, 0, 4, 16, 16);
    private static final VoxelShape TRUNK_E = Block.box(4, 4, 4, 16, 12, 12);
    private static final VoxelShape EAST = VoxelShapes.or(BASE_E, TRUNK_E);
    private static final VoxelShape BASE_W = Block.box(12, 0, 0, 16, 16, 16);
    private static final VoxelShape TRUNK_W = Block.box(0, 4, 4, 12, 12, 12);
    private static final VoxelShape WEST = VoxelShapes.or(BASE_W, TRUNK_W);
    private static final VoxelShape BASE_N = Block.box(0, 0, 12, 16, 16, 16);
    private static final VoxelShape TRUNK_N = Block.box(4, 4, 0, 12, 12, 12);
    private static final VoxelShape NORTH = VoxelShapes.or(BASE_N, TRUNK_N);
    private static final VoxelShape BASE_S = Block.box(0, 0, 0, 16, 16, 4);
    private static final VoxelShape TRUNK_S = Block.box(4, 4, 4, 12, 12, 16);
    private static final VoxelShape SOUTH = VoxelShapes.or(BASE_S, TRUNK_S);

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
        TileEntity te = world.getBlockEntity(pos);
        if (te instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) te;
            switch (engine.currentDirection) {
                case DOWN:
                    return DOWN;
                case UP:
                    return UP;
                case WEST:
                    return WEST;
                case EAST:
                    return EAST;
                case SOUTH:
                    return SOUTH;
                case NORTH:
                    return NORTH;
            }
        }
        return VoxelShapes.block();
    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, BlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
//        E engineType = state.getValue(getEngineProperty());
        E engineType = this.engineType;
//        BiFunction<BlockPos, BlockState, ? extends TileEngineBase_BC8> constructor = engineTileConstructors.get(engineType);
        Supplier<? extends TileEngineBase_BC8> constructor = BCCoreBlocks.engineTileConstructors.get(engineType);
        if (constructor == null) {
            return null;
        }
        TileEngineBase_BC8 tile = constructor.get();
//        tile.setWorld(world);
        return tile;
    }

//    @Override
//    public void getSubBlocks(ItemGroup tab, NonNullList<ItemStack> list) {
//        for (E engine : getEngineProperty().getAllowedValues()) {
//            if (engineTileConstructors.containsKey(engine)) {
//                list.add(new ItemStack(this, 1, engine.ordinal()));
//            }
//        }
//    }

    // Calen: use datagen LootTable
//    @Override
//    public int damageDropped(BlockState state) {
//        return state.getValue(getEngineProperty()).ordinal();
//    }

    @Override
    public void neighborChanged(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean p_60514_) {
        super.neighborChanged(state, world, pos, block, fromPos, p_60514_);
        if (world.isClientSide) return;
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
            engine.rotateIfInvalid();
        }
    }

    // ICustomRotationHandler

    @Override
    public ActionResultType attemptRotation(World world, BlockPos pos, BlockState state, Direction sideWrenched) {
        TileEntity tile = world.getBlockEntity(pos);
        if (tile instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
            return engine.attemptRotation();
        }
        return ActionResultType.FAIL;
    }
}
