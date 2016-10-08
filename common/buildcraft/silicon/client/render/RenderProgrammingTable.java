package buildcraft.silicon.client.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.silicon.tile.TileProgrammingTable;

public class RenderProgrammingTable extends FastTESR<TileProgrammingTable> {
    @Override
    public void renderTileEntityFast(TileProgrammingTable tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        Minecraft.getMinecraft().mcProfiler.startSection("bc");
        Minecraft.getMinecraft().mcProfiler.startSection("table");
        Minecraft.getMinecraft().mcProfiler.startSection("programming");

        TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().registerSprite(new ResourceLocation("blocks/glass_white"));
        int combinedLight = tile.getWorld().getCombinedLight(tile.getPos(), 0);
        int light1 = combinedLight >> 16 & 65535;
        int light2 = combinedLight & 65535;
        buffer.pos(x + 4 / 16D, y + 9 / 16D, z + 4 / 16D).color(255, 255, 255, 255).tex(sprite.getMinU(), sprite.getMinV()).lightmap(light1, light2).endVertex();
        buffer.pos(x + 12 / 16D, y + 9 / 16D, z + 4 / 16D).color(255, 255, 255, 255).tex(sprite.getMaxU(), sprite.getMinV()).lightmap(light1, light2).endVertex();
        buffer.pos(x + 12 / 16D, y + 9 / 16D, z + 12 / 16D).color(255, 255, 255, 255).tex(sprite.getMaxU(), sprite.getMaxV()).lightmap(light1, light2).endVertex();
        buffer.pos(x + 4 / 16D, y + 9 / 16D, z + 12 / 16D).color(255, 255, 255, 255).tex(sprite.getMinU(), sprite.getMaxV()).lightmap(light1, light2).endVertex();

        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
        Minecraft.getMinecraft().mcProfiler.endSection();
    }
}
