package buildcraft.api.filler;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.core.IBox;

public interface IFillerPattern {

	public int getId();

	public void setId(int id);

	public boolean iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace);

	public String getTextureFile();

	public int getTextureIndex();

	public String getName();

}
