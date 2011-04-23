package net.minecraft.src.buildcraft;

import net.minecraft.src.AxisAlignedBB;
import net.minecraft.src.Block;
import net.minecraft.src.Material;
import net.minecraft.src.ModLoader;
import net.minecraft.src.TileEntity;
import net.minecraft.src.mod_BuildCraft;

public class BlockPlainPipe extends Block {
	
//	public int modelID;
	public int texture;
	
	public BlockPlainPipe(int i) {
		super(i, Material.glass);

//		modelID = ModLoader.getUniqueBlockModelID(mod_BuildCraft.getInstance(),
//				true);
		
		texture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/plain_pipe.png");
		
		minX = 0.3;
		minY = 0.0;
		minZ = 0.3;
		
		maxX = 0.7;
		maxY = 1.0;
		maxZ = 0.7;
	}
//	
//    public int getRenderType()
//    {
//        return modelID;
//    }
    
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
    
}
