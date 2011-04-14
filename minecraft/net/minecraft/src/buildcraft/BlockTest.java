package net.minecraft.src.buildcraft;

import java.util.Random;

import net.minecraft.src.Block;
import net.minecraft.src.EntityPlayer;
import net.minecraft.src.Material;
import net.minecraft.src.World;

public class BlockTest extends Block {

	public BlockTest(int i, int j) {
		super(i, j, Material.rock);
		
		setTickOnLoad(true);
	}
	
	
	
	 public void onBlockClicked(World world, int i, int j, int k, EntityPlayer entityplayer)
	   {
	      
//	      for (int ind = 0; ind <= 10; ++ind) {
//	         world.setBlockWithNotify(i, j, k + ind, Block.wood.blockID);
//	      }
	      
	   }
	 
	 private int growX, growY, growK;
	 
	 public void onBlockAdded(World world, int i, int j, int k) {
		 growX = i;
		 growY = j;
		 growK = k;
	 }
	 
	 
	 public void updateTick(World world, int i, int j, int k, Random random) {
		 growK = growK + 1;
		 world.setBlockWithNotify(growX, growY, growK, Block.wood.blockID);
		 
	 }
	 
	public int tickRate() {
		return 10000;
	}
	 

}
