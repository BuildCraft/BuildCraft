package info.jbcs.minecraft.chisel.api;

import net.minecraft.block.Block;
import net.minecraft.world.IBlockAccess;

public interface IFacade {

  int getFacadeMetadata(IBlockAccess world, int x, int y, int z, int side);
  
  Block getFacade(IBlockAccess world, int x, int y, int z, int side);
    
}
