package ct.buildcraft.api.core.render;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;

/** Holds information on some sort of sprite. These might not be part of the main texture atlas (they might be from a
 * GUI texture), in which case {@link #bindTexture()} should be called before using the results from
 * {@link #getInterpU(double)} or {@link #getInterpV(double)}
 * <p>
 * <b> IMPORTANT: Unlike Minecraft's {@link TextureAtlasSprite} this uses co-ordinates between 0 and 1, rather than 0
 * and 16! </b> */
public interface ISprite {
    /** Binds this sprites backing texture so that this sprite will be referenced when you use the results of
     * {@link #getInterpU(double)} and {@link #getInterpV(double)} */
    void bindTexture();

    /** @param u A value between 0 and 1
     * @Deprecated use {@link TextureAtlasSprite#getU(double)} , with your param multiply by 16
     * @return */
 //   @Deprecated
    float getInterpU(double u);

    /** @param v A value between 0 and 1
     * @Deprecated use {@link TextureAtlasSprite#getU(double)} , with your param multiply by 16 
     * @return */
 //   @Deprecated
    float getInterpV(double v);
}
