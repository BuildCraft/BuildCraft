package buildcraft.transport.render;

import tv.twitch.Core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import com.google.common.primitives.Ints;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.client.model.ISmartItemModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import buildcraft.BuildCraftTransport;
import buildcraft.core.CoreConstants;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeRenderState;
import buildcraft.transport.TileGenericPipe;

public class PipeRendererModel implements IBakedModel, ISmartBlockModel, ISmartItemModel {
	private IExtendedBlockState state;
	private ItemStack stack;
	private boolean isItem;

	public PipeRendererModel() {

	}

	private PipeRendererModel(IBlockState state) {
		this.state = (IExtendedBlockState) state;
		isItem = false;
	}

	private PipeRendererModel(ItemStack stack) {
		this.stack = stack;
		isItem = true;
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

	private boolean[] isConnected = new boolean[6];

	private float vcmin(int s) {
		return isConnected[s] ? 0.0F : CoreConstants.PIPE_MIN_POS;
	}

	private float vcmax(int s) {
		return isConnected[s] ? 1.0F : CoreConstants.PIPE_MAX_POS;
	}

	private int tcmin(int s) {
		return isConnected[s] ? 0 : 4;
	}

	private int tcmax(int s) {
		return isConnected[s] ? 16 : 12;
	}

	private BakedQuad createQuadYAxis(float s1, float s2, float s3, float s4, float y, TextureAtlasSprite sprite, EnumFacing side) {
		return new BakedQuad(Ints.concat(
				vertexToInts(s1, y, s3, sprite, s1 * 16, s3 * 16),
				vertexToInts(s1, y, s4, sprite, s1 * 16, s4 * 16),
				vertexToInts(s2, y, s4, sprite, s2 * 16, s4 * 16),
				vertexToInts(s2, y, s3, sprite, s2 * 16, s3 * 16)
		), -1, side);
	}

	private BakedQuad createQuadXAxis(float s3, float s4, float s1, float s2, float x, TextureAtlasSprite sprite, EnumFacing side) {
		return new BakedQuad(Ints.concat(
				vertexToInts(x, s1, s3, sprite, s1 * 16, s3 * 16),
				vertexToInts(x, s1, s4, sprite, s1 * 16, s4 * 16),
				vertexToInts(x, s2, s4, sprite, s2 * 16, s4 * 16),
				vertexToInts(x, s2, s3, sprite, s2 * 16, s3 * 16)
		), -1, side);
	}

	private BakedQuad createQuadZAxis(float s1, float s2, float s3, float s4, float z, TextureAtlasSprite sprite, EnumFacing side) {
		return new BakedQuad(Ints.concat(
				vertexToInts(s1, s3, z, sprite, s1 * 16, s3 * 16),
				vertexToInts(s1, s4, z, sprite, s1 * 16, s4 * 16),
				vertexToInts(s2, s4, z, sprite, s2 * 16, s4 * 16),
				vertexToInts(s2, s3, z, sprite, s2 * 16, s3 * 16)
		), -1, side);
	}

	private void renderCube(List<BakedQuad> quads, float x1, float y1, float z1, float x2, float y2, float z2, TextureAtlasSprite sprite) {
		quads.add(createQuadYAxis(x1, x2, z1, z2, y1, sprite, EnumFacing.DOWN));
		quads.add(createQuadYAxis(x1, x2, z1, z2, y2, sprite, EnumFacing.UP));
		quads.add(createQuadXAxis(y1, y2, z1, z2, x1, sprite, EnumFacing.WEST));
		quads.add(createQuadXAxis(y1, y2, z1, z2, x2, sprite, EnumFacing.EAST));
		quads.add(createQuadZAxis(y1, y2, x1, x2, z1, sprite, EnumFacing.NORTH));
		quads.add(createQuadZAxis(y1, y2, x1, x2, z2, sprite, EnumFacing.SOUTH));
		quads.add(createQuadYAxis(x1, x2, z1, z2, y1, sprite, EnumFacing.UP));
		quads.add(createQuadYAxis(x1, x2, z1, z2, y2, sprite, EnumFacing.DOWN));
		quads.add(createQuadXAxis(y1, y2, z1, z2, x1, sprite, EnumFacing.EAST));
		quads.add(createQuadXAxis(y1, y2, z1, z2, x2, sprite, EnumFacing.WEST));
		quads.add(createQuadZAxis(y1, y2, x1, x2, z1, sprite, EnumFacing.SOUTH));
		quads.add(createQuadZAxis(y1, y2, x1, x2, z2, sprite, EnumFacing.NORTH));
	}

	@Override
	public List getGeneralQuads() {
		LinkedList<BakedQuad> quads = new LinkedList<BakedQuad>();
		if (state != null) {
			//TileGenericPipe.CoreState coreState = state.getValue(TileGenericPipe.CORE_STATE_PROP);
			PipeRenderState renderState = state.getValue(TileGenericPipe.RENDER_STATE_PROP);
			if (renderState == null || renderState.pipeConnectionMatrix == null) {
				return quads;
			}

			for (int i = 0; i < 6; i++) {
				isConnected[i] = renderState.pipeConnectionMatrix.isConnected(EnumFacing.getFront(i));
			}
		} else {
			for (int i = 0; i < 6; i++) {
				isConnected[i] = i < 2;
			}
		}
		TextureAtlasSprite sprite = PipeIconProvider.TYPE.PipeItemsQuartz.getIcon();
		renderCube(quads, CoreConstants.PIPE_MIN_POS,CoreConstants.PIPE_MIN_POS, CoreConstants.PIPE_MIN_POS,
				CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS, CoreConstants.PIPE_MAX_POS,
				sprite);
		float min = CoreConstants.PIPE_MIN_POS;
		float max = CoreConstants.PIPE_MAX_POS;
		if (isConnected[0]) {
			renderCube(quads, min, 0, min, max, min, max, sprite);
		}
		if (isConnected[1]) {
			renderCube(quads, min, max, min, max, 1, max, sprite);
		}
		if (isConnected[4]) {
			renderCube(quads, 0, min, min, min, max, max, sprite);
		}
		if (isConnected[5]) {
			renderCube(quads, max, min, min, 1, max, max, sprite);
		}
		if (isConnected[2]) {
			renderCube(quads, min, min, 0, max, max, min, sprite);
		}
		if (isConnected[3]) {
			renderCube(quads, min, min, max, max, max, 1, sprite);
		}
		return quads;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return true;
	}

	@Override
	public boolean isGui3d() {
		return true;
	}

	@Override
	public boolean isBuiltInRenderer() {
		return false;
	}

	@Override
	public TextureAtlasSprite getTexture() {
		return PipeIconProvider.TYPE.PipeFluidsCobblestone.getIcon();
	}

	@Override
	public ItemCameraTransforms getItemCameraTransforms() {
		return ItemCameraTransforms.DEFAULT;
	}

	@Override
	public IBakedModel handleItemState(ItemStack stack) {
		return new PipeRendererModel(stack);
	}
}
