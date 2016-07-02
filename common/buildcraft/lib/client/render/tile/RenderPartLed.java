package buildcraft.lib.client.render.tile;

import java.util.function.BiConsumer;

import javax.vecmath.Point3f;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.client.model.ModelLoader;

import buildcraft.lib.client.model.MutableVertex;

/** A variable sized LED that can render somewhere in a TESR. Passing a resolver instance will let you modify the
 * location, colour, lightmap, and size of the single LED. */
public class RenderPartLed<T extends TileEntity> implements ITileRenderPart<T> {
    /** The centre of this LED. */
    public final MutableVertex center = new MutableVertex();
    public double sizeX = 1 / 16.0, sizeY = 1 / 16.0, sizeZ = 1 / 16.0;
    private final BiConsumer<T, RenderPartLed<T>> resolver;

    public RenderPartLed(BiConsumer<T, RenderPartLed<T>> resolver) {
        this.resolver = resolver;
    }

    public RenderPartLed(double x, double y, double z, BiConsumer<T, RenderPartLed<T>> resolver) {
        this.resolver = resolver;
        center.positiond(x, y, z);
    }

    @Override
    public void render(T tile, VertexBuffer buffer) {
        TextureAtlasSprite sprite = ModelLoader.White.INSTANCE;
        // Reset the vertex so that edits don't spill out to other tiles.
        center.texf(sprite.getInterpolatedU(8), sprite.getInterpolatedV(8));

        resolver.accept(tile, this);

        renderLed(buffer, center, sizeX, sizeY, sizeZ);
    }

    /** Renders an LED, without changing the vertex. However this does ignore the "normal" and "texture" components of
     * the vertex. */
    public static void renderLed(VertexBuffer vb, MutableVertex center, double sizeX, double sizeY, double sizeZ) {
        Point3f pos = center.position();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        double rX = sizeX / 2;
        double rY = sizeY / 2;
        double rZ = sizeZ / 2;

        // TOP
        vertex(vb, center, x - rX, y + rY, z - rZ);
        vertex(vb, center, x + rX, y + rY, z - rZ);
        vertex(vb, center, x + rX, y + rY, z + rZ);
        vertex(vb, center, x - rX, y + rY, z + rZ);

        // BOTTOM
        vertex(vb, center, x - rX, y - rY, z - rZ);
        vertex(vb, center, x + rX, y - rY, z - rZ);
        vertex(vb, center, x + rX, y - rY, z + rZ);
        vertex(vb, center, x - rX, y - rY, z + rZ);

        // NORTH
        vertex(vb, center, x - rX, y - rY, z - rZ);
        vertex(vb, center, x - rX, y + rY, z - rZ);
        vertex(vb, center, x - rX, y + rY, z + rZ);
        vertex(vb, center, x - rX, y - rY, z + rZ);

        // SOUTH
        vertex(vb, center, x + rX, y - rY, z - rZ);
        vertex(vb, center, x + rX, y + rY, z - rZ);
        vertex(vb, center, x + rX, y + rY, z + rZ);
        vertex(vb, center, x + rX, y - rY, z + rZ);

        // EAST
        vertex(vb, center, x - rX, y - rY, z - rZ);
        vertex(vb, center, x - rX, y + rY, z - rZ);
        vertex(vb, center, x + rX, y + rY, z - rZ);
        vertex(vb, center, x + rX, y - rY, z - rZ);

        // WEST
        vertex(vb, center, x - rX, y - rY, z + rZ);
        vertex(vb, center, x - rX, y + rY, z + rZ);
        vertex(vb, center, x + rX, y + rY, z + rZ);
        vertex(vb, center, x + rX, y - rY, z + rZ);
    }

    private static void vertex(VertexBuffer vb, MutableVertex center, double x, double y, double z) {
        // Using DefaultVertexFormats.BLOCK
        // -- POSITION_3F // pos
        // -- COLOR_4UB // colour
        // -- TEX_2F // texture
        // -- TEX_2S // lightmap
        vb.pos(x, y, z);
        center.renderColour(vb);
        center.renderTex(vb);
        center.renderLightMap(vb);
        vb.endVertex();
    }
}
