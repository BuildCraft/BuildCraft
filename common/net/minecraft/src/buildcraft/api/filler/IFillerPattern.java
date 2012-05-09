package net.minecraft.src.buildcraft.api.filler;

import net.minecraft.src.ItemStack;
import net.minecraft.src.TileEntity;
import net.minecraft.src.buildcraft.api.IBox;

public interface IFillerPattern {
	public int getId();
	public void setId(int id);
	
	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace);
	public String getTextureFile();
	public int getTextureIndex();
	public String getName();

}
