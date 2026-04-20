package ct.buildcraft.core.client.render;

import ct.buildcraft.core.BCCore;
import ct.buildcraft.factory.BCFactory;
import ct.buildcraft.lib.engine.TileEngineBase_BC8;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

public class RenderEngine_BC8 implements BlockEntityRenderer<TileEngineBase_BC8>{
	

	
	public RenderEngine_BC8(BlockEntityRendererProvider.Context bpc) {
	}
	
	private static final TextureAtlasSprite LIGHT = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BCCore.ClientModEvents.TRUNK_LIGHT);
	private static final TextureAtlasSprite TRUNK = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BCCore.ClientModEvents.TRUNK);
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
	public void render(TileEngineBase_BC8 tile, float partialTicks, PoseStack matrix, MultiBufferSource buffer, int light, int overlay) {
		matrix.pushPose();
		matrix.translate(0.5f, 0.5f, 0.5f);
        Matrix4f matrix4f = matrix.last().pose();
        Matrix3f matrix3f = matrix.last().normal();
//        VertexConsumer builder = buffer.getBuffer(RenderType.solid());

        float offset  = tile.RenderProgress * 8/16f;
//        float f1 = neighborcombineresult.<Float2FloatFunction>apply(ChestBlock.opennessCombiner(tile)).get(light)
//        int i = neighborcombineresult.<Int2IntFunction>apply(new BrightnessCombiner<>()).applyAsInt(light);
        TextureAtlasSprite BACK = tile.getTextureBack();
        TextureAtlasSprite SIDE = tile.getTextureSide();
        VertexConsumer builder = buffer.getBuffer(RenderType.solid());
        int texoffset = 0;
        switch(tile.getCurrentFacing()) {
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
        	break;
        }
        case OVERHEAT:{
        	texoffset = 8;
        }
        default:
            break;
        }
//        renderStatic(BACK,SIDE, matrix4f, matrix3f, builder, light, 0,overlay);
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
        
/*        RenderSystem.setShader(GameRenderer::getBlockShader);
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableDepthTest();
        back.atlas().bind();
        var tess  = Tesselator.getInstance();
        var b = tess.getBuilder();
        b.begin(Mode.QUADS, DefaultVertexFormat.BLOCK);*/
        

        builder.vertex(matrix4f, -8/16f, -4/16f + offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -4/16f + offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -4/16f + offset, (width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -4/16f +offset, (width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();        
        builder.vertex(matrix4f, -8/16f, 0/16f +offset-0.001f, (width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, 0/16f +offset-0.001f, (width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, 0/16f + offset-0.001f, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, 0/16f +offset-0.001f, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
//        tess.end();
	}
    private static void renderLight(TextureAtlasSprite sprite,Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int light,float offset, int overlay,int texoffset) {

        float width = 8 / 16f;

        float minU = sprite.getU(0+texoffset);
        float maxU = sprite.getU(2+texoffset);
        float minV = sprite.getV(4);
        float maxV = sprite.getV(10-offset*12);
        offset = offset*6/8;
        builder.vertex(matrix4f, -4/16f, 6/16f+0.001f , (-width / 2)-0.001f).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -2/16f, 6/16f+0.001f , (-width / 2)-0.001f).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -2/16f, 0/16f+offset, (-width / 2)-0.001f).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -4/16f, 0/16f+offset, (-width / 2)-0.001f).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 2/16f, 6/16f+0.001f , (-width / 2)-0.001f).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 4/16f, 6/16f+0.001f , (-width / 2)-0.001f).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 4/16f, 0/16f+offset, (-width / 2)-0.001f).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 2/16f, 0/16f+offset, (-width / 2)-0.001f).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
    }
    private static void renderMovingSide(TextureAtlasSprite side,Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int light,float offset, int overlay){
        float width = 16 / 16f;
        float minU = side.getU(0);
        float maxU = side.getU(16);
        float minV = side.getV(0);
        float maxV = side.getV(4);

        builder.vertex(matrix4f, -8/16f, 0/16f+offset , (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, 0/16f+offset , (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -4/16f+offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -4/16f+offset, (-width / 2)).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex(); 
    }
    private static void renderChamber(TextureAtlasSprite sprite,Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int light,float offset, int overlay) {
    	float width = 10/ 16f;
    //	progress = 1f;

        float minU = sprite.getU(3);
        float maxU = sprite.getU(13);
        float minV = sprite.getV(0);
        float maxV = sprite.getV(8);

        builder.vertex(matrix4f, -5/16f, (-4/16f+offset) , (-width / 2)).color(1f, 1f, 1f,1f).uv(minU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 5/16f, (-4/16f+offset) , (-width / 2)).color(1f, 1f, 1f,1f).uv(maxU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 5/16f, -4/16f, (-width / 2)).color(1f, 1f, 1f,1f).uv(maxU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -5/16f, -4/16f, (-width / 2)).color(1f, 1f, 1f,1f).uv(minU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
    }
    private static void renderStatic(TextureAtlasSprite back,TextureAtlasSprite side,Matrix4f matrix4f, Matrix3f normalMatrix, VertexConsumer builder, int light,float offset, int overlay) {
		
        float minU = back.getU(0);
        float maxU = back.getU(16);
        float minV = back.getV(0);
        float maxV = back.getV(16);
        float minU0 = side.getU(0);
        float maxU0 = side.getU(16);
        float minV0 = side.getV(0);
        float maxV0 = side.getV(4);
        
        builder.vertex(matrix4f, -8/16f, -8/16f, -8/16f).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -8/16f, -8/16f).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -8/16f, 8/16f).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -8/16f, 8/16f).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -4/16f, 8/16f).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -4/16f, 8/16f).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, minV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -4/16f, -8/16f).color(0.8f, 0.8f, 0.8f,0.8f).uv(maxU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -4/16f, -8/16f).color(0.8f, 0.8f, 0.8f,0.8f).uv(minU, maxV).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
 
        builder.vertex(matrix4f, -8/16f, -4/16f , -8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(minU0, minV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -4/16f , -8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(maxU0, minV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -8/16f, -8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(maxU0, maxV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -8/16f, -8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(minU0, maxV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        
        builder.vertex(matrix4f, -8/16f, -4/16f , 8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(minU0, minV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -4/16f , -8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(maxU0, minV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -8/16f, -8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(maxU0, maxV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -8/16f, 8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(minU0, maxV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
   
        builder.vertex(matrix4f, 8/16f, -4/16f , 8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(minU0, minV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -4/16f , 8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(maxU0, minV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, -8/16f, -8/16f, 8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(maxU0, maxV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -8/16f, 8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(minU0, maxV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        
        builder.vertex(matrix4f, 8/16f, -4/16f , -8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(minU0, minV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -4/16f , 8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(maxU0, minV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -8/16f, 8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(maxU0, maxV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
        builder.vertex(matrix4f, 8/16f, -8/16f, -8/16f).color(0.75f, 0.75f, 0.75f,0.75f).uv(minU0, maxV0).overlayCoords(overlay).uv2(light).normal(normalMatrix, 0, 0, 1).endVertex();
   
        
    }
}

