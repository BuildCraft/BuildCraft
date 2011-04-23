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
		
		texture = ModLoader.addOverride("/terrain.png",
		"/buildcraft_gui/plain_pipe.png");
		
		minX = Utils.pipeMinSize;
		minY = 0.0;
		minZ = Utils.pipeMinSize;
		
		maxX = Utils.pipeMaxSize;
		maxY = 1.0;
		maxZ = Utils.pipeMaxSize;
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
    
}
