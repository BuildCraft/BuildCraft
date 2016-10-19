package buildcraft.transport.client.model.key;

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

    public PipeModelKey(PipeDefinition definition, int center, int[] sides, float[] connected, EnumDyeColor colour) {
        this.definition = definition;
        this.center = center;
        this.sides = sides;
        this.connected = connected;
        this.colour = colour;
    }
}
