package buildcraft.energy.block;

import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import buildcraft.energy.tile.TileEngineStone_BC8;
import buildcraft.lib.block.BlockBuildCraftTile_BC8;
import buildcraft.lib.engine.BlockEngineBase_BC8;

public class BlockEngineStone extends BlockEngineBase_BC8  {
    public BlockEngineStone(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        TileEngineStone_BC8 tile = new TileEngineStone_BC8();
        tile.setWorldObj(world);
        return tile;
    }
}
