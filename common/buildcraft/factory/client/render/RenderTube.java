package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileMiner;
import buildcraft.factory.tile.TileMiningWell;
import buildcraft.factory.tile.TilePump;
import buildcraft.lib.client.render.tile.RenderMultiTile;
import buildcraft.lib.client.render.tile.RenderPartElement;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.ResourceLocation;

public class RenderTube extends RenderMultiTile<TileMiner> {
    public RenderTube() {
    }

    @Override
    public void renderTileEntityFast(TileMiner tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        // TODO: optimize
        parts.clear();
        if(tile.tubeY == 0) {
            return;
        }
        for(double i = tile.tubeY; i < tile.getPos().getY(); i++) {
            double finalI = i;
            parts.add(new RenderPartElement<>((tile2, part) -> {
                part.center.positiond(0.5, finalI - tile.getPos().getY() + 0.5, 0.5);
                part.center.colouri(0xFF_FF_FF_FF);
                part.center.lightf(0x1, 0);
                part.sizeZ = part.sizeX = 8.0 / 16.0;
                part.sizeY = 16.0 / 16.0;
                ResourceLocation endTexture = null;
                ResourceLocation sideTexture = null;
                if(tile instanceof TileMiningWell) {
                    endTexture = ((TileMiningWell) tile).TUBE_END_TEXTURE.spriteLocation;
                    sideTexture = ((TileMiningWell) tile).TUBE_SIDE_TEXTURE.spriteLocation;
                } else if(tile instanceof TilePump) {
                    endTexture = ((TilePump) tile).TUBE_END_TEXTURE.spriteLocation;
                    sideTexture = ((TilePump) tile).TUBE_SIDE_TEXTURE.spriteLocation;
                }
                part.topTexture = part.bottomTexture = endTexture;
                part.northTexture = part.southTexture = part.eastTexture = part.westTexture = sideTexture;
            }));
        }
        super.renderTileEntityFast(tile, x, y, z, partialTicks, destroyStage, buffer);
    }
}
