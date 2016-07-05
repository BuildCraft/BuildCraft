package buildcraft.factory.client.render;

import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.render.tile.RenderMultiTile;
import buildcraft.lib.client.render.tile.RenderPartElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;

public class RenderTank extends RenderMultiTile<TileTank> {
    public RenderTank() {
        parts.add(new RenderPartElement<>((tile, part) -> {
            part.sizeY = ((double)tile.tank.getFluidAmount() / tile.tank.getCapacity()) * 16.0 / 16.0;
            if(part.sizeY != 0) {
                part.sizeX = part.sizeZ = 10.0 / 16.0;
            } else {
                part.sizeX = part.sizeZ = 0;
            }
            part.center.positiond(0.5, part.sizeY / 2, 0.5);
//            TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
//            String flow = tile.tank.getFluidType().getFlowing().toString();
//            TextureAtlasSprite sprite;
//            if(map.getTextureExtry(flow) != null) {
//                sprite = map.getTextureExtry(flow);
//            } else {
//                sprite = map.registerSprite(tile.tank.getFluidType().getFlowing());
//            }
            if(tile.tank.getFluidType() == null) {
                return;
            }
            part.topTexture = part.bottomTexture = part.northTexture = part.southTexture = part.eastTexture = part.westTexture = tile.tank.getFluidType().getFlowing();
            if(tile.tank.isEmpty()) {
                part.center.colouri(0x00_FF_FF_FF);
            } else {
                part.center.colouri(0xFF_FF_FF_FF);
            }
//            part.center.colouri(0xFF_FF_FF_FF);
//            part.center.lightf(0x1, 0);
//            part.sizeZ = part.sizeX = 8.0 / 16.0;
//            part.sizeY = 16.0 / 16.0;
//            ResourceLocation endTexture = null;
//            ResourceLocation sideTexture = null;
//            if(tile instanceof TileMiningWell) {
//                endTexture = ((TileMiningWell) tile).TUBE_END_TEXTURE.spriteLocation;
//                sideTexture = ((TileMiningWell) tile).TUBE_SIDE_TEXTURE.spriteLocation;
//            } else if(tile instanceof TilePump) {
//                endTexture = ((TilePump) tile).TUBE_END_TEXTURE.spriteLocation;
//                sideTexture = ((TilePump) tile).TUBE_SIDE_TEXTURE.spriteLocation;
//            }
//            part.topTexture = part.bottomTexture = endTexture;
//            part.northTexture = part.southTexture = part.eastTexture = part.westTexture = sideTexture;
        }));
    }

    @Override
    public void renderTileEntityFast(TileTank tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        super.renderTileEntityFast(tile, x, y, z, partialTicks, destroyStage, buffer);
    }
}
