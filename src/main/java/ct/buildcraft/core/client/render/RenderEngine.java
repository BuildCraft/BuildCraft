package ct.buildcraft.core.client.render;

import ct.buildcraft.core.BCCore;
import ct.buildcraft.core.block.BlockEngine;
import ct.buildcraft.core.blockEntity.TileEngineBase;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.ChestBlock;

public class RenderEngine implements BlockEntityRenderer<TileEngineBase>{
	
	public RenderEngine(BlockEntityRendererProvider.Context bpc) {}
	
	private static final TextureAtlasSprite LIGHT = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BCCore.ClientModEvents.TRUNK_LIGHT);
//	private static final TextureAtlasSprite BASE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BCCore.TRUNK_BLUE);
	private static final TextureAtlasSprite CHAMBER = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BCCore.ClientModEvents.CHAMBER);
	public static final TextureAtlasSprite REDSTONE_BACK = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("buildcraftcore:blocks/engine/wood/back"));
	public static final TextureAtlasSprite REDSTONE_SIDE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("buildcraftcore:blocks/engine/wood/side")); 
	public static final TextureAtlasSprite CREATIVE_BACK = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("buildcraftcore:blocks/engine/creative/back")); 
	public static final TextureAtlasSprite CREATIVE_SIDE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("buildcraftcore:blocks/engine/creative/side"));
	public static final TextureAtlasSprite IRON_BACK = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("buildcraftenergy:blocks/engine/iron/back"));
	public static final TextureAtlasSprite IRON_SIDE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("buildcraftenergy:blocks/engine/iron/side"));
	public static final TextureAtlasSprite STONE_BACK = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("buildcraftenergy:blocks/engine/stone/back"));
	public static final TextureAtlasSprite STONE_SIDE = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(new ResourceLocation("buildcraftenergy:blocks/engine/stone/side"));
	

	
	@Override
	public void render(TileEngineBase tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int light, int overlay) {
		matrix.pushPose();
		matrix.translate(0.5f, 0.5f, 0.5f);
        Matrix4f matrix4f = matrix.last().pose();
        Matrix3f matrix3f = matrix.last().normal();
//        VertexConsumer builder = buffer.getBuffer(RenderType.solid());

        float offset  = tile.progress * 8/16f;
//        float f1 = neighborcombineresult.<Float2FloatFunction>apply(ChestBlock.opennessCombiner(tile)).get(light)
//        int i = neighborcombineresult.<Int2IntFunction>apply(new BrightnessCombiner<>()).applyAsInt(light);
        TextureAtlasSprite BACK = tile.getTextureBack();
        TextureAtlasSprite SIDE = tile.getTextureSide();
        VertexConsumer builder = BACK.wrap(buffer.getBuffer(RenderType.solid()));
        int texoffset = 0;
        switch(tile.getBlockState().getValue(BlockEngine.FACING)) {
		case DOWN:
			matrix.mulPose(Vector3f.XP.rotationDegrees(180));
			break;
		case EAST:
			matrix.mulPose(Vector3f.XP.rotationDegrees(90));
			matrix.mulPose(Vector3f.ZN.rotationDegrees(90));
			break;
		case NORTH:
			matrix.mulPose(Vector3f.XN.rotationDegrees(90));
			break;
		case SOUTH:
			matrix.mulPose(Vector3f.XP.rotationDegrees(90));
			break;
		case UP:
			break;
		case WEST:
			matrix.mulPose(Vector3f.XP.rotationDegrees(90));
			matrix.mulPose(Vector3f.ZP.rotationDegrees(90));
			break;
        }
        switch (tile.getPowerStage()) {
        case BLUE:{
            break;
        }
        case GREEN:{
        	texoffset = 2;
        	break;
        }
        case YELLOW:{
        	texoffset = 4;
        	break;
        }
        case RED:{
        	texoffset = 6;
        }
        default:
            break;
        }
        renderMovingBack(BACK, matrix4f, matrix3f, builder, light, offset,overlay);
        for(int i=0;i<4;i++) {
        	renderLight(LIGHT, matrix4f, matrix3f, builder, 15728880, offset,overlay,texoffset);
        	renderMovingSide(SIDE, matrix4f, matrix3f, builder, light, offset,overlay);
        	renderChamber(CHAMBER, matrix4f, matrix3f, builder, light, offset,overlay);
        	matrix.mulPose(Vector3f.YP.rotationDegrees(90));
        }
        

        
		matrix.popPose();
		
	}
	private static void renderMovingBack(TextureAtlasSprite back,Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int light,float offset, int overlay) {
		float width = 1f;
		
        float minU = back.getU(0);
        float maxU = back.getU(16);
        float minV = back.getV(0);
        float maxV = back.getV(16);

        builder.vertex(matrix4f, -8/16f, -4/16f + offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
                .uv(minU, minV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, 8/16f, -4/16f + offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
                .uv(maxU, minV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, 8/16f, -4/16f + offset, (width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
               .uv(maxU, maxV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, -8/16f, -4/16f +offset, (width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
                .uv(minU, maxV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();
        
        
        builder.vertex(matrix4f, -8/16f, 0/16f +offset-0.001f, (width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
        	.uv(minU, minV)
        	.overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
        	.endVertex();

        builder.vertex(matrix4f, 8/16f, 0/16f +offset-0.001f, (width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
        	.uv(maxU, minV)
        	.overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
        	.endVertex();

        builder.vertex(matrix4f, 8/16f, 0/16f + offset-0.001f, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
        	.uv(maxU, maxV)
        	.overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
        	.endVertex();

        builder.vertex(matrix4f, -8/16f, 0/16f +offset-0.001f, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
        	.uv(minU, maxV)
        	.overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
        	.endVertex();
        

	}
    private static void renderLight(TextureAtlasSprite sprite,Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int light,float offset, int overlay,int texoffset) {

        float width = 8 / 16f;

        float minU = sprite.getU(0+texoffset);
        float maxU = sprite.getU(2+texoffset);
        float minV = sprite.getV(4);
        float maxV = sprite.getV(10-offset*12);
        offset = offset*6/8;
        builder.vertex(matrix4f, -4/16f, 6/16f+0.001f , (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
                .uv(minU, minV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, -2/16f, 6/16f+0.001f , (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
                .uv(maxU, minV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, -2/16f, 0/16f+offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
               .uv(maxU, maxV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, -4/16f, 0/16f+offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
                .uv(minU, maxV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();
        builder.vertex(matrix4f, 2/16f, 6/16f+0.001f , (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
        	.uv(minU, minV)
        	.overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
        	.endVertex();

        builder.vertex(matrix4f, 4/16f, 6/16f+0.001f , (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
        	.uv(maxU, minV)
        	.overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
        	.endVertex();

        builder.vertex(matrix4f, 4/16f, 0/16f+offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
        	.uv(maxU, maxV)
        	.overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
        	.endVertex();

        builder.vertex(matrix4f, 2/16f, 0/16f+offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
        	.uv(minU, maxV)
        	.overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
        	.endVertex();

    }
    private static void renderMovingSide(TextureAtlasSprite side,Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int light,float offset, int overlay){
        float width = 16 / 16f;
        float minU = side.getU(0);
        float maxU = side.getU(16);
        float minV = side.getV(0);
        float maxV = side.getV(4);

        builder.vertex(matrix4f, -8/16f, 0/16f+offset , (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
                .uv(minU, minV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, 8/16f, 0/16f+offset , (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
                .uv(maxU, minV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, 8/16f, -4/16f+offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
               .uv(maxU, maxV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, -8/16f, -4/16f+offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f)
                .uv(minU, maxV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();
        
    }
    private static void renderChamber(TextureAtlasSprite sprite,Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int light,float offset, int overlay) {
    	float width = 10/ 16f;
    //	progress = 1f;

        float minU = sprite.getU(3);
        float maxU = sprite.getU(13);
        float minV = sprite.getV(0);
        float maxV = sprite.getV(8);

        builder.vertex(matrix4f, -5/16f, (-4/16f+offset) , (-width / 2)).color(1f, 1f, 1f,1f)
                .uv(minU, minV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, 5/16f, (-4/16f+offset) , (-width / 2)).color(1f, 1f, 1f,1f)
                .uv(maxU, minV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, 5/16f, -4/16f, (-width / 2)).color(1f, 1f, 1f,1f)
               .uv(maxU, maxV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();

        builder.vertex(matrix4f, -5/16f, -4/16f, (-width / 2)).color(1f, 1f, 1f,1f)
                .uv(minU, maxV)
                .overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1)
                .endVertex();
        
    	
    }
}

