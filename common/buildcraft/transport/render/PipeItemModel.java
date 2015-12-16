package buildcraft.transport.render;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.Vec3;

import buildcraft.core.lib.EntityResizableCuboid;
import buildcraft.core.lib.render.BuildCraftBakedModel;
import buildcraft.core.lib.render.RenderResizableCuboid;
import buildcraft.core.lib.utils.ColorUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.transport.ItemPipe;
import buildcraft.transport.PipeIconProvider;

public class PipeItemModel extends BuildCraftBakedModel {
    protected PipeItemModel(ImmutableList<BakedQuad> quads, TextureAtlasSprite particle) {
        super(quads, particle, DefaultVertexFormats.ITEM, getBlockTransforms());
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    public static PipeItemModel create(ItemPipe item, int colorIndex) {
        List<BakedQuad> quads = Lists.newArrayList();

        TextureAtlasSprite sprite = item.getSprite();
        if (sprite == null) {
            sprite = Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
        }

        Vec3 center = Utils.VEC_HALF;
        Vec3 radius = new Vec3(0.25, 0.5, 0.25);

        EntityResizableCuboid cuboid = new EntityResizableCuboid(null);
        cuboid.texture = sprite;
        cuboid.setTextureOffset(new Vec3(4, 0, 4));
        cuboid.setPosition(center.subtract(radius));
        cuboid.setSize(Utils.multiply(radius, 2));

        RenderResizableCuboid.INSTANCE.renderCubeStatic(quads, cuboid);

        for (int i = 0; i < quads.size(); i++) {
            BakedQuad quad = quads.get(i);
            quad = replaceTint(quad, 0xFFFFFFFF);
            quads.set(i, quad);
        }

        // Set up the colour
        if (colorIndex != 0) {
            radius = new Vec3(0.249, 0.499, 0.249);
            cuboid = new EntityResizableCuboid(null);
            cuboid.setTextureOffset(new Vec3(4, 0, 4));
            cuboid.texture = PipeIconProvider.TYPE.PipeStainedOverlay.getIcon();
            cuboid.setPosition(center.subtract(radius));
            cuboid.setSize(Utils.multiply(radius, 2));

            List<BakedQuad> coloredQuads = Lists.newArrayList();
            // Render it into a different list
            RenderResizableCuboid.INSTANCE.renderCubeStatic(coloredQuads, cuboid);

            int quadColor = 0xFF000000 + ColorUtils.getRGBColor(colorIndex - 1);
            // Add all of the quads we just rendered to the main list
            for (BakedQuad coloredQuad : coloredQuads) {
                // Change the colour to "quadColor"
                quads.add(new BakedQuad(coloredQuad.getVertexData(), quadColor, coloredQuad.getFace()));
            }
        }

        return new PipeItemModel(ImmutableList.copyOf(quads), sprite);
    }
}
