package buildcraft.transport;

import java.util.LinkedList;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.Action;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.api.gates.Trigger;
import buildcraft.core.network.IndexInPayload;
import buildcraft.core.network.PacketPayload;

import net.minecraft.src.EntityPlayer;
import net.minecraft.src.ItemStack;
import net.minecraft.src.NBTTagCompound;
import net.minecraft.src.World;

public abstract class Gate {

	public static enum GateKind {
		None, Single, AND_2, OR_2, AND_3, OR_3, AND_4, OR_4;

		public static GateKind getKindFromDamage(ItemStack itemstack) {
			switch (itemstack.getItemDamage()) {
			case 0:
				return Single;
			case 1:
				return AND_2;
			case 2:
				return OR_2;
			case 3:
				return AND_3;
			case 4:
				return OR_3;
			case 5:
				return AND_4;
			case 6:
				return OR_4;
			default:
				return None;
			}
		}
	}

	public static enum GateConditional {
		None, AND, OR
	}

	protected Pipe pipe;
	public GateKind kind;

	// / CONSTRUCTOR
	public Gate(Pipe pipe) {
		this.pipe = pipe;
	}

	public Gate(Pipe pipe, ItemStack stack) {

		this.pipe = pipe;
		kind = GateKind.getKindFromDamage(stack);
	}

	// / SAVING & LOADING
	public void writeToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("Kind", kind.ordinal());
	}

	public void readFromNBT(NBTTagCompound nbttagcompound) {
		kind = Gate.GateKind.values()[nbttagcompound.getInteger("Kind")];
	}

	// / SMP
	public PacketPayload toPayload() {
		PacketPayload payload = new PacketPayload(1, 0, 0);
		payload.intPayload[0] = kind.ordinal();
		return payload;
	}

	public void fromPayload(PacketPayload payload, IndexInPayload index) {
		kind = GateKind.values()[payload.intPayload[index.intIndex + 0]];
	}

	// GUI
	public abstract void openGui(EntityPlayer player);

	// / UPDATING
	public abstract void update();

	public abstract void dropGate(World world, int i, int j, int k);

	// / INFORMATION
	public abstract String getName();

	public abstract GateConditional getConditional();

	// / ACTIONS
	public abstract void addActions(LinkedList<IAction> list);

	public abstract void startResolution();

	public abstract boolean resolveAction(IAction action);

	// / TRIGGERS
	public abstract void addTrigger(LinkedList<ITrigger> list);

	// / TEXTURES
	public abstract int getTexture(boolean isSignalActive);

	public abstract String getGuiFile();

	public static boolean isGateItem(ItemStack stack) {
		return stack.itemID == BuildCraftTransport.pipeGate.shiftedIndex
				|| stack.itemID == BuildCraftTransport.pipeGateAutarchic.shiftedIndex;
	}

}
