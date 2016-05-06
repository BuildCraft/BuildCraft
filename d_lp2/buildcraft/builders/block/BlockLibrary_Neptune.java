package buildcraft.builders.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.lib.block.BlockBCTile_Neptune;

public class BlockLibrary_Neptune extends BlockBCTile_Neptune {
    public BlockLibrary_Neptune(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return null;
    }
}
