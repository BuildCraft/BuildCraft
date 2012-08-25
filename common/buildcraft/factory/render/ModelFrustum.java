package buildcraft.factory.render;

import net.minecraft.src.ModelRenderer;
import net.minecraft.src.PositionTextureVertex;
import net.minecraft.src.Tessellator;
import net.minecraft.src.TexturedQuad;

public class ModelFrustum {

	/**
	 * The (x,y,z) vertex positions and (u,v) texture coordinates for each of
	 * the 8 points on a cube
	 */
	private PositionTextureVertex[] vertexPositions;

	/** An array of 6 TexturedQuads, one for each face of a cube */
	private TexturedQuad[] quadList;

	/** X vertex coordinate of lower box corner */
	public final float posX1;

	/** Y vertex coordinate of lower box corner */
	public final float posY1;

	/** Z vertex coordinate of lower box corner */
	public final float posZ1;

	/** X vertex coordinate of upper box corner */
	public final float posX2;

	/** Y vertex coordinate of upper box corner */
	public final float posY2;

	/** Z vertex coordinate of upper box corner */
	public final float posZ2;
	public String field_40673_g;

	public ModelFrustum(ModelRenderer par1ModelRenderer, int textureOffsetX, int textureOffsetY, float originX, float originY,
			float originZ, int bottomWidth, int bottomDepth, int topWidth, int topDepth, int height, float scaleFactor) {
		this.posX1 = originX;
		this.posY1 = originY;
		this.posZ1 = originZ;

		this.vertexPositions = new PositionTextureVertex[8];
		this.quadList = new TexturedQuad[6];

		float bottomDeltaX = bottomWidth > topWidth ? 0 : (topWidth - bottomWidth) / 2f;
		float topDeltaX = bottomWidth > topWidth ? (bottomWidth - topWidth) / 2f : 0;

		float bottomDeltaZ = bottomDepth > topDepth ? 0 : (topDepth - bottomDepth) / 2f;
		float topDeltaZ = bottomDepth > topDepth ? (bottomDepth - topDepth) / 2f : 0;

		float targetX = originX + Math.max((float) bottomWidth, (float) topWidth);
		float targetY = originY + height;
		float targetZ = originZ + Math.max((float) bottomDepth, (float) topDepth);

		this.posX2 = targetX;
		this.posY2 = targetY;
		this.posZ2 = targetZ;

		originX -= scaleFactor;
		originY -= scaleFactor;
		originZ -= scaleFactor;
		targetX += scaleFactor;
		targetY += scaleFactor;
		targetZ += scaleFactor;

		if (par1ModelRenderer.mirror) {
			float var14 = targetX;
			targetX = originX;
			originX = var14;
		}

		PositionTextureVertex var23 = new PositionTextureVertex(originX + bottomDeltaX, originY, originZ + bottomDeltaZ, 0.0F,
				0.0F);
		PositionTextureVertex var15 = new PositionTextureVertex(targetX - bottomDeltaX, originY, originZ + bottomDeltaZ, 0.0F,
				8.0F);
		PositionTextureVertex var16 = new PositionTextureVertex(targetX - topDeltaX, targetY, originZ + topDeltaZ, 8.0F, 8.0F);
		PositionTextureVertex var17 = new PositionTextureVertex(originX + topDeltaX, targetY, originZ + topDeltaZ, 8.0F, 0.0F);

		PositionTextureVertex var18 = new PositionTextureVertex(originX + bottomDeltaX, originY, targetZ - bottomDeltaZ, 0.0F,
				0.0F);
		PositionTextureVertex var19 = new PositionTextureVertex(targetX - bottomDeltaX, originY, targetZ - bottomDeltaZ, 0.0F,
				8.0F);
		PositionTextureVertex var20 = new PositionTextureVertex(targetX - topDeltaX, targetY, targetZ - topDeltaZ, 8.0F, 8.0F);
		PositionTextureVertex var21 = new PositionTextureVertex(originX + topDeltaX, targetY, targetZ - topDeltaZ, 8.0F, 0.0F);
		this.vertexPositions[0] = var23;
		this.vertexPositions[1] = var15;
		this.vertexPositions[2] = var16;
		this.vertexPositions[3] = var17;
		this.vertexPositions[4] = var18;
		this.vertexPositions[5] = var19;
		this.vertexPositions[6] = var20;
		this.vertexPositions[7] = var21;

		int depth = Math.max(bottomDepth, topDepth);
		int width = Math.max(bottomWidth, topWidth);

		this.quadList[0] = new TexturedQuad(new PositionTextureVertex[] { var19, var15, var16, var20 }, textureOffsetX + depth
				+ width, textureOffsetY + depth, textureOffsetX + depth + width + depth, textureOffsetY + depth + height,
				par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);
		this.quadList[1] = new TexturedQuad(new PositionTextureVertex[] { var23, var18, var21, var17 }, textureOffsetX,
				textureOffsetY + depth, textureOffsetX + depth, textureOffsetY + depth + height, par1ModelRenderer.textureWidth,
				par1ModelRenderer.textureHeight);
		this.quadList[2] = new TexturedQuad(new PositionTextureVertex[] { var19, var18, var23, var15 }, textureOffsetX + depth,
				textureOffsetY, textureOffsetX + depth + width, textureOffsetY + depth, par1ModelRenderer.textureWidth,
				par1ModelRenderer.textureHeight);
		this.quadList[3] = new TexturedQuad(new PositionTextureVertex[] { var16, var17, var21, var20 }, textureOffsetX + depth
				+ width, textureOffsetY + depth, textureOffsetX + depth + width + width, textureOffsetY,
				par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);
		this.quadList[4] = new TexturedQuad(new PositionTextureVertex[] { var15, var23, var17, var16 }, textureOffsetX + depth,
				textureOffsetY + depth, textureOffsetX + depth + width, textureOffsetY + depth + height,
				par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);
		this.quadList[5] = new TexturedQuad(new PositionTextureVertex[] { var18, var19, var20, var21 }, textureOffsetX + depth
				+ width + depth, textureOffsetY + depth, textureOffsetX + depth + width + depth + width, textureOffsetY + depth
				+ height, par1ModelRenderer.textureWidth, par1ModelRenderer.textureHeight);

		if (par1ModelRenderer.mirror) {
			for (int var22 = 0; var22 < this.quadList.length; ++var22) {
				this.quadList[var22].flipFace();
			}
		}
	}

	/**
	 * Draw the six sided box defined by this ModelBox
	 */
	public void render(Tessellator par1Tessellator, float par2) {
		for (int var3 = 0; var3 < this.quadList.length; ++var3) {
			this.quadList[var3].draw(par1Tessellator, par2);
		}
	}

	public ModelFrustum func_40671_a(String par1Str) {
		this.field_40673_g = par1Str;
		return this;
	}
}
