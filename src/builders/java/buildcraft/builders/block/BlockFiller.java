/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.builders.BuildCraftBuilders;
import buildcraft.builders.tile.TileFiller;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.GuiIds;
import buildcraft.core.block.BlockBuildCraftLED;

public class BlockFiller extends BlockBuildCraftLED {
    public BlockFiller() {
        super(Material.iron);

        setHardness(5F);
        setCreativeTab(BCCreativeTab.get("main"));
        setRotatable(true);
        setPassCount(4);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, pos, entityplayer, par6, par7, par8, par9)) {
            return true;
        }

        if (entityplayer.isSneaking()) {
            return false;
        }

        if (!world.isRemote) {
            entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.FILLER, world, pos);
        }
        return true;

    }

    @Override
    public int getIconGlowLevel(IBlockAccess access, BlockPos pos) {
        if (renderPass == 0 || renderPass == 3) {
            return -1;
        } else {
            TileFiller tile = (TileFiller) access.getTileEntity(pos);
            return tile.getIconGlowLevel(renderPass);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileFiller();
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
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        return 1;
    }

    @Override
    public TextureAtlasSprite getIconAbsolute(IBlockAccess access, BlockPos pos, int side, int meta) {
        if (renderPass < 3) {
            return super.getIconAbsolute(access, pos, side, meta);
        } else {
            if (side == 2) {
                TileEntity tile = access.getTileEntity(pos);
                if (tile instanceof TileFiller && ((TileFiller) tile).currentPattern != null) {
                    return ((TileFiller) tile).currentPattern.getBlockOverlay();
                }
            }
            return null;
        }
    }

    @Override
    public TextureAtlasSprite getIconAbsolute(int side, int meta) {
        if (renderPass < 3) {
            return super.getIconAbsolute(side, meta);
        } else {
            return null;
        }
    }
}
