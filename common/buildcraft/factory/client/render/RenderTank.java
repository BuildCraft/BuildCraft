package buildcraft.factory.client.render;

import buildcraft.factory.block.BlockTank;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.render.tile.RenderMultiTile;
import buildcraft.lib.client.render.tile.RenderPartElement;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.EnumFacing;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

public class RenderTank extends RenderMultiTile<TileTank> {
    public RenderTank() {
        // TODO: optimize
        parts.add(new RenderPartElement<>((tile, part) -> {
            part.sizeY = ((double)tile.tank.getFluidAmount() / tile.tank.getCapacity()) * 16.0 / 16.0;
            if(part.sizeY != 0) {
                part.sizeX = part.sizeZ = 12.0 / 16.0 * 0.99;
            } else {
                part.sizeX = part.sizeZ = 0;
            }
            part.center.positiond(0.5, part.sizeY / 2, 0.5);
            if(tile.tank.getFluidType() == null) {
                return;
            }
            part.topTexture = part.bottomTexture = part.northTexture = part.southTexture = part.eastTexture = part.westTexture = tile.tank.getFluidType().getStill();
            part.shouldSideBeRendered = side -> {
                if(side.getAxis() == EnumFacing.Axis.Y) {
                    if(Minecraft.getMinecraft().theWorld.getBlockState(tile.getPos().offset(side)).getBlock() instanceof BlockTank) {
                        TileTank tileSide = (TileTank) Minecraft.getMinecraft().theWorld.getTileEntity(tile.getPos().offset(side));
                        return tileSide.tank.isEmpty();
                    }
                    return true;
                }
                return true;
            };
        }));
    }

    @Override
    public void renderTileEntityFast(TileTank tile, double x, double y, double z, float partialTicks, int destroyStage, VertexBuffer buffer) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        super.renderTileEntityFast(tile, x, y, z, partialTicks, destroyStage, buffer);
//        GL11.glDisable(GL11.GL_CULL_FACE); // FIXME: Why I can't disable it here?
    }
}
