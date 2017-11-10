/**
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gates;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.transport.Gate;
import buildcraft.transport.Pipe;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;

public final class GateFactory {

	/**
	 * Deactivate constructor
	 */
	private GateFactory() {
	}

	public static Gate makeGate(Pipe<?> pipe, GateMaterial material, GateLogic logic, ForgeDirection direction) {
		return new Gate(pipe, material, logic, direction);
	}

	public static Gate makeGate(Pipe<?> pipe, ItemStack stack, ForgeDirection direction) {
		if (stack == null || stack.stackSize <= 0 || !(stack.getItem() instanceof ItemGate)) {
			return null;
		}

		Gate gate = makeGate(pipe, ItemGate.getMaterial(stack), ItemGate.getLogic(stack), direction);

		for (IGateExpansion expansion : ItemGate.getInstalledExpansions(stack)) {
			gate.addGateExpansion(expansion);
		}

		return gate;
	}

	public static Gate makeGate(Pipe<?> pipe, NBTTagCompound nbt) {
		GateMaterial material = GateMaterial.REDSTONE;
		GateLogic logic = GateLogic.AND;
		ForgeDirection direction = ForgeDirection.UNKNOWN;

		// Legacy Support
		if (nbt.hasKey("Kind")) {
			int kind = nbt.getInteger("Kind");
			switch (kind) {
				case 1:
				case 2:
					material = GateMaterial.IRON;
					break;
				case 3:
				case 4:
					material = GateMaterial.GOLD;
					break;
				case 5:
				case 6:
					material = GateMaterial.DIAMOND;
					break;
			}
			switch (kind) {
				case 2:
				case 4:
				case 6:
					logic = GateLogic.OR;
					break;
			}
		}

		if (nbt.hasKey("material")) {
			try {
				material = GateMaterial.valueOf(nbt.getString("material"));
			} catch (IllegalArgumentException ex) {
				return null;
			}
		}
		if (nbt.hasKey("logic")) {
			try {
				logic = GateLogic.valueOf(nbt.getString("logic"));
			} catch (IllegalArgumentException ex) {
				return null;
			}
		}
		if (nbt.hasKey("direction")) {
			direction = ForgeDirection.getOrientation(nbt.getInteger("direction"));
		}

		Gate gate = makeGate(pipe, material, logic, direction);
		gate.readFromNBT(nbt);

		// Legacy support
		if (nbt.hasKey("Pulser")) {
			NBTTagCompound pulsarTag = nbt.getCompoundTag("Pulser");
			GateExpansionController pulsarCon = GateExpansionPulsar.INSTANCE.makeController(pipe.container);
			pulsarCon.readFromNBT(pulsarTag);
			gate.expansions.put(GateExpansionPulsar.INSTANCE, pulsarCon);
		}

		NBTTagList exList = nbt.getTagList("expansions", Constants.NBT.TAG_COMPOUND);
		for (int i = 0; i < exList.tagCount(); i++) {
			NBTTagCompound conNBT = exList.getCompoundTagAt(i);
			IGateExpansion ex = GateExpansions.getExpansion(conNBT.getString("type"));
			if (ex != null) {
				GateExpansionController con = ex.makeController(pipe.container);
				con.readFromNBT(conNBT.getCompoundTag("data"));
				gate.expansions.put(ex, con);
			}
		}

		return gate;
	}
}
