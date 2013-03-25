package buildcraft.transport;

import buildcraft.api.core.IIconProvider;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public interface IPipeRenderState {
	public PipeRenderState getRenderState();
	
	@SideOnly(Side.CLIENT)
	public IIconProvider getPipeIcons();
}
