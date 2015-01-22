package buildcraft.api.transport.pluggable;

import net.minecraft.block.Block;

public interface IFacadePluggable {
	public Block getCurrentBlock();
	public int getCurrentMetadata();
	public boolean isTransparent();
	public boolean isHollow();
}
