package buildcraft.transport.client.model.key;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.item.EnumDyeColor;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.api.transport.neptune.PipeDefinition;

@SideOnly(Side.CLIENT)
public final class PipeModelKey {
    public static final PipeModelKey DEFAULT_KEY;

    static {
        int sprite = -1;
        int[] sides = { sprite, sprite, sprite, sprite, sprite, sprite };
        float[] connected = { 0.25f, 0.25f, 0, 0, 0, 0 };
        DEFAULT_KEY = new PipeModelKey(null, sprite, sides, connected, null);
    }

    public final PipeDefinition definition;
    public final int center;
    public final int[] sides;
    public final float[] connected;
    public final EnumDyeColor colour;
    private final int hash;

    public PipeModelKey(PipeDefinition definition, int center, int[] sides, float[] connected, EnumDyeColor colour) {
        this.definition = definition;
        this.center = center;
        this.sides = sides;
        this.connected = connected;
        this.colour = colour;
        this.hash = Arrays.hashCode(new int[] {//
            Objects.hashCode(definition),//
            center,//
            Arrays.hashCode(sides),//
            Arrays.hashCode(connected),//
            Objects.hashCode(colour)//
        });
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (obj.getClass() != getClass()) return false;
        PipeModelKey other = (PipeModelKey) obj;
        if (definition != other.definition) return false;
        if (center != other.center) return false;
        if (!Arrays.equals(sides, other.sides)) return false;
        if (!Arrays.equals(connected, other.connected)) return false;
        return colour == other.colour;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
