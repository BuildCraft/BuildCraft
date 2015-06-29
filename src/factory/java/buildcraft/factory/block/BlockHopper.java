/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory.block;

import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.IItemPipe;
import buildcraft.core.BuildCraftCore;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.factory.BuildCraftFactory;
import buildcraft.factory.tile.TileHopper;

public class BlockHopper extends BlockBuildCraft {

    private static TextureAtlasSprite icon;

    public BlockHopper() {
        super(Material.iron);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileHopper();
    }

    @Override
    public boolean renderAsNormalBlock() {
        return false;
    }

    @Override
    public boolean isOpaqueCube() {
        return false;
    }

    @Override
    public int getRenderType() {
        return BuildCraftCore.blockByEntityModel;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, pos, entityplayer, par6, par7, par8, par9)) {
            return true;
        }

        if (entityplayer.isSneaking()) {
            return false;
        }

        if (entityplayer.getCurrentEquippedItem() != null) {
            if (entityplayer.getCurrentEquippedItem().getItem() instanceof IItemPipe) {
                return false;
            }
        }

        if (!world.isRemote) {
            entityplayer.openGui(BuildCraftFactory.instance, GuiIds.HOPPER, world, pos);
        }

        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(TextureAtlasSpriteRegister par1IconRegister) {
        icon = par1IconRegister.registerIcon("buildcraftfactory:hopperBlock/bottom");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public TextureAtlasSprite getIconAbsolute(int par1, int par2) {
        return icon;
    }
}
