/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.factory.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.EnumFacing.AxisDirection;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.pipe.ICustomPipeConnection;

import buildcraft.lib.block.BlockBCTile_Neptune;

import buildcraft.factory.tile.TileHeatExchangeEnd;
import buildcraft.factory.tile.TileHeatExchangeStart;

public class BlockHeatExchange extends BlockBCTile_Neptune implements ICustomPipeConnection {
    public enum Part {
        START,
        END,
        MIDDLE {
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
            public Axis getAxis(IBlockState state) {
                return state.getValue(PROP_AXIS);
            }

            @Override
            void addProperties(List<IProperty<?>> properties) {
                properties.add(PROP_AXIS);
                properties.add(PROP_CONNECTED_NEG);
                properties.add(PROP_CONNECTED_POS);
            }

            @Override
            IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
                Axis axis = getAxis(state);
                EnumFacing facePos = EnumFacing.getFacingFromAxis(AxisDirection.POSITIVE, axis);
                state = state.withProperty(PROP_CONNECTED_POS, doesNeighbourConnect(world, pos, facePos));
                EnumFacing faceNeg = EnumFacing.getFacingFromAxis(AxisDirection.NEGATIVE, axis);
                state = state.withProperty(PROP_CONNECTED_NEG, doesNeighbourConnect(world, pos, faceNeg));
                return state;
            }

            @Override
            boolean doesConnect(IBlockState state, EnumFacing from) {
                return from.getAxis() == state.getValue(PROP_AXIS);
            }
        };

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
        public Axis getAxis(IBlockState state) {
            return state.getValue(PROP_FACING).getAxis();
        }

        void addProperties(List<IProperty<?>> properties) {
            properties.add(PROP_FACING);
            properties.add(PROP_CONNECTED);
            properties.add(PROP_CONNECTED_Y);
        }

        IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
            EnumFacing facing = state.getValue(PROP_FACING);
            boolean connected = doesNeighbourConnect(world, pos, facing);
            state = state.withProperty(PROP_CONNECTED, connected);

            // EnumFacing yFace = this == Part.START ? EnumFacing.DOWN : EnumFacing.UP;
            // TODO: test the above connection (use the tile?)
            state = state.withProperty(PROP_CONNECTED_Y, Boolean.FALSE);
            return state;
        }

        boolean doesConnect(IBlockState state, EnumFacing from) {
            EnumFacing thisFacing = state.getValue(PROP_FACING);
            return from == thisFacing;
        }

        static boolean doesNeighbourConnect(IBlockAccess world, BlockPos thisPos, EnumFacing dir) {
            IBlockState offset = world.getBlockState(thisPos.offset(dir));
            Block block = offset.getBlock();
            if (block instanceof BlockHeatExchange) {
                Part part = ((BlockHeatExchange) block).part;
                return part.doesConnect(offset, dir.getOpposite());
            }
            return false;
        }
    }

    public static final IProperty<Axis> PROP_AXIS = PropertyEnum.create("axis", Axis.class, Axis.X, Axis.Z);
    public static final IProperty<Boolean> PROP_CONNECTED = PropertyBool.create("connected");
    public static final IProperty<Boolean> PROP_CONNECTED_Y = PropertyBool.create("connected_y");
    public static final IProperty<Boolean> PROP_CONNECTED_POS = PropertyBool.create("connected_pos");
    public static final IProperty<Boolean> PROP_CONNECTED_NEG = PropertyBool.create("connected_neg");

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
        currentInitPart.addProperties(properties);
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
    public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
        return part.getActualState(state, world, pos);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return part.getPlacement(getDefaultState(), placer.getHorizontalFacing());
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        switch (part) {
            case START:
                return new TileHeatExchangeStart();
            case MIDDLE:
                return null;
            case END:
                return new TileHeatExchangeEnd();
            default:
                throw new IllegalStateException("Unknown part " + part);
        }
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

    @Override
    public float getExtension(World world, BlockPos pos, EnumFacing face, IBlockState state) {
        if (part == Part.MIDDLE) {
            return 0;
        }
        EnumFacing thisFacing = state.getValue(PROP_FACING);
        if (face != thisFacing.getOpposite()) {
            return 0;
        }
        return 2 / 16.0f;
    }
}
