/**
 * Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.silicon.block;

import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.power.ILaserTargetBlock;
import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.silicon.BCSiliconGuis;
import buildcraft.silicon.TileChargingTable;
import buildcraft.silicon.TileProgrammingTable;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockLaserTable extends BlockBCTile_Neptune implements ILaserTargetBlock {
    private final AxisAlignedBB AABB = new AxisAlignedBB(0, 0, 0, 1, 9 / 16D, 1);

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
                return new TileProgrammingTable();
        }
        return null;
    }

    @Override
    public int damageDropped(IBlockState state) {
        return type.ordinal();
    }

    @Override
    public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {
        for(EnumLaserTableType type : EnumLaserTableType.values()) {
            list.add(new ItemStack(item, 1, type.ordinal()));
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABB;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
        switch(type) {
            case ASSEMBLY_TABLE:
                BCSiliconGuis.ASSEMBLY_TABLE.openGUI(player, pos);
                return true;
            case ADVANCED_CRAFTING_TABLE:
                BCSiliconGuis.ADVANCED_CRAFTING_TABLE.openGUI(player, pos);
                return true;
            case INTEGRATION_TABLE:
                BCSiliconGuis.INTEGRATION_TABLE.openGUI(player, pos);
                return true;
            case CHARGING_TABLE:
            case PROGRAMMING_TABLE:
        }
        return false;
    }
}
