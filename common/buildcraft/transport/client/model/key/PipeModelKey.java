package buildcraft.transport.client.model.key;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.item.EnumDyeColor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class PipeModelKey {
    public static final PipeModelKey DEFAULT_KEY;

    static {
        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        TextureAtlasSprite[] sides = { sprite, sprite, sprite, sprite, sprite, sprite };
        boolean[] connected = { true, true, false, false, false, false };
        DEFAULT_KEY = new PipeModelKey(sprite, sides, connected, null);
    }

    public final TextureAtlasSprite center;
    public final TextureAtlasSprite[] sides;
    public final boolean[] connected;
    public final EnumDyeColor colour;
    private final int hash;

    public PipeModelKey(TextureAtlasSprite center, TextureAtlasSprite[] sides, boolean[] connected, EnumDyeColor colour) {
        this.center = center;
        this.sides = sides;
        this.connected = connected;
        this.colour = colour;
        hash = Arrays.hashCode(new int[] { System.identityHashCode(center), Arrays.hashCode(sides), Arrays.hashCode(connected), Objects.hashCode(colour) });
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj instanceof PipeModelKey) {
            PipeModelKey other = (PipeModelKey) obj;
            return center == other.center && colour == other.colour && eq(sides, other.sides) && Arrays.equals(connected, other.connected);
        } else {
            return false;
        }
    }

    private static boolean eq(Object[] ar1, Object[] ar2) {
        if (ar1 == ar2) return true;
        if (ar1 == null || ar2 == null) return false;
        if (ar1.length != ar2.length) return false;
        for (int i = 0; i < ar1.length; i++) {
            if (ar1[i] != ar2[i]) {
                return false;
            }
        }
        return true;
    }
}
