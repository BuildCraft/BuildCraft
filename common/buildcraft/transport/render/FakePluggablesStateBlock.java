package buildcraft.transport.render;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.ForgeDirection;


/*
 * This is fake block to render pluggables without altering host state
 * May prove useful if we will make API for roboports, plugs and facades
 */
@SideOnly(Side.CLIENT)
public class FakePluggablesStateBlock extends Block implements ITextureStates {
	
	private int renderMask = 0;	
	
	private TextureStateManager textureState;
	
	
	protected FakePluggablesStateBlock() {
		super(Material.glass);
		textureState = new TextureStateManager(null); //Always Clientside
	}
	
	public TextureStateManager getTextureState() {
		return textureState;
	}
	@Override	
	public IIcon getIcon(int side, int meta) {
		return textureState.isSided() ? textureState.getTextureArray()[side] : textureState.getTexture();
	}
	@Override
	public void setRenderSide(ForgeDirection side, boolean render) {
		if (render) {
			renderMask |= 1 << side.ordinal();
		} else {
			renderMask &= ~(1 << side.ordinal());
		}
	}
	@Override
	public void setRenderAllSides() {
		renderMask = 0x3f;
	}
	@Override
	public Block getBlock() {
		return this;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldSideBeRendered(IBlockAccess blockAccess, int x, int y, int z, int side) {
		return (renderMask & (1 << side)) != 0;
	}	

}
