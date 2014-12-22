package buildcraft.transport.render;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.common.primitives.Ints;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartBlockModel;
import buildcraft.BuildCraftTransport;
import buildcraft.transport.PipeIconProvider;

public class PipeRendererModel implements ISmartBlockModel {
	private IBlockState state;

	public PipeRendererModel() {

	}

	private PipeRendererModel(IBlockState state) {
		this.state = state;
	}

	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		return new PipeRendererModel(state);
	}

	private int[] vertexToInts(float x, float y, float z, int color, TextureAtlasSprite texture, float u, float v) {
		return new int[] {
				Float.floatToRawIntBits(x),
				Float.floatToRawIntBits(y),
				Float.floatToRawIntBits(z),
				color,
				Float.floatToRawIntBits(texture.getInterpolatedU(u)),
				Float.floatToRawIntBits(texture.getInterpolatedV(v)),
				0
		};
	}

	private int[] vertexToInts(float x, float y, float z, TextureAtlasSprite texture, float u, float v) {
		return vertexToInts(x, y, z, -1, texture, u, v);
	}
	
	@Override
	public List getFaceQuads(EnumFacing p_177551_1_) {
		return Collections.emptyList();
	}

	@Override
	public List getGeneralQuads() {
		System.out.println("CALLEDESKI!");
		LinkedList<BakedQuad> quads = new LinkedList<BakedQuad>();
		quads.add(new BakedQuad(Ints.concat(
				vertexToInts(0.125f, 0.125f, 0.125f, PipeIconProvider.TYPE.PipeFluidsCobblestone.getIcon(), 2, 14),
				vertexToInts(0.125f, 0.125f, 0.875f, PipeIconProvider.TYPE.PipeFluidsCobblestone.getIcon(), 2, 14),
				vertexToInts(0.875f, 0.125f, 0.875f, PipeIconProvider.TYPE.PipeFluidsCobblestone.getIcon(), 14, 14),
				vertexToInts(0.875f, 0.125f, 0.125f, PipeIconProvider.TYPE.PipeFluidsCobblestone.getIcon(), 14, 2)
		), -1, EnumFacing.DOWN));
		return quads;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return false;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getTexture() {
		return null;
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}
}
