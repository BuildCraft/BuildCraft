/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.block;

import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.mj.ILaserTargetBlock;
import buildcraft.api.tiles.IBCTileMenuProvider;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.block.IBlockWithTickableTE;
import buildcraft.lib.misc.MessageUtil;
import buildcraft.lib.tile.TileBC_Neptune;
import buildcraft.silicon.tile.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class BlockLaserTable extends BlockBCTile_Neptune<TileLaserTableBase> implements ILaserTargetBlock, IBlockWithTickableTE<TileLaserTableBase> {
    private final EnumLaserTableType type;

    public BlockLaserTable(String idBC, AbstractBlock.Properties props, EnumLaserTableType type) {
        super(idBC, props);
        this.type = type;
    }

//    @Override
//    public boolean isOpaqueCube(IBlockState state) {
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
//    public boolean isFullCube(BlockState state) {
//        return false;
//    }

//    @Override
//    public BlockRenderLayer getBlockLayer() {
//        return BlockRenderLayer.CUTOUT;
//    }

    @Override
//    public TileBC_Neptune createTileEntity(World world, BlockState state)
    public TileBC_Neptune newBlockEntity(IBlockReader world) {
        switch (type) {
            case ASSEMBLY_TABLE:
                return new TileAssemblyTable();
            case ADVANCED_CRAFTING_TABLE:
                return new TileAdvancedCraftingTable();
            case INTEGRATION_TABLE:
                return new TileIntegrationTable();
            case CHARGING_TABLE:
                return new TileChargingTable();
            case PROGRAMMING_TABLE:
                return new TileProgrammingTable_Neptune();
        }
        return null;
    }

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D);

    @Override
//    public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos)
    public VoxelShape getShape(BlockState state, IBlockReader getter, BlockPos pos, ISelectionContext context) {
        return SHAPE;
    }

    @Override
//    public boolean onBlockActivated(World world, BlockPos pos, BlockState state, PlayerEntity player, Hand hand, Direction side, float hitX, float hitY, float hitZ)
    public ActionResultType use(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hitResult) {
//        switch(type) {
//            case ASSEMBLY_TABLE:
//                if (!world.isRemote) {
//                    BCSiliconGuis.ASSEMBLY_TABLE.openGUI(player, pos);
//                }
//                return true;
//            case ADVANCED_CRAFTING_TABLE:
//                if (!world.isRemote) {
//                    BCSiliconGuis.ADVANCED_CRAFTING_TABLE.openGUI(player, pos);
//                }
//                return true;
//            case INTEGRATION_TABLE:
//                if (!world.isRemote) {
//                    BCSiliconGuis.INTEGRATION_TABLE.openGUI(player, pos);
//                }
//                return true;
//            case CHARGING_TABLE:
//            case PROGRAMMING_TABLE:
//        }
        if (!world.isClientSide) {
            TileEntity te = world.getBlockEntity(pos);
            if (te instanceof IBCTileMenuProvider) {
                IBCTileMenuProvider tile = (IBCTileMenuProvider) te;
//                BCSiliconGuis.ADVANCED_CRAFTING_TABLE.openGUI(player, pos, state);
                MessageUtil.serverOpenTileGui(player, tile, pos);
                return ActionResultType.SUCCESS;
            }
        }
        return ActionResultType.SUCCESS;
    }
}
