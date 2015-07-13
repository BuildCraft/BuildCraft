package buildcraft.factory.render;

import java.util.List;

import javax.vecmath.Vector3f;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;

import buildcraft.core.lib.render.BuildCraftBakedModel;

public class ChuteRenderModel extends BuildCraftBakedModel {
    public static TextureAtlasSprite sideTexture = null;
    @SuppressWarnings("deprecation")
    private final IBakedModel parent;

    @SuppressWarnings("deprecation")
    protected ChuteRenderModel(ImmutableList<BakedQuad> quads, IBakedModel parent) {
        super(quads, null, DefaultVertexFormats.BLOCK);
        this.parent = parent;
    }

    @SuppressWarnings({ "deprecation", "unchecked" })
    public static ChuteRenderModel create(IBakedModel parent) {
        if (parent == null) {
            /* The "chute.json" block model file contains the top and bottom boxes, so it will look strange if it
             * doesn't exist. Just print out a warning to make sure they know that this is why. Print out a full stack
             * trace because this really shouldn't happen, and it makes it much more obvious in the logfile where the
             * error message is. */
            new IllegalStateException("For some reason, the block model for the chute block was missing!"
                + "\nThis is not meant to happen, you have a bad JAR file!").printStackTrace();
        }
        List<BakedQuad> lst = Lists.newArrayList(parent.getGeneralQuads());

        Vector3f eastSouthUp__ = new Vector3f(15 / 16F, 9 / 16F, 15 / 16F);
        Vector3f eastSouthDown = new Vector3f(11 / 16F, 3 / 16F, 11 / 16F);
        Vector3f eastNorthUp__ = new Vector3f(15 / 16F, 9 / 16F, 1 / 16F);
        Vector3f eastNorthDown = new Vector3f(11 / 16F, 3 / 16F, 5 / 16F);

        Vector3f westSouthUp__ = new Vector3f(1 / 16F, 9 / 16F, 15 / 16F);
        Vector3f westSouthDown = new Vector3f(5 / 16F, 3 / 16F, 11 / 16F);
        Vector3f westNorthUp__ = new Vector3f(1 / 16F, 9 / 16F, 1 / 16F);
        Vector3f westNorthDown = new Vector3f(5 / 16F, 3 / 16F, 5 / 16F);

        float[] uvs = new float[4];
        uvs[U_MIN] = sideTexture.getMinU();
        uvs[U_MAX] = sideTexture.getMaxU();
        uvs[V_MIN] = sideTexture.getMinV();
        uvs[V_MAX] = sideTexture.getInterpolatedV(8);

        int[] eastVertexData = getFrom(eastNorthDown, eastNorthUp__, eastSouthUp__, eastSouthDown, uvs);
        bakeQuad(lst, eastVertexData, EnumFacing.EAST);

        int[] westVertexData = getFrom(westSouthDown, westSouthUp__, westNorthUp__, westNorthDown, uvs);
        bakeQuad(lst, westVertexData, EnumFacing.WEST);

        int[] northVertexData = getFrom(westNorthDown, westNorthUp__, eastNorthUp__, eastNorthDown, uvs);
        bakeQuad(lst, northVertexData, EnumFacing.NORTH);

        int[] southVertexData = getFrom(eastSouthDown, eastSouthUp__, westSouthUp__, westSouthDown, uvs);
        bakeQuad(lst, southVertexData, EnumFacing.SOUTH);

        return new ChuteRenderModel(ImmutableList.copyOf(lst), parent);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return parent.getItemCameraTransforms();
    }
}
