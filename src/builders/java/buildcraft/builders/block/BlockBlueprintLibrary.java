/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.block;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.builders.BuildCraftBuilders;
import buildcraft.builders.tile.TileBlueprintLibrary;
import buildcraft.core.BCCreativeTab;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.block.BlockBuildCraft;

public class BlockBlueprintLibrary extends BlockBuildCraft {
    public BlockBlueprintLibrary() {
        super(Material.wood, BCCreativeTab.get("main"));
        setRotatable(true);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, EntityPlayer entityplayer, int par6, float par7, float par8, float par9) {
        if (super.onBlockActivated(world, pos, entityplayer, par6, par7, par8, par9)) {
            return true;
        }

        if (entityplayer.isSneaking()) {
            return false;
        }

        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileBlueprintLibrary) {
            if (!world.isRemote) {
                entityplayer.openGui(BuildCraftBuilders.instance, GuiIds.BLUEPRINT_LIBRARY, world, pos);
            }
        }

        return true;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileBlueprintLibrary();
    }
}
