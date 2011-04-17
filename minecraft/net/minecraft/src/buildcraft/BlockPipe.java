package net.minecraft.src.buildcraft;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.World;
import net.minecraft.src.mod_BuildCraft;

public class BlockPipe extends BlockContainer {
	
	public int modelID;
	public int texture;
	
	public BlockPipe(int i, int j) {
		super(i, j, Material.ground);

		modelID = ModLoader.getUniqueBlockModelID(mod_BuildCraft.getInstance(),
				true);
		
		texture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/pipe.png");
	}
	
    public int getRenderType()
    {
        return modelID;
    }
    
    public boolean isOpaqueCube()
    {
        return false;
    }
    
    public boolean renderAsNormalBlock()
    {
        return false;
    }

    public int getBlockTextureFromSide(int i) {
    	return texture;
    }
    
    @Override
    public void onBlockPlaced(World world, int i, int j, int k, int l)
    {
		TilePipe tile = new TilePipe (i, j, k);
		world.setBlockTileEntity(i, j, k, tile);
    }

	@Override
	protected TileEntity getBlockEntity() {
		return new TilePipe ();
	}

}
