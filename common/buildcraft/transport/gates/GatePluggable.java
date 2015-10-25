package buildcraft.transport.gates;

import java.util.Set;

import io.netty.buffer.ByteBuf;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.AxisAlignedBB;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.render.ITextureStates;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.pluggable.IPipePluggableDynamicRenderer;
import buildcraft.api.transport.pluggable.IPipePluggableRenderer;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.CoreConstants;
import buildcraft.core.lib.utils.MatrixTranformations;
import buildcraft.transport.Gate;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.render.PipeRendererTESR;

public class GatePluggable extends PipePluggable {
	private static final class GatePluggableRenderer implements IPipePluggableRenderer, IPipePluggableDynamicRenderer {
		public static final GatePluggableRenderer INSTANCE = new GatePluggableRenderer();

		private GatePluggableRenderer() {

		}

		@Override
		public void renderPluggable(IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, double x, double y, double z) {
			PipeRendererTESR.renderGate(x, y, z, (GatePluggable) pipePluggable, side);
		}

		@Override
		public void renderPluggable(RenderBlocks renderblocks, IPipe pipe, ForgeDirection side, PipePluggable pipePluggable, ITextureStates blockStateMachine, int renderPass, int x, int y, int z) {
			if (renderPass == 0) {
				PipeRendererTESR.renderGateStatic(renderblocks, side, (GatePluggable) pipePluggable, blockStateMachine, x, y, z);
			}
		}
	}

	public GateDefinition.GateMaterial material;
	public GateDefinition.GateLogic logic;
	public IGateExpansion[] expansions;
	public boolean isLit, isPulsing;

	public Gate realGate, instantiatedGate;
	private float pulseStage;

	public GatePluggable() {
	}

	public GatePluggable(Gate gate) {
		instantiatedGate = gate;
		initFromGate(gate);
	}

	private void initFromGate(Gate gate) {
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
		buf.writeBoolean(realGate != null ? realGate.isGateActive() : false);
		buf.writeBoolean(realGate != null ? realGate.isGatePulsing() : false);

		final int expansionsSize = expansions.length;
		buf.writeShort(expansionsSize);

		for (IGateExpansion expansion : expansions) {
			buf.writeShort(GateExpansions.getExpansionID(expansion));
		}
	}

	@Override
	public void readData(ByteBuf buf) {
		material = GateDefinition.GateMaterial.fromOrdinal(buf.readByte());
		logic = GateDefinition.GateLogic.fromOrdinal(buf.readByte());
		isLit = buf.readBoolean();
		isPulsing = buf.readBoolean();

		final int expansionsSize = buf.readUnsignedShort();
		expansions = new IGateExpansion[expansionsSize];

		for (int i = 0; i < expansionsSize; i++) {
			expansions[i] = GateExpansions.getExpansionByID(buf.readUnsignedShort());
		}
	}

	@Override
	public boolean requiresRenderUpdate(PipePluggable o) {
		// rendered by TESR
		return false;
	}

	@Override
	public ItemStack[] getDropItems(IPipeTile pipe) {
		ItemStack gate = ItemGate.makeGateItem(material, logic);
		for (IGateExpansion expansion : expansions) {
			ItemGate.addGateExpansion(gate, expansion);
		}
		return new ItemStack[]{gate};
	}

	@Override
	public void update(IPipeTile pipe, ForgeDirection direction) {
		if (isPulsing || pulseStage > 0.11F) {
			// if it is moving, or is still in a moved state, then complete
			// the current movement
			pulseStage = (pulseStage + 0.01F) % 1F;
		} else {
			pulseStage = 0;
		}
	}

	@Override
	public void onAttachedPipe(IPipeTile pipe, ForgeDirection direction) {
		TileGenericPipe pipeReal = (TileGenericPipe) pipe;
		if (!pipeReal.getWorld().isRemote) {
			if (instantiatedGate != null) {
				pipeReal.pipe.gates[direction.ordinal()] = instantiatedGate;
			} else {
				Gate gate = pipeReal.pipe.gates[direction.ordinal()];
				if (gate == null || gate.material != material || gate.logic != logic) {
					pipeReal.pipe.gates[direction.ordinal()] = GateFactory.makeGate(pipeReal.pipe, material, logic, direction);
					for (IGateExpansion expansion : expansions) {
						pipeReal.pipe.gates[direction.ordinal()].addGateExpansion(expansion);
					}
					pipeReal.scheduleRenderUpdate();
				}
			}

			realGate = pipeReal.pipe.gates[direction.ordinal()];
		}
	}

	@Override
	public void onDetachedPipe(IPipeTile pipe, ForgeDirection direction) {
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
	public boolean isBlocking(IPipeTile pipe, ForgeDirection direction) {
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

	@Override
	public IPipePluggableRenderer getRenderer() {
		return GatePluggableRenderer.INSTANCE;
	}

	@Override
	public IPipePluggableDynamicRenderer getDynamicRenderer() {
		return GatePluggableRenderer.INSTANCE;
	}

	public float getPulseStage() {
		return pulseStage;
	}

	public GateDefinition.GateMaterial getMaterial() {
		return material;
	}

	public GateDefinition.GateLogic getLogic() {
		return logic;
	}

	public IGateExpansion[] getExpansions() {
		return expansions;
	}
}
