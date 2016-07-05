package buildcraft.lib.client.render.tile;

import buildcraft.lib.client.model.MutableVertex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelLoader;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Point3f;
import java.util.function.BiConsumer;
import java.util.function.Function;

/** A variable sized element (like LED) that can render somewhere in a TESR. Passing a resolver instance will let you modify the
 * location, colour, lightmap, and size of the single element. */
public class RenderPartElement<T extends TileEntity> implements ITileRenderPart<T> {
    /** The centre of this element. */
    public final MutableVertex center = new MutableVertex();
    public double sizeX = 1 / 16.0, sizeY = 1 / 16.0, sizeZ = 1 / 16.0;
    public ResourceLocation topTexture = null, bottomTexture = null, northTexture = null, southTexture = null, eastTexture = null, westTexture = null;
    private final BiConsumer<T, RenderPartElement<T>> resolver;
    public Function<EnumFacing, Boolean> shouldSideBeRendered = side -> true;

    public RenderPartElement(BiConsumer<T, RenderPartElement<T>> resolver) {
        this.resolver = resolver;
    }

    public RenderPartElement(double x, double y, double z, BiConsumer<T, RenderPartElement<T>> resolver) {
        this.resolver = resolver;
        center.positiond(x, y, z);
    }

    @Override
    public void render(T tile, VertexBuffer buffer) {
        TextureAtlasSprite sprite = ModelLoader.White.INSTANCE;
        // Reset the vertex so that edits don't spill out to other tiles.
        center.texf(sprite.getInterpolatedU(8), sprite.getInterpolatedV(8));

        resolver.accept(tile, this);

        renderElement(buffer, center, sizeX, sizeY, sizeZ, shouldSideBeRendered, this);
    }

    /** Renders an element, without changing the vertex. However this does ignore the "normal" and "texture" components of
     * the vertex. */
    public static void renderElement(VertexBuffer vb, MutableVertex center, double sizeX, double sizeY, double sizeZ, Function<EnumFacing, Boolean> shouldSideBeRendered, RenderPartElement renderPartElement) {
        Point3f pos = center.position();
        double x = pos.x;
        double y = pos.y;
        double z = pos.z;

        double rX = sizeX / 2;
        double rY = sizeY / 2;
        double rZ = sizeZ / 2;

        // TOP
        if(shouldSideBeRendered.apply(EnumFacing.UP)) {
            vertex(vb, center, x - rX, y + rY, z + rZ, 1, 0, renderPartElement.topTexture);
            vertex(vb, center, x + rX, y + rY, z + rZ, 1, 1, renderPartElement.topTexture);
            vertex(vb, center, x + rX, y + rY, z - rZ, 0, 1, renderPartElement.topTexture);
            vertex(vb, center, x - rX, y + rY, z - rZ, 0, 0, renderPartElement.topTexture);
        }

        // BOTTOM
        if(shouldSideBeRendered.apply(EnumFacing.DOWN)) {
            vertex(vb, center, x - rX, y - rY, z - rZ, 0, 0, renderPartElement.bottomTexture);
            vertex(vb, center, x + rX, y - rY, z - rZ, 0, 1, renderPartElement.bottomTexture);
            vertex(vb, center, x + rX, y - rY, z + rZ, 1, 1, renderPartElement.bottomTexture);
            vertex(vb, center, x - rX, y - rY, z + rZ, 1, 0, renderPartElement.bottomTexture);
        }

        // NORTH
        if(shouldSideBeRendered.apply(EnumFacing.NORTH)) {
            vertex(vb, center, x - rX, y - rY, z + rZ, 1, 0, renderPartElement.northTexture);
            vertex(vb, center, x - rX, y + rY, z + rZ, 1, 1, renderPartElement.northTexture);
            vertex(vb, center, x - rX, y + rY, z - rZ, 0, 1, renderPartElement.northTexture);
            vertex(vb, center, x - rX, y - rY, z - rZ, 0, 0, renderPartElement.northTexture);
        }

        // SOUTH
        if(shouldSideBeRendered.apply(EnumFacing.SOUTH)) {
            vertex(vb, center, x + rX, y - rY, z - rZ, 0, 0, renderPartElement.southTexture);
            vertex(vb, center, x + rX, y + rY, z - rZ, 0, 1, renderPartElement.southTexture);
            vertex(vb, center, x + rX, y + rY, z + rZ, 1, 1, renderPartElement.southTexture);
            vertex(vb, center, x + rX, y - rY, z + rZ, 1, 0, renderPartElement.southTexture);
        }

        // EAST
        if(shouldSideBeRendered.apply(EnumFacing.EAST)) {
            vertex(vb, center, x - rX, y - rY, z - rZ, 0, 0, renderPartElement.eastTexture);
            vertex(vb, center, x - rX, y + rY, z - rZ, 0, 1, renderPartElement.eastTexture);
            vertex(vb, center, x + rX, y + rY, z - rZ, 1, 1, renderPartElement.eastTexture);
            vertex(vb, center, x + rX, y - rY, z - rZ, 1, 0, renderPartElement.eastTexture);
        }

        // WEST
        if(shouldSideBeRendered.apply(EnumFacing.WEST)) {
            vertex(vb, center, x + rX, y - rY, z + rZ, 1, 0, renderPartElement.westTexture);
            vertex(vb, center, x + rX, y + rY, z + rZ, 1, 1, renderPartElement.westTexture);
            vertex(vb, center, x - rX, y + rY, z + rZ, 0, 1, renderPartElement.westTexture);
            vertex(vb, center, x - rX, y - rY, z + rZ, 0, 0, renderPartElement.westTexture);
        }
    }

    private static void vertex(VertexBuffer vb, MutableVertex center, double x, double y, double z, double u, double v, ResourceLocation texture) {
        // Using DefaultVertexFormats.BLOCK
        // -- POSITION_3F // pos
        // -- COLOR_4UB // colour
        // -- TEX_2F // texture
        // -- TEX_2S // lightmap
        vb.pos(x, y, z);
        center.renderColour(vb);
        if(texture != null) {
            TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
            TextureAtlasSprite sprite;
            if(map.getTextureExtry(texture.toString()) != null) {
                sprite = map.getTextureExtry(texture.toString());
            } else {
                sprite = map.registerSprite(texture);
            }
            vb.tex(sprite.getInterpolatedU(u * 16), sprite.getInterpolatedV(v * 16));
        } else {
            center.renderTex(vb);
        }
        center.renderLightMap(vb);
        vb.endVertex();
        // FIXME: lighting is strange now
    }
}
