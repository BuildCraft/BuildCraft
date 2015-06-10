/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import java.util.ArrayList;

import net.minecraft.block.Block;
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
import buildcraft.core.block.BlockLEDHatchBase;

public class BlockQuarry extends BlockLEDHatchBase {
    public BlockQuarry() {
        super(Material.iron);

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

    public void searchFrames(World world, BlockPos pos) {
        int width2 = 1;
        if (!world.checkChunksExist(i - width2, j - width2, k - width2, i + width2, j + width2, k + width2)) {
            return;
        }

        Block block = world.getBlock(pos);

        if (block != BuildCraftBuilders.frameBlock) {
            return;
        }

        int meta = world.getBlockMetadata(pos);

        if ((meta & 8) == 0) {
            world.setBlockMetadataWithNotify(pos, meta | 8, 0);

            EnumFacing[] dirs = EnumFacing.VALID_DIRECTIONS;

            for (EnumFacing dir : dirs) {
                switch (dir) {
                    case UP:
                        searchFrames(world, i, j + 1, k);
                        break;
                    case DOWN:
                        searchFrames(world, i, j - 1, k);
                        break;
                    case SOUTH:
                        searchFrames(world, pos + 1);
                        break;
                    case NORTH:
                        searchFrames(world, pos - 1);
                        break;
                    case EAST:
                        searchFrames(world, i + 1, j, k);
                        break;
                    case WEST:
                    default:
                        searchFrames(world, i - 1, j, k);
                        break;
                }
            }
        }
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, BlockPos pos, int metadata, int fortune) {
        if (BuildCraftBuilders.quarryOneTimeUse) {
            return new ArrayList<ItemStack>();
        }
        return super.getDrops(world, pos, metadata, fortune);
    }

    @Override
    public void breakBlock(World world, BlockPos pos, Block block, int metadata) {
        if (world.isRemote) {
            return;
        }

        BuildCraftBuilders.frameBlock.removeNeighboringFrames(world, pos);

        super.breakBlock(world, pos, block, metadata);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, pos, entityplayer, par6, par7, par8, par9)) {
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
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isSideSolid(IBlockAccess world, BlockPos pos, EnumFacing side) {
        return false;
    }

    @Override
    public int getIconGlowLevel(IBlockAccess access, BlockPos pos) {
        if (renderPass < 2) {
            return -1;
        } else {
            TileQuarry tile = (TileQuarry) access.getTileEntity(pos);
            return tile.getIconGlowLevel(renderPass);
        }
    }
}
