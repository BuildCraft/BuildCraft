package buildcraft.builders.addon;

import buildcraft.core.marker.volume.IFastAddonRenderer;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.model.ModelLoader;

import java.util.List;

public class AddonRendererFillingPlanner implements IFastAddonRenderer<AddonFillingPlanner> {
    @Override
    public void renderAddonFast(AddonFillingPlanner addon, EntityPlayer player, float partialTicks, VertexBuffer vb) {
        List<BlockPos> blocksShouldBePlaced = addon.getBlocksShouldBePlaced();
        for (BlockPos blockPos : blocksShouldBePlaced) {
            AxisAlignedBB bb = new AxisAlignedBB(blockPos, blockPos.add(1, 1, 1)).expandXyz(-0.1);
            TextureAtlasSprite s = ModelLoader.White.INSTANCE;

            vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.minZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.minY, bb.minZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.minX, bb.minY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.minX, bb.minY, bb.minZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.minZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.minY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.minX, bb.minY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.minX, bb.minY, bb.minZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();

            vb.pos(bb.maxX, bb.minY, bb.minZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMinV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 127).tex(s.getMinU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMaxV()).lightmap(240, 0).endVertex();
            vb.pos(bb.maxX, bb.minY, bb.maxZ).color(255, 255, 255, 127).tex(s.getMaxU(), s.getMinV()).lightmap(240, 0).endVertex();
        }
    }
}
