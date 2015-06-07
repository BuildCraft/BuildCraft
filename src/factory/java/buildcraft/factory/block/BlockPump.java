/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import buildcraft.api.tools.IToolWrench;
import buildcraft.core.block.BlockBuildCraftLED;
import buildcraft.factory.tile.TilePump;

public class BlockPump extends BlockBuildCraftLED {
    private TextureAtlasSprite[] led;

    public BlockPump() {
        super(Material.iron);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TilePump();
    }

    @Override
    public boolean onBlockActivated(World world, int i, int j, int k, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, i, j, k, entityplayer, par6, par7, par8, par9)) {
            return true;
        }

        TileEntity tile = world.getTileEntity(i, j, k);

        if (tile instanceof TilePump) {
            TilePump pump = (TilePump) tile;

            // Drop through if the player is sneaking
            if (entityplayer.isSneaking()) {
                return false;
            }

            // Restart the quarry if its a wrench
            Item equipped = entityplayer.getCurrentEquippedItem() != null ? entityplayer.getCurrentEquippedItem().getItem() : null;
            if (equipped instanceof IToolWrench && ((IToolWrench) equipped).canWrench(entityplayer, i, j, k)) {

                pump.tank.reset();
                pump.rebuildQueue();
                ((IToolWrench) equipped).wrenchUsed(entityplayer, i, j, k);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onNeighborBlockChange(World world, BlockPos pos, Block block) {
        super.onNeighborBlockChange(world, pos, block);
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TilePump) {
            ((TilePump) tile).onNeighborBlockChange(block);
        }
    }

    @Override
    public int getIconGlowLevel(IBlockAccess access, BlockPos pos) {
        if (renderPass < 1) {
            return -1;
        } else {
            TilePump tile = (TilePump) access.getTileEntity(pos);
            return tile.getIconGlowLevel(renderPass);
        }
    }

    @Override
    public int getLightValue(IBlockAccess world, BlockPos pos) {
        return 1;
    }
}
