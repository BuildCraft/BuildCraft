/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.engine;

import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.blocks.ICustomRotationHandler;
import buildcraft.api.core.IEngineType;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.registry.RegistryConfig;
import buildcraft.lib.tile.TileBC_Neptune;

public abstract class BlockEngineBase_BC8<E extends Enum<E> & IEngineType> extends BlockBCTile_Neptune
    implements ICustomRotationHandler {
    private final Map<E, Supplier<? extends TileEngineBase_BC8>> engineTileConstructors =
        new EnumMap<>(getEngineProperty().getValueClass());

    public BlockEngineBase_BC8(Material material, String id) {
        super(material, id);
    }

    // Engine directly related methods

    public void registerEngine(E type, Supplier<? extends TileEngineBase_BC8> constructor) {
        if (RegistryConfig.isEnabled("engines", getRegistryName() + "/" + type.name().toLowerCase(Locale.ROOT),
            getUnlocalizedName(type))) {
            engineTileConstructors.put(type, constructor);
        }
    }

    public boolean isRegistered(E type) {
        return engineTileConstructors.containsKey(type);
    }

    @Nonnull
    public ItemStack getStack(E type) {
        return new ItemStack(this, 1, type.ordinal());
    }

    public abstract IProperty<E> getEngineProperty();

    public abstract E getEngineType(int meta);

    public abstract String getUnlocalizedName(E engine);

    // BlockState

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, getEngineProperty());
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
    public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEngineBase_BC8) {
            TileEngineBase_BC8 engine = (TileEngineBase_BC8) tile;
            return side == engine.currentDirection.getOpposite();
        }
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public TileBC_Neptune createTileEntity(World world, IBlockState state) {
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
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List<ItemStack> list) {
        for (E engine : getEngineProperty().getAllowedValues()) {
            if (engineTileConstructors.containsKey(engine)) {
                list.add(new ItemStack(this, 1, engine.ordinal()));
            }
        }
    }

    @Override
    public int damageDropped(IBlockState state) {
        return state.getValue(getEngineProperty()).ordinal();
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block block) {
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
