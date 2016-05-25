/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockFloodGate extends BlockBuildCraft {
    public BlockFloodGate() {
        super(Material.iron, CONNECTED_DOWN, CONNECTED_EAST, CONNECTED_WEST, CONNECTED_NORTH, CONNECTED_SOUTH);
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        TileEntity tile = access.getTileEntity(pos);

        if (tile instanceof TileFloodGate) {
            TileFloodGate floodGate = (TileFloodGate) tile;
            for (EnumFacing face : EnumFacing.values()) {
                if (face == EnumFacing.UP) continue;
                state = state.withProperty(CONNECTED_MAP.get(face), !floodGate.isSideBlocked(face));
            }
        }
        return state;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileFloodGate();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing side, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, pos, state, entityplayer, side, par7, par8, par9)) {
            return true;
        }

        // Drop through if the player is sneaking
        if (entityplayer.isSneaking()) {
            return false;
        }

        TileEntity tile = world.getTileEntity(pos);

        if (tile instanceof TileFloodGate) {
            TileFloodGate floodGate = (TileFloodGate) tile;
            // Restart the flood gate if it's a wrench
            Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
            if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pos)) {
                if (side == EnumFacing.UP) {
                    floodGate.rebuildQueue();
                } else if (side != null) {
                    floodGate.switchSide(side);
                }

                ((IToolWrench) equipped).wrenchUsed(entityplayer, pos);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, IBlockState state, Block block) {
        super.onNeighborBlockChange(world, pos, state, block);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileFloodGate) {
            ((TileFloodGate) tile).onNeighborBlockChange(block);
        }
    }
}
