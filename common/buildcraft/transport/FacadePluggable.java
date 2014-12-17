package buildcraft.transport;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.pipes.IPipeContainer;
import buildcraft.api.pipes.IPipePluggableRenderer;
import buildcraft.api.pipes.PipePluggable;
import buildcraft.core.utils.MatrixTranformations;

public class FacadePluggable extends PipePluggable {
	public ItemFacade.FacadeState[] states;
	private ItemFacade.FacadeState activeState;

	// Client sync
	private Block block;
	private int meta;
	private boolean transparent, renderAsHollow;

	public FacadePluggable(ItemFacade.FacadeState[] states) {
		this.states = states;
		activeState = states.length > 0 ? states[0] : null;
	}

	public FacadePluggable() {
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
	public ItemStack[] getDropItems(IPipeContainer pipe) {
		if (states != null) {
			return new ItemStack[] { ItemFacade.getFacade(states) };
		} else {
			return new ItemStack[] { ItemFacade.getFacade(new ItemFacade.FacadeState(getRenderingBlock(), getRenderingMeta(), null, isHollow())) };
		}
	}

	@Override
	public boolean isBlocking(IPipeContainer pipe, ForgeDirection direction) {
		return isHollow();
	}

	public boolean isHollow() {
		return states == null ? renderAsHollow : !states[0].hollow;
	}

	public Block getRenderingBlock() { return block; }
	public int getRenderingMeta() { return meta; }
	public boolean getRenderingTransparent() { return transparent; }

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
	public boolean isSolidOnSide(IPipeContainer pipe, ForgeDirection direction) {
		return !isHollow();
	}

	@Override
	public IPipePluggableRenderer getRenderer() {
		return null;
	}

	@Override
	public void writeData(ByteBuf data) {
		if (activeState == null) {
			activeState = states.length > 0 ? states[0] : null;
		}

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
		transparent = (flags & 0x80) > 0;
		renderAsHollow = (flags & 0x40) > 0;
	}

	protected void setActiveState(int id) {
		if (id >= 0 && id < states.length) {
			activeState = states[id];
		}
	}
}
