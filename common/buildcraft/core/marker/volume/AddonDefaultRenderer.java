package buildcraft.core.marker.volume;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.client.model.ModelLoader;

public class AddonDefaultRenderer<T extends Addon> implements IFastAddonRenderer<T> {
    @Override
    public void renderAddonFast(T addon, EntityPlayer player, float partialTicks, VertexBuffer vb) {
        AxisAlignedBB bb = addon.getBoundingBox();
        TextureAtlasSprite s = ModelLoader.White.INSTANCE;

        vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.minX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        vb.pos(bb.minX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        vb.pos(bb.minX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.minX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        vb.pos(bb.minX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.minX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

        vb.pos(bb.maxX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();
    }
}
