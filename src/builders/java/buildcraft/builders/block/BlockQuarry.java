/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.tools.IToolWrench;
import buildcraft.builders.BuildCraftBuilders;
import buildcraft.builders.tile.TileQuarry;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.core.lib.utils.Utils;

public class BlockQuarry extends BlockBuildCraft {
    public BlockQuarry() {
        // Connected_Direction is used for the pipe connected model
        super(Material.iron, FACING_PROP, LED_DONE, LED_POWER, CONNECTED_UP, CONNECTED_DOWN, CONNECTED_EAST, CONNECTED_WEST, CONNECTED_NORTH,
                CONNECTED_SOUTH);

        setHardness(10F);
        setResistance(10F);
        setStepSound(soundTypeAnvil);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving, ItemStack stack) {
        super.onBlockPlacedBy(world, pos, state, entityliving, stack);
        if (entityliving instanceof EntityPlayer) {
            TileEntity tile = world.getTileEntity(pos);
            if (tile instanceof TileQuarry) {
                ((TileQuarry) tile).placedBy = (EntityPlayer) entityliving;
            }
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileQuarry();
    }

    @Override
    public IBlockState getActualState(IBlockState state, IBlockAccess access, BlockPos pos) {
        state = super.getActualState(state, access, pos);
        TileEntity tile = access.getTileEntity(pos);
        if (tile == null | !(tile instanceof TileQuarry)) {
            return state;
        }
        TileQuarry quarry = (TileQuarry) tile;

        for (EnumFacing face : EnumFacing.VALUES) {
            TileEntity other = access.getTileEntity(pos.offset(face));
            boolean hasPipe = Utils.checkPipesConnections(quarry, other);
            state = state.withProperty(CONNECTED_MAP.get(face), hasPipe);
        }

        return state;
    }

    // public void searchFrames(World world, BlockPos pos) {
    // int width2 = 1;
    // int i = pos.getX();
    // int j = pos.getY();
    // int k = pos.getZ();
    // if (!Utils.checkChunksExist(world, i - width2, j - width2, k - width2, i + width2, j + width2, k + width2)) {
    // return;
    // }
    //
    // IBlockState state = world.getBlockState(pos);
    // Block block = state.getBlock();
    //
    // if (block != BuildCraftBuilders.frameBlock) {
    // return;
    // }
    //
    // if ((meta & 8) == 0) {
    // world.setBlockMetadataWithNotify(pos, meta | 8, 0);
    //
    // EnumFacing[] dirs = EnumFacing.VALID_DIRECTIONS;
    //
    // for (EnumFacing dir : dirs) {
    // switch (dir) {
    // case UP:
    // searchFrames(world, i, j + 1, k);
    // break;
    // case DOWN:
    // searchFrames(world, i, j - 1, k);
    // break;
    // case SOUTH:
    // searchFrames(world, pos + 1);
    // break;
    // case NORTH:
    // searchFrames(world, pos - 1);
    // break;
    // case EAST:
    // searchFrames(world, i + 1, j, k);
    // break;
    // case WEST:
    // default:
    // searchFrames(world, i - 1, j, k);
    // break;
    // }
    // }
    // }
    // }

    @Override
    public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
        if (BuildCraftBuilders.quarryOneTimeUse) {
            return new ArrayList<ItemStack>();
        }
        return super.getDrops(world, pos, state, fortune);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        if (world.isRemote) {
            return;
        }

        BuildCraftBuilders.frameBlock.removeNeighboringFrames(world, pos);

        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer entityplayer, EnumFacing facing, float hitX,
            float hitY, float hitZ) {
        if (super.onBlockActivated(world, pos, state, entityplayer, facing, hitX, hitY, hitZ)) {
            return true;
        }

        TileQuarry tile = (TileQuarry) world.getTileEntity(pos);

        // Drop through if the player is sneaking
        if (entityplayer.isSneaking()) {
            return false;
        }

        // Restart the quarry if its a wrench
        Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
        if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, pos)) {

            tile.reinitalize();
            ((IToolWrench) equipped).wrenchUsed(entityplayer, pos);
            return true;

        }

        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }
}
