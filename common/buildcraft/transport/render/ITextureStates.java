package buildcraft.transport.render;

import net.minecraft.block.Block;
import net.minecraft.util.IIcon;

import buildcraft.api.rendering.ICullable;

public interface ITextureStates extends ICullable {

	TextureStateManager getTextureState();
	
	IIcon getIcon(int side, int meta);
	
	Block getBlock();
	
}
