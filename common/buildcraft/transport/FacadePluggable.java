package buildcraft.transport;

import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.pipes.IPipeContainer;
import buildcraft.api.pipes.IPipePluggable;
import buildcraft.api.pipes.IPipePluggableRenderer;
import buildcraft.core.utils.MatrixTranformations;

public class FacadePluggable implements IPipePluggable {
	public ItemFacade.FacadeState[] states;

	public FacadePluggable(ItemFacade.FacadeState[] states) {
		this.states = states;
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
		return states == null ? null : new ItemStack[] { ItemFacade.getFacade(states) };
	}

	@Override
	public void onAttachedPipe(IPipeContainer pipe, ForgeDirection direction) {

	}

	@Override
	public void onDetachedPipe(IPipeContainer pipe, ForgeDirection direction) {

	}

	@Override
	public boolean isBlocking(IPipeContainer pipe, ForgeDirection direction) {
		return false;
	}

	@Override
	public void invalidate() {

	}

	@Override
	public void validate(IPipeContainer pipe, ForgeDirection direction) {

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
	public IPipePluggableRenderer getRenderer() {
		return null;
	}

	@Override
	public void writeData(ByteBuf data) {
	}

	@Override
	public void readData(ByteBuf data) {

	}
}
