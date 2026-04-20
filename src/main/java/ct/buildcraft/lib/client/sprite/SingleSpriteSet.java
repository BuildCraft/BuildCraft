package ct.buildcraft.lib.client.sprite;

import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.RandomSource;

public class SingleSpriteSet implements SpriteSet{

	public TextureAtlasSprite texture; 
	public SingleSpriteSet(TextureAtlasSprite tex) {
		texture = tex;
	}
	
	@Override
	public TextureAtlasSprite get(int p_107966_, int p_107967_) {
		return texture;
	}

	@Override
	public TextureAtlasSprite get(RandomSource p_234102_) {
		return texture;
	}

}
