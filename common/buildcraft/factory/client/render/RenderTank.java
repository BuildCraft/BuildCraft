package buildcraft.factory.client.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.util.EnumFacing;

import net.minecraftforge.client.model.animation.FastTESR;

import buildcraft.factory.block.BlockTank;
import buildcraft.factory.tile.TileTank;
import buildcraft.lib.client.render.tile.RenderPartCube;

public class RenderTank extends FastTESR<TileTank> {
    public RenderTank() {
        // TODO: Use a fluid renderer
        parts.add(new RenderPartCube<>((tile, part) -> {
            part.sizeY = ((double) tile.tank.getFluidAmount() / tile.tank.getCapacity()) * 16.0 / 16.0;
            if (part.sizeY != 0) {
                part.sizeX = part.sizeZ = 12.0 / 16.0 * 0.99;
            } else {
                part.sizeX = part.sizeZ = 0;
            }
            part.center.positiond(0.5, part.sizeY / 2, 0.5);
            if (tile.tank.getFluidType() == null) {
                return;
            }
            part.topTexture = part.bottomTexture = part.northTexture = part.southTexture = part.eastTexture = part.westTexture = tile.tank.getFluidType().getStill();
            part.shouldSideBeRendered = side -> {
                if (side.getAxis() == EnumFacing.Axis.Y) {
                    if (Minecraft.getMinecraft().theWorld.getBlockState(tile.getPos().offset(side)).getBlock() instanceof BlockTank) {
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
        super.renderTileEntityFast(tile, x, y, z, partialTicks, destroyStage, buffer);
    }
}
