package net.minecraft.src.buildcraft.factory;

import net.minecraft.src.BlockContainer;
import net.minecraft.src.BuildCraftCore;
import net.minecraft.src.Material;
import net.minecraft.src.TileEntity;
import net.minecraft.src.forge.ITextureProvider;

public class BlockPump extends BlockContainer implements ITextureProvider {

	public BlockPump(int i) {
		super(i, Material.iron);
		
		
		setHardness(5F);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected TileEntity getBlockEntity() {	
		return new TilePump();
	}
	
	@Override
	public String getTextureFile() {		
		return BuildCraftCore.customBuildCraftTexture;
	}

	 public int getBlockTextureFromSide(int i) {
		 switch (i) {
		 case 0:
			 return 6 * 16 + 4;
		 case 1:
			 return 6 * 16 + 5;		 
		 default:
			 return 6 * 16 + 3;		 
		 }
	 }
}
