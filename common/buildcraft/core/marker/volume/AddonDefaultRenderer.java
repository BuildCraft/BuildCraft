package buildcraft.core.marker.volume;

import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;

public class AddonDefaultRenderer implements IFastAddonRenderer {
    @Override
    public void renderAddonFast(Addon addon, EntityPlayer player, float partialTicks, VertexBuffer vb) {
        AxisAlignedBB bb = addon.getBoundingBox();
        vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.minX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.minZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.maxY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
        vb.pos(bb.maxX, bb.minY, bb.maxZ).color(255, 255, 255, 255).tex(0, 0).lightmap(250, 250).endVertex();
    }
}
