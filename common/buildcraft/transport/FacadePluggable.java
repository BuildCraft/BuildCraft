package buildcraft.transport;

import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IFacadePluggable;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.render.FacadeRenderHelper;

public class FacadePluggable extends PipePluggable implements IFacadePluggable {
	public static final class FacadePluggableRenderer implements IPipePluggableRenderer {
		public static final IPipePluggableRenderer INSTANCE = new FacadePluggableRenderer();

		private FacadePluggableRenderer() {

		}

		@Override
		public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, ITextureStates blockStateMachine, int renderPass, int x, int y, int z) {
			FacadeRenderHelper.pipeFacadeRenderer(renderblocks, blockStateMachine, pipe.getTile(), renderPass, x, y, z, side, (IFacadePluggable) pipePluggable);
		}
	}


	public ItemFacade.FacadeState[] states;
	private ItemFacade.FacadeState activeState;
	private IPipeTile pipe;

	// Client sync
	private Block block;
	private int meta;
	private boolean transparent, renderAsHollow;

	public FacadePluggable(ItemFacade.FacadeState[] states) {
		this.states = states;
		prepareStates();
	}

	public FacadePluggable() {
	}

	@Override
	public void invalidate() {
		this.pipe = null;
	}

	@Override
	public void validate(IPipeTile pipe, ForgeDirection direction) {
		this.pipe = pipe;
	}

	@Override
	public boolean requiresRenderUpdate(PipePluggable o) {
		FacadePluggable other = (FacadePluggable) o;
		return other.block != block || other.meta != meta || other.transparent != transparent || other.renderAsHollow != renderAsHollow;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		if (states != null) {
			nbt.setTag("states", ItemFacade.FacadeState.writeArray(states));
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("states")) {
			states = ItemFacade.FacadeState.readArray(nbt.getTagList("states", Constants.NBT.TAG_COMPOUND));
		}
	}

	@Override
	public ItemStack[] getDropItems(IPipeTile pipe) {
		if (states != null) {
			return new ItemStack[]{ItemFacade.getFacade(states)};
		} else {
			return new ItemStack[]{ItemFacade.getFacade(new ItemFacade.FacadeState(getCurrentBlock(), getCurrentMetadata(), null, isHollow()))};
		}
	}

	@Override
	public boolean isBlocking(IPipeTile pipe, ForgeDirection direction) {
		return !isHollow();
	}

	@Override
	public Block getCurrentBlock() {
		prepareStates();
		return activeState == null ? block : activeState.block;
	}

	@Override
	public int getCurrentMetadata() {
		prepareStates();
		return activeState == null ? meta : activeState.metadata;
	}

	@Override
	public boolean isTransparent() {
		prepareStates();
		return activeState == null ? transparent : activeState.transparent;
	}

	public boolean isHollow() {
		prepareStates();
		return activeState == null ? renderAsHollow : activeState.hollow;
	}

	@Override
	public AxisAlignedBB getBoundingBox(ForgeDirection side) {
		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = 0.0F;
		bounds[0][1] = 1.0F;
		// Y START - END
		bounds[1][0] = 0.0F;
		bounds[1][1] = TransportConstants.FACADE_THICKNESS;
		// Z START - END
		bounds[2][0] = 0.0F;
		bounds[2][1] = 1.0F;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	@Override
	public boolean isSolidOnSide(IPipeTile pipe, ForgeDirection direction) {
		return !isHollow();
	}

	@Override
	public IPipePluggableRenderer getRenderer() {
		return FacadePluggableRenderer.INSTANCE;
	}

	@Override
	public void writeData(ByteBuf data) {
		prepareStates();

		if (activeState == null || activeState.block == null) {
			data.writeShort(0);
		} else {
			data.writeShort(Block.getIdFromBlock(activeState.block));
		}

		data.writeByte((activeState != null && activeState.transparent ? 128 : 0) |
				(activeState != null && activeState.hollow ? 64 : 0) |
				(activeState == null ? 0 : activeState.metadata));
	}

	@Override
	public void readData(ByteBuf data) {
		int blockId = data.readUnsignedShort();
		if (blockId > 0) {
			block = Block.getBlockById(blockId);
		} else {
			block = null;
		}

		int flags = data.readUnsignedByte();

		meta = flags & 0x0F;
		transparent = (flags & 0x80) != 0;
		renderAsHollow = (flags & 0x40) != 0;
	}

	private void prepareStates() {
		if (states != null && states.length > 1) {
			if (pipe == null || pipe.getPipe() == null) {
				activeState = states[0];
				return;
			}

			IPipe p = pipe.getPipe();
			int defaultStateId = -1;
			int activeStateId = -1;

			for (int i = 0; i < states.length; i++) {
				ItemFacade.FacadeState state = states[i];
				if (state.wire == null) {
					defaultStateId = i;
					continue;
				}
				if (p.isWireActive(state.wire)) {
					activeStateId = i;
					break;
				}
			}

			activeState = activeStateId < 0 ? (defaultStateId < 0 ? states[0] : states[defaultStateId]) : states[activeStateId];
		} else if (activeState == null) {
			activeState = states != null && states.length > 0 ? states[0] : null;
		}
	}
}
