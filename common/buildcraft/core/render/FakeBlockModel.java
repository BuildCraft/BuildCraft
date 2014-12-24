package buildcraft.core.render;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;

public class FakeBlockModel implements IBakedModel {

	private IBakedModel parent;

	public FakeBlockModel(IBakedModel base) {
		this.parent = base;
	}

	@Override
	public List getFaceQuads(EnumFacing p_177551_1_) {
		return parent.getFaceQuads(p_177551_1_);
	}

	@Override
	public List getGeneralQuads() {
		return parent.getGeneralQuads();
	}

	@Override
	public boolean isAmbientOcclusion() {
		return parent.isAmbientOcclusion();
	}

	@Override
	public boolean isGui3d() {
		return parent.isGui3d();
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getTexture() {
		return Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return parent.getItemCameraTransforms();
	}

}
