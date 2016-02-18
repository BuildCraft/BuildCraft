package buildcraft.factory.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.core.lib.block.BlockBuildCraft;
import buildcraft.factory.tile.TileDistiller;

public class BlockDistiller extends BlockBuildCraft {
    public BlockDistiller() {
        super(Material.iron);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileDistiller();
    }
}
