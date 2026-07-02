package ct.buildcraft.robotics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import ct.buildcraft.robotics.entity.EntityRobot;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders a placed BuildCraft robot as the original 7.1.x renderer did: a textured 8x8x8 cube centered on the robot
 * position. Do not render the robot entity via the item renderer here; the item model has inventory transforms, which
 * makes placed robots look oversized and can also hide the per-profession entity texture when the client has not baked
 * the correct item override yet.
 */
public class RenderRobot extends EntityRenderer<EntityRobot> {
    private static final ResourceLocation OVERLAY_RED = new ResourceLocation("buildcraftrobotics", "textures/entities/overlay_side.png");
    private static final ResourceLocation OVERLAY_CYAN = new ResourceLocation("buildcraftrobotics", "textures/entities/overlay_bottom.png");
    private static final float MIN = -4.0F / 16.0F;
    private static final float MAX = 4.0F / 16.0F;
    private static final float TEX_SIZE = 32.0F;

    public RenderRobot(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.15F;
    }

    @Override
    public void render(EntityRobot robot, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Same idea as the 1.7.10 renderer: rotate the cube by the robot yaw, then draw the cube around the entity
        // origin. Most docked robots currently have yaw 0, but keeping this makes the renderer correct once movement
        // and AI are ported.
        poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F - robot.getYRot()));

        PoseStack.Pose pose = poseStack.last();
        renderRobotCube(buffer.getBuffer(RenderType.entityCutoutNoCull(getTextureLocation(robot))),
                pose.pose(), pose.normal(), packedLight, 1.0F);

        // Old BuildCraft rendered this overlay for non-sleeping robots. The inventory item renderer always used this
        // active overlay, but placed robots only show it while actually doing work.
        if (!robot.isAsleepForRendering()) {
            float storagePercent = Math.max(0.0F, Math.min(1.0F, robot.getEnergy() / (float) EntityRobot.MAX_ENERGY));
            renderRobotCube(buffer.getBuffer(RenderType.entityTranslucent(OVERLAY_RED)),
                    pose.pose(), pose.normal(), LightTexture.FULL_BRIGHT, storagePercent);
            renderRobotCube(buffer.getBuffer(RenderType.entityTranslucent(OVERLAY_CYAN)),
                    pose.pose(), pose.normal(), LightTexture.FULL_BRIGHT, 1.0F);
        }

        poseStack.popPose();
        super.render(robot, yaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRobot robot) {
        return robot.getTexture();
    }

    private static void renderRobotCube(VertexConsumer builder, Matrix4f pose, Matrix3f normal, int light, float alpha) {
        // BuildCraft 7.1.x used ModelRenderer(model, 0, 0).addBox(-4, -4, -4, 8, 8, 8) with 32x32 robot
        // textures. That is the standard old entity-head layout: two 8x8 caps on the first row and four 8x8
        // side faces on the second row. The previous port used 4x4 block-model UVs, so half the faces sampled
        // transparent areas of the texture and looked untextured.
        quad(builder, pose, normal, light,
                MIN, MAX, MIN, MAX, MAX, MIN, MAX, MAX, MAX, MIN, MAX, MAX,
                16, 0, 24, 8, 0, 1, 0, alpha); // up
        quad(builder, pose, normal, light,
                MIN, MIN, MAX, MAX, MIN, MAX, MAX, MIN, MIN, MIN, MIN, MIN,
                8, 0, 16, 8, 0, -1, 0, alpha); // down
        quad(builder, pose, normal, light,
                MIN, MIN, MIN, MAX, MIN, MIN, MAX, MAX, MIN, MIN, MAX, MIN,
                8, 8, 16, 16, 0, 0, -1, alpha); // north/front
        quad(builder, pose, normal, light,
                MAX, MIN, MAX, MIN, MIN, MAX, MIN, MAX, MAX, MAX, MAX, MAX,
                24, 8, 32, 16, 0, 0, 1, alpha); // south/back
        quad(builder, pose, normal, light,
                MIN, MIN, MAX, MIN, MIN, MIN, MIN, MAX, MIN, MIN, MAX, MAX,
                0, 8, 8, 16, -1, 0, 0, alpha); // west/right side in the old texture layout
        quad(builder, pose, normal, light,
                MAX, MIN, MIN, MAX, MIN, MAX, MAX, MAX, MAX, MAX, MAX, MIN,
                16, 8, 24, 16, 1, 0, 0, alpha); // east/left side in the old texture layout
    }

    private static void quad(VertexConsumer builder, Matrix4f pose, Matrix3f normal, int light,
                             float x1, float y1, float z1,
                             float x2, float y2, float z2,
                             float x3, float y3, float z3,
                             float x4, float y4, float z4,
                             float u1, float v1, float u2, float v2,
                             float nx, float ny, float nz, float alpha) {
        vertex(builder, pose, normal, light, x1, y1, z1, u1 / TEX_SIZE, v2 / TEX_SIZE, nx, ny, nz, alpha);
        vertex(builder, pose, normal, light, x2, y2, z2, u2 / TEX_SIZE, v2 / TEX_SIZE, nx, ny, nz, alpha);
        vertex(builder, pose, normal, light, x3, y3, z3, u2 / TEX_SIZE, v1 / TEX_SIZE, nx, ny, nz, alpha);
        vertex(builder, pose, normal, light, x4, y4, z4, u1 / TEX_SIZE, v1 / TEX_SIZE, nx, ny, nz, alpha);
    }

    private static void vertex(VertexConsumer builder, Matrix4f pose, Matrix3f normal, int light,
                               float x, float y, float z, float u, float v,
                               float nx, float ny, float nz, float alpha) {
        builder.vertex(pose, x, y, z)
                .color(1.0F, 1.0F, 1.0F, alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light)
                .normal(normal, nx, ny, nz)
                .endVertex();
    }
}
