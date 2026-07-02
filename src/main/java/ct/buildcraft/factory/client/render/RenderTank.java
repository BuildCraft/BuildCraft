//this rendering code comes from ITank mod (1.19.4) by EwyBoy,since there are poor doc for me to look up;
//from https://github.com/EwyBoy/ITank/blob/1.19.4/src/main/java/com/ewyboy/itank/client/TankRenderer.java
package ct.buildcraft.factory.client.render;


import ct.buildcraft.factory.tile.TileTank;
import ct.buildcraft.lib.client.render.fluid.FluidRenderer;
import ct.buildcraft.lib.client.render.fluid.FluidSpriteType;
import ct.buildcraft.lib.fluid.FluidSmoother.FluidStackInterp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidStack;

public class RenderTank implements BlockEntityRenderer<TileTank> {

    private static final Vec3 MIN = new Vec3(0.13, 0.01, 0.13);
    private static final Vec3 MAX = new Vec3(0.86, 0.99, 0.86);
    private static final Vec3 MIN_CONNECTED = new Vec3(0.13, 0, 0.13);
    private static final Vec3 MAX_CONNECTED = new Vec3(0.86, 1 - 1e-5, 0.86);

    public RenderTank(BlockEntityRendererProvider.Context ctx) {
    }

    @Override
    public void render(TileTank tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int light, int overlay) {
        FluidStackInterp forRender = tile.getFluidForRender(partialTicks);
        if (forRender == null) {
            return;
        }
        matrix.pushPose();
        VertexConsumer bb = buffer.getBuffer(RenderType.cutout());
        
        boolean[] sideRender = { true, true, true, true, true, true };
        boolean connectedUp = isFullyConnected(tile, Direction.UP, partialTicks);
        boolean connectedDown = isFullyConnected(tile, Direction.DOWN, partialTicks);
        sideRender[Direction.DOWN.ordinal()] = !connectedDown;
        sideRender[Direction.UP.ordinal()] = !connectedUp;

        Vec3 min = connectedDown ? MIN_CONNECTED : MIN;
        Vec3 max = connectedUp ? MAX_CONNECTED : MAX;
        FluidStack fluid = forRender.fluid;
        int blocklight0 = light&0x0000F0;
        int skylight0 = light&0xF00000;
        int blocklight = fluid.getFluid().getFluidType().getLightLevel(fluid)<<4;
        blocklight = blocklight > blocklight0 ? blocklight : blocklight0;
        int combinedLight = (skylight0)+(blocklight);
        
        
        FluidRenderer.vertex.lighti(combinedLight);
        FluidRenderer.vertex.overlay(overlay);

        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid, forRender.amount, tile.tank.getCapacity(), min, max, bb, matrix.last(), sideRender);
        matrix.popPose();
    }
    
    private static boolean isFullyConnected(TileTank thisTank, Direction face, float partialTicks) {
        BlockPos pos = thisTank.getBlockPos().offset(face.getNormal());
        BlockEntity oTile = thisTank.getLevel().getBlockEntity(pos);
        if (oTile instanceof TileTank oTank) {
            if (!TileTank.canTanksConnect(thisTank, oTank, face)) {
                return false;
            }
            FluidStackInterp forRender = oTank.getFluidForRender(partialTicks);
            if (forRender == null) {
                return false;
            }
            FluidStack fluid = forRender.fluid;
            if (fluid == null || forRender.amount <= 0) {
                return false;
            } else if (thisTank.getFluidForRender(partialTicks) == null
                || !fluid.isFluidEqual(thisTank.getFluidForRender(partialTicks).fluid)) {
                return false;
            }
            if (fluid.getFluid().getFluidType().isLighterThanAir()) {
                face = face.getOpposite();
            }
            return forRender.amount >= oTank.tank.getCapacity() || face == Direction.UP;
        } else {
            return false;
        }
    }

/*    private void renderFluidInTank(BlockAndTintGetter world, BlockPos pos, FluidStack fluidStack, PoseStack matrix, MultiBufferSource buffer, int amount, boolean s) {
        matrix.pushPose();
        matrix.translate(0.5d, 0.5d, 0.5d);
        Matrix4f matrix4f = matrix.last().pose();
        Matrix3f matrix3f = matrix.last().normal();

        Fluid fluid = fluidStack.getFluid();
        IClientFluidTypeExtensions fluidAttributes = IClientFluidTypeExtensions.of(fluid);
        TextureAtlasSprite fluidTexture = getFluidStillSprite(fluidAttributes, fluidStack);

        int color = fluidAttributes.getTintColor(fluidStack);

        VertexConsumer builder = buffer.getBuffer(RenderType.translucent());
        if(amount < 8000) {
        	this.renderTopFluidFace(fluidTexture, matrix4f, matrix3f, builder, color, (amount/(float)8000));
        }
        else if(amount == 8000&&s)
        	this.renderTopFluidFace0(fluidTexture, matrix4f, matrix3f, builder, color);
        else  amount = 8000;

        for (int i = 0; i < 4; i++) {
            this.renderNorthFluidFace(fluidTexture, matrix4f, matrix3f, builder, color, (amount/(float)8000));
            matrix.mulPose(Vector3f.YP.rotationDegrees(90));
        }
        matrix.mulPose(Vector3f.XP.rotationDegrees(180));
        this.renderTopFluidFace0(fluidTexture, matrix4f, matrix3f, builder, color);
        
        matrix.popPose();
    }

    private void renderTopFluidFace(TextureAtlasSprite sprite, Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int color, float percent) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = ((color) & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        float width = 12 / 16f;
        float height = 16 / 16f;

        float minU = sprite.getU(3);
        float maxU = sprite.getU(13);
        float minV = sprite.getV(3);
        float maxV = sprite.getV(13);

        builder.vertex(matrix4f, width / 2, -height / 2 + percent * height, width / 2).color(r, g, b, a)
                .uv(minU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, -1, 0)
                .endVertex();

        builder.vertex(matrix4f, width / 2, -height / 2 + percent * height, -width / 2).color(r, g, b, a)
                .uv(minU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, -1, 0)
                .endVertex();

        builder.vertex(matrix4f, -width / 2, -height / 2 + percent * height, -width / 2).color(r, g, b, a)
                .uv(maxU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, -1, 0)
                .endVertex();

        builder.vertex(matrix4f, -width / 2, -height / 2 + percent * height, width / 2).color(r, g, b, a)
                .uv(maxU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, -1, 0)
                .endVertex();
    }
    private void renderTopFluidFace0(TextureAtlasSprite sprite, Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int color) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = ((color) & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        float width = 10 / 16f;
        float height = 16 / 16f;

        float minU = sprite.getU(3);
        float maxU = sprite.getU(13);
        float minV = sprite.getV(3);
        float maxV = sprite.getV(13);

        builder.vertex(matrix4f, -width / 2, -height / 2 + 1.0f * height, -width / 2).color(r, g, b, a)
                .uv(minU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();

        builder.vertex(matrix4f, -width / 2, -height / 2 + 1.0f * height, width / 2).color(r, g, b, a)
                .uv(minU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();

        builder.vertex(matrix4f, width / 2, -height / 2 + 1.0f * height, width / 2).color(r, g, b, a)
                .uv(maxU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();

        builder.vertex(matrix4f, width / 2, -height / 2 + 1.0f * height, -width / 2).color(r, g, b, a)
                .uv(maxU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, 1, 0)
                .endVertex();
    }

    private void renderNorthFluidFace(TextureAtlasSprite sprite, Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int color, float percent) {
        float r = ((color >> 16) & 0xFF) / 255f;
        float g = ((color >> 8) & 0xFF) / 255f;
        float b = ((color) & 0xFF) / 255f;
        float a = ((color >> 24) & 0xFF) / 255f;

        float width = 12 / 16f;
        float height = 16 / 16f;

        float minU = sprite.getU(3);
        float maxU = sprite.getU(13);
        float minV = sprite.getV(1);
        float maxV = sprite.getV(15 * percent);

        builder.vertex(matrix4f, -width / 2, -height / 2 + height * percent, (-width / 2) + 0.001f).color(r, g, b, a)
                .uv(minU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, width / 2, -height / 2 + height * percent, (-width / 2) + 0.001f).color(r, g, b, a)
                .uv(maxU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, width / 2, -height / 2, (-width / 2) + 0.001f).color(r, g, b, a)
                .uv(maxU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, -width / 2, -height / 2, (-width / 2) + 0.001f).color(r, g, b, a)
                .uv(minU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0, 0, 1)
                .endVertex();
    }

    private TextureAtlasSprite getFluidStillSprite(IClientFluidTypeExtensions properties, FluidStack fluidStack) {
        return Minecraft.getInstance()
               .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(properties.getStillTexture(fluidStack));
    }*/


}