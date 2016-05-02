package buildcraft.core.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.core.tile.TileMarkerPath;
import buildcraft.lib.block.BlockMarkerBase;

public class BlockMarkerPath extends BlockMarkerBase {
    public BlockMarkerPath(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileMarkerPath();
    }

}
