package buildcraft.core.render;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.vecmath.Vector3d;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.model.ISmartBlockModel;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.fluids.BlockFluidBase;
import buildcraft.core.fluids.FluidHelper;

import com.google.common.collect.Maps;

/**
 * The fluid rendering setup associated with this class was originally created by kirderf1 for www.github.com/mraof/minestuck
 * When copying this code, please keep this comment or refer back to the original source in another way, if possible.
 */
public class FluidBlockModel implements ISmartBlockModel {
	private static final double RENDER_OFFSET = 0.001;

	private final Map<List<Double>, SimpleBakedModel> models = Maps.newHashMap();

	@Override
	public IBakedModel handleBlockState(IBlockState state) {
		IExtendedBlockState extState = (IExtendedBlockState) state;
		double heightNW = extState.getValue(FluidHelper.HEIGHT_NW);
		double heightSW = extState.getValue(FluidHelper.HEIGHT_SW);
		double heightSE = extState.getValue(FluidHelper.HEIGHT_SE);
		double heightNE = extState.getValue(FluidHelper.HEIGHT_NE);
		double flow = extState.getValue(FluidHelper.FLOW_DIRECTION);
		return getModel((BlockFluidBase) state.getBlock(), Arrays.asList(heightSE, heightSW, heightNW, heightNE, flow));
	}

	private SimpleBakedModel getModel(BlockFluidBase block, List<Double> param) {
		SimpleBakedModel model = models.get(param);
		if (model == null) {
			model = this.buildModel(block, param);
			models.put(param, model);
		}
		return model;
	}

	private SimpleBakedModel buildModel(BlockFluidBase block, List<Double> param) {
		List<List<BakedQuad>> faceQuads = new ArrayList<List<BakedQuad>>();
		for (int i = 0; i < 6; i++) {
			if (i == EnumFacing.DOWN.ordinal()) {
				Vector3d vec1 = new Vector3d(0, 0, 0);
				Vector3d vec2 = new Vector3d(1, 0, 0);
				Vector3d vec3 = new Vector3d(1, 0, 1);
				Vector3d vec4 = new Vector3d(0, 0, 1);
				faceQuads.add(Arrays.asList(createQuad(vec1, vec2, vec3, vec4, block.getFluid().getStillIcon(), EnumFacing.DOWN)));
			}
			else if (i == EnumFacing.UP.ordinal()) {
				TextureAtlasSprite sprite = param.get(4) < -999F ? block.getFluid().getStillIcon() : block.getFluid().getFlowingIcon();

				Vector3d vec1 = new Vector3d(0, param.get(2), 0);
				Vector3d vec2 = new Vector3d(0, param.get(1), 1);
				Vector3d vec3 = new Vector3d(1, param.get(0), 1);
				Vector3d vec4 = new Vector3d(1, param.get(3), 0);
				BakedQuad quad1 = createQuad(vec1, vec2, vec3, vec4, sprite, EnumFacing.UP, param.get(4));

				vec1 = new Vector3d(0, param.get(2) - RENDER_OFFSET, 0);
				vec2 = new Vector3d(1, param.get(3) - RENDER_OFFSET, 0);
				vec3 = new Vector3d(1, param.get(0) - RENDER_OFFSET, 1);
				vec4 = new Vector3d(0, param.get(1) - RENDER_OFFSET, 1);
				BakedQuad quad2 = createQuad(vec1, vec2, vec3, vec4, sprite, EnumFacing.DOWN, param.get(4));
				faceQuads.add(Arrays.asList(quad1, quad2));
			}
			else {
				EnumFacing facing = EnumFacing.values()[i];
				double height1 = param.get(facing.getHorizontalIndex());
				double height2 = param.get((facing.getHorizontalIndex() + 1) % 4);
				int p = facing.getAxisDirection() == EnumFacing.AxisDirection.POSITIVE ? 1 : 0;
				double offset = facing.getAxisDirection().getOffset() * RENDER_OFFSET;

				if (facing.getAxis() == EnumFacing.Axis.X) {
					Vector3d vec1 = new Vector3d(p, 0, p);
					Vector3d vec2 = new Vector3d(p, 0, 1 - p);
					Vector3d vec3 = new Vector3d(p, height1, 1 - p);
					Vector3d vec4 = new Vector3d(p, height2, p);
					BakedQuad quad1 = createQuad(vec1, vec2, vec3, vec4, block.getFluid().getFlowingIcon(), facing);

					vec1 = new Vector3d(p - offset, 0, p);
					vec2 = new Vector3d(p - offset, height2, p);
					vec3 = new Vector3d(p - offset, height1, 1 - p);
					vec4 = new Vector3d(p - offset, 0, 1 - p);
					BakedQuad quad2 = createQuad(vec1, vec2, vec3, vec4, block.getFluid().getFlowingIcon(), facing.getOpposite());

					faceQuads.add(Arrays.asList(quad1, quad2));
				}
				else {
					Vector3d vec1 = new Vector3d(1 - p, 0, p);
					Vector3d vec2 = new Vector3d(p, 0, p);
					Vector3d vec3 = new Vector3d(p, height1, p);
					Vector3d vec4 = new Vector3d(1 - p, height2, p);
					BakedQuad quad1 = createQuad(vec1, vec2, vec3, vec4, block.getFluid().getFlowingIcon(), facing);

					vec1 = new Vector3d(1 - p, 0, p - offset);
					vec2 = new Vector3d(1 - p, height2, p - offset);
					vec3 = new Vector3d(p, height1, p - offset);
					vec4 = new Vector3d(p, 0, p - offset);
					BakedQuad quad2 = createQuad(vec1, vec2, vec3, vec4, block.getFluid().getFlowingIcon(), facing.getOpposite());
					faceQuads.add(Arrays.asList(quad1, quad2));
				}
			}
		}

		return new SimpleBakedModel(Collections.emptyList(), faceQuads, false, false, block.getFluid().getStillIcon(), null);
	}

	private BakedQuad createQuad(Vector3d vec1, Vector3d vec2, Vector3d vec3, Vector3d vec4, TextureAtlasSprite sprite, EnumFacing facing) {
		return createQuad(vec1, vec2, vec3, vec4, sprite, facing, -1000F);
	}

	private BakedQuad createQuad(Vector3d vec1, Vector3d vec2, Vector3d vec3, Vector3d vec4, TextureAtlasSprite sprite, EnumFacing facing,
			double flowDir) {
		int[] data = new int[28];
		int shade = getShade(facing);

		float xFlow = -1, zFlow = -1;
		if (flowDir > -999F) {
			xFlow = MathHelper.sin((float) flowDir) * 0.25F;
			zFlow = MathHelper.cos((float) flowDir) * 0.25F;
		}

		Vector3d[] vectors = { vec1, vec2, vec3, vec4 };
		for (int i = 0; i < 4; i++) {
			Vector3d vec = vectors[i];
			int index = i * 7;
			data[index] = Float.floatToRawIntBits((float) vec.x);
			data[index + 1] = Float.floatToRawIntBits((float) vec.y);
			data[index + 2] = Float.floatToRawIntBits((float) vec.z);
			data[index + 3] = shade;
			if (facing.getAxis() == EnumFacing.Axis.X) {
				data[index + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(vec.z * 16F));
				data[index + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV(16F - vec.y * 16F));
			}
			else if (facing.getAxis() == EnumFacing.Axis.Y) {
				if (flowDir < -999F) {
					data[index + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(vec.x * 16F));
					data[index + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV(vec.z * 16F));
				}
				else {
					double arg = 8F + ((vec.z * 2 - 1) * xFlow + (vec.x * 2 - 1) * zFlow) * 16F;
					data[index + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(arg));
					arg = 8F + ((1 - 2 * vec.x) * xFlow + (vec.z * 2 - 1) * zFlow) * 16F;
					data[index + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV(arg));
				}
			}
			else {
				data[index + 4] = Float.floatToRawIntBits(sprite.getInterpolatedU(vec.x * 16F));
				data[index + 5] = Float.floatToRawIntBits(sprite.getInterpolatedV(16F - vec.y * 16F));
			}

		}
		return new BakedQuad(data, -1, facing);
	}

	private int getShade(EnumFacing facing)	// Copied from FaceBakery (Only the end result, and not the process)
	{
		if (facing == EnumFacing.DOWN)
			return -8421505;
		if (facing == EnumFacing.SOUTH || facing == EnumFacing.NORTH)
			return -3355444;
		if (facing == EnumFacing.WEST || facing == EnumFacing.EAST)
			return -6710887;
		else
			return -1;
	}

	// These are all ignored

	@Override
	public List getFaceQuads(EnumFacing facing) {
		return null;
	}

	@Override
	public List getGeneralQuads() {
		return null;
	}

	@Override
	public boolean isAmbientOcclusion() {
		return false;
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
		return null;
	}
}
