package buildcraft.core.blueprints;

import buildcraft.api.blueprints.BlueprintManager;
import buildcraft.api.blueprints.BptSlotInfo;
import buildcraft.api.blueprints.IBptContext;
import java.util.LinkedList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BptSlot extends BptSlotInfo {

	public enum Mode {
		ClearIfInvalid, Build
	};

	public Mode mode = Mode.Build;
	public ItemStack stackToUse;

	public boolean isValid(IBptContext context) {
		return BlueprintManager.blockBptProps[blockId].isValid(this, context);
	}

	public void rotateLeft(IBptContext context) {
		BlueprintManager.blockBptProps[blockId].rotateLeft(this, context);
	}

	public boolean ignoreBuilding() {
		return BlueprintManager.blockBptProps[blockId].ignoreBuilding(this);
	}

	public void initializeFromWorld(IBptContext context, int xs, int ys, int zs) {
		BlueprintManager.blockBptProps[blockId].initializeFromWorld(this, context, xs, ys, zs);
	}

	public void postProcessing(IBptContext context) {
		BlueprintManager.blockBptProps[blockId].postProcessing(this, context);
	}

	public LinkedList<ItemStack> getRequirements(IBptContext context) {
		LinkedList<ItemStack> res = new LinkedList<ItemStack>();

		BlueprintManager.blockBptProps[blockId].addRequirements(this, context, res);

		return res;
	}

	public final void buildBlock(IBptContext context) {
		BlueprintManager.blockBptProps[blockId].buildBlock(this, context);
	}

	// returns what was used
	public ItemStack useItem(IBptContext context, ItemStack req, ItemStack stack) {
		return BlueprintManager.blockBptProps[blockId].useItem(this, context, req, stack);
	}

	@SuppressWarnings("unchecked")
	@Override
	public BptSlot clone() {
		BptSlot obj = new BptSlot();

		obj.x = x;
		obj.y = y;
		obj.z = z;
		obj.blockId = blockId;
		obj.meta = meta;
		obj.cpt = (NBTTagCompound) cpt.copy();
		obj.storedRequirements = (LinkedList<ItemStack>) storedRequirements.clone();

		if (stackToUse != null) {
			obj.stackToUse = stackToUse.copy();
		}

		obj.mode = mode;

		return obj;
	}

}
