/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraft.silicon.block;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.mj.ILaserTargetBlock;

import buildcraft.lib.block.BlockBCTile_Neptune;

import buildcraft.silicon.BCSiliconGuis;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileChargingTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileProgrammingTable_Neptune;

public class BlockLaserTable extends BlockBCTile_Neptune implements ILaserTargetBlock {
    private final EnumLaserTableType type;

    public BlockLaserTable(EnumLaserTableType type, Material material, String id) {
        super(material, id);
        this.type = type;
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
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        switch(type) {
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

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return new AxisAlignedBB(0 / 16D, 0 / 16D, 0 / 16D, 16 / 16D, 9 / 16D, 16 / 16D);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
        switch(type) {
            case ASSEMBLY_TABLE:
                if (!world.isRemote) {
                    BCSiliconGuis.ASSEMBLY_TABLE.openGUI(player, pos);
                }
                return true;
            case ADVANCED_CRAFTING_TABLE:
                if (!world.isRemote) {
                    BCSiliconGuis.ADVANCED_CRAFTING_TABLE.openGUI(player, pos);
                }
                return true;
            case INTEGRATION_TABLE:
                if (!world.isRemote) {
                    BCSiliconGuis.INTEGRATION_TABLE.openGUI(player, pos);
                }
                return true;
            case CHARGING_TABLE:
            case PROGRAMMING_TABLE:
        }
        return false;
    }
}
