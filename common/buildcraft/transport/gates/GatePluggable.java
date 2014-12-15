package buildcraft.transport.gates;

import java.util.Set;
import io.netty.buffer.ByteBuf;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.AxisAlignedBB;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.pipes.IPipeContainer;
import buildcraft.api.pipes.IPipePluggable;
import buildcraft.api.pipes.IPipePluggableRenderer;
import buildcraft.core.CoreConstants;
import buildcraft.core.utils.MatrixTranformations;
import buildcraft.transport.Gate;
import buildcraft.transport.TileGenericPipe;

public class GatePluggable implements IPipePluggable {
	public GateDefinition.GateMaterial material;
	public GateDefinition.GateLogic logic;
	public IGateExpansion[] expansions;

	public GatePluggable() {
	}

	public GatePluggable(Gate gate) {
		this.material = gate.material;
		this.logic = gate.logic;

		Set<IGateExpansion> gateExpansions = gate.expansions.keySet();
		this.expansions = gateExpansions.toArray(new IGateExpansion[gateExpansions.size()]);
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setByte(ItemGate.NBT_TAG_MAT, (byte) material.ordinal());
		nbt.setByte(ItemGate.NBT_TAG_LOGIC, (byte) logic.ordinal());

		NBTTagList expansionsList = nbt.getTagList(ItemGate.NBT_TAG_EX, Constants.NBT.TAG_STRING);
		for (IGateExpansion expansion : expansions) {
			expansionsList.appendTag(new NBTTagString(expansion.getUniqueIdentifier()));
		}
		nbt.setTag(ItemGate.NBT_TAG_EX, expansionsList);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		material = GateDefinition.GateMaterial.fromOrdinal(nbt.getByte(ItemGate.NBT_TAG_MAT));
		logic = GateDefinition.GateLogic.fromOrdinal(nbt.getByte(ItemGate.NBT_TAG_LOGIC));

		NBTTagList expansionsList = nbt.getTagList(ItemGate.NBT_TAG_EX, Constants.NBT.TAG_STRING);
		final int expansionsSize = expansionsList.tagCount();
		expansions = new IGateExpansion[expansionsSize];
		for (int i = 0; i < expansionsSize; i++) {
			expansions[i] = GateExpansions.getExpansion(expansionsList.getStringTagAt(i));
		}
	}

	@Override
	public void writeData(ByteBuf buf) {
		buf.writeByte(material.ordinal());
		buf.writeByte(logic.ordinal());

		final int expansionsSize = expansions.length;
		buf.writeInt(expansionsSize);
		for (IGateExpansion expansion : expansions) {
			buf.writeShort(GateExpansions.getExpansionID(expansion));
		}
	}

	@Override
	public void readData(ByteBuf buf) {
		material = GateDefinition.GateMaterial.fromOrdinal(buf.readByte());
		logic = GateDefinition.GateLogic.fromOrdinal(buf.readByte());

		final int expansionsSize = buf.readInt();
		expansions = new IGateExpansion[expansionsSize];
		for (int i = 0; i < expansionsSize; i++) {
			expansions[i] = GateExpansions.getExpansionByID(buf.readUnsignedShort());
		}
	}

	@Override
	public ItemStack[] getDropItems(IPipeContainer pipe) {
		ItemStack gate = ItemGate.makeGateItem(material, logic);
		for (IGateExpansion expansion : expansions) {
			ItemGate.addGateExpansion(gate, expansion);
		}
		return new ItemStack[] { gate };
	}

	@Override
	public void update(IPipeContainer pipe, ForgeDirection direction) {

	}

	@Override
	public void onAttachedPipe(IPipeContainer pipe, ForgeDirection direction) {
		TileGenericPipe pipeReal = (TileGenericPipe) pipe;
		if (!pipeReal.getWorld().isRemote) {
			Gate gate = pipeReal.pipe.gates[direction.ordinal()];
			if (gate == null || gate.material != material || gate.logic != logic) {
				pipeReal.pipe.gates[direction.ordinal()] = GateFactory.makeGate(pipeReal.pipe, material, logic, direction);
				pipeReal.scheduleRenderUpdate();
			}
		}
	}

	@Override
	public void onDetachedPipe(IPipeContainer pipe, ForgeDirection direction) {
		TileGenericPipe pipeReal = (TileGenericPipe) pipe;
		if (!pipeReal.getWorld().isRemote) {
			Gate gate = pipeReal.pipe.gates[direction.ordinal()];
			if (gate != null) {
				gate.resetGate();
				pipeReal.pipe.gates[direction.ordinal()] = null;
			}
			pipeReal.scheduleRenderUpdate();
		}
	}

	@Override
	public boolean isBlocking(IPipeContainer pipe, ForgeDirection direction) {
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		}
		if (!(obj instanceof GatePluggable)) {
			return false;
		}
		GatePluggable o = (GatePluggable) obj;
		if (o.material.ordinal() != material.ordinal()) {
			return false;
		}
		if (o.logic.ordinal() != logic.ordinal()) {
			return false;
		}
		if (o.expansions.length != expansions.length) {
			return false;
		}
		for (int i = 0; i < expansions.length; i++) {
			if (o.expansions[i] != expansions[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void invalidate() {

	}

	@Override
	public void validate(IPipeContainer pipe, ForgeDirection direction) {

	}

	@Override
	public AxisAlignedBB getBoundingBox(ForgeDirection side) {
		float min = CoreConstants.PIPE_MIN_POS + 0.05F;
		float max = CoreConstants.PIPE_MAX_POS - 0.05F;

		float[][] bounds = new float[3][2];
		// X START - END
		bounds[0][0] = min;
		bounds[0][1] = max;
		// Y START - END
		bounds[1][0] = CoreConstants.PIPE_MIN_POS - 0.10F;
		bounds[1][1] = CoreConstants.PIPE_MIN_POS;
		// Z START - END
		bounds[2][0] = min;
		bounds[2][1] = max;

		MatrixTranformations.transform(bounds, side);
		return AxisAlignedBB.getBoundingBox(bounds[0][0], bounds[1][0], bounds[2][0], bounds[0][1], bounds[1][1], bounds[2][1]);
	}

	// TODO: Port Gates to the Pluggable render system
	@Override
	public IPipePluggableRenderer getRenderer() {
		return null;
	}
}
