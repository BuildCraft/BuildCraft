package buildcraft.api.filler;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import buildcraft.api.core.IBox;
import buildcraft.api.power.IPowerProvider;

public interface IFillerPattern {

	public int getId();

	public void setId(int id);

	public float expectedPowerUse();

	public float iteratePattern(TileEntity tile, IBox box, ItemStack stackToPlace, IPowerProvider power);

	public String getTextureFile();

	public int getTextureIndex();

	public String getName();

}
