package buildcraft.transport.gates;

import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.utils.Localization;
import buildcraft.transport.Gate;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.Icon;

public class ItemGate extends ItemBuildCraft {

	private static final String NBT_TAG_MAT = "mat";
	private static final String NBT_TAG_LOGIC = "logic";
	private static final String NBT_TAG_EX = "ex";

	public ItemGate(int id) {
		super(id);
		setHasSubtypes(false);
		setMaxDamage(0);
		setPassSneakClick(true);
	}

	private static NBTTagCompound getNBT(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemGate))
			return null;
		return InvUtils.getItemData(stack);
	}

	public static void setMaterial(ItemStack stack, GateMaterial material) {
		NBTTagCompound nbt = InvUtils.getItemData(stack);
		nbt.setByte(NBT_TAG_MAT, (byte) material.ordinal());
	}

	public static GateMaterial getMaterial(ItemStack stack) {
		NBTTagCompound nbt = getNBT(stack);
		if (nbt == null)
			return GateMaterial.REDSTONE;
		return GateMaterial.fromOrdinal(nbt.getByte(NBT_TAG_MAT));
	}

	public static GateLogic getLogic(ItemStack stack) {
		NBTTagCompound nbt = getNBT(stack);
		if (nbt == null)
			return GateLogic.AND;
		return GateLogic.fromOrdinal(nbt.getByte(NBT_TAG_LOGIC));
	}

	public static void setLogic(ItemStack stack, GateLogic logic) {
		NBTTagCompound nbt = InvUtils.getItemData(stack);
		nbt.setByte(NBT_TAG_LOGIC, (byte) logic.ordinal());
	}

	public static void addGateExpansion(ItemStack stack, IGateExpansion expansion) {
		NBTTagCompound nbt = getNBT(stack);
		if (nbt == null)
			return;
		NBTTagList expansionList = nbt.getTagList(NBT_TAG_EX);
		expansionList.appendTag(new NBTTagString("", expansion.getUniqueIdentifier()));
		nbt.setTag(NBT_TAG_EX, expansionList);
	}

	public static boolean hasGateExpansion(ItemStack stack, IGateExpansion expansion) {
		NBTTagCompound nbt = getNBT(stack);
		if (nbt == null)
			return false;
		try {
			NBTTagList expansionList = nbt.getTagList(NBT_TAG_EX);
			for (int i = 0; i < expansionList.tagCount(); i++) {
				NBTTagString ex = (NBTTagString) expansionList.tagAt(i);
				if (ex.data.equals(expansion.getUniqueIdentifier()))
					return true;
			}
		} catch (RuntimeException error) {
		}
		return false;
	}

	public static Set<IGateExpansion> getInstalledExpansions(ItemStack stack) {
		Set<IGateExpansion> expansions = new HashSet<IGateExpansion>();
		NBTTagCompound nbt = getNBT(stack);
		if (nbt == null)
			return expansions;
		try {
			NBTTagList expansionList = nbt.getTagList(NBT_TAG_EX);
			for (int i = 0; i < expansionList.tagCount(); i++) {
				NBTTagString exTag = (NBTTagString) expansionList.tagAt(i);
				IGateExpansion ex = GateExpansions.getExpansion(exTag.data);
				if (ex != null)
					expansions.add(ex);
			}
		} catch (RuntimeException error) {
		}
		return expansions;
	}

	public static ItemStack makeGateItem(GateMaterial material, GateLogic logic) {
		ItemStack stack = new ItemStack(BuildCraftTransport.pipeGate);
		NBTTagCompound nbt = InvUtils.getItemData(stack);
		nbt.setByte(NBT_TAG_MAT, (byte) material.ordinal());
		nbt.setByte(NBT_TAG_LOGIC, (byte) logic.ordinal());
		return stack;
	}

	public static ItemStack makeGateItem(Gate gate) {
		ItemStack stack = new ItemStack(BuildCraftTransport.pipeGate);
		NBTTagCompound nbt = InvUtils.getItemData(stack);
		nbt.setByte(NBT_TAG_MAT, (byte) gate.material.ordinal());
		nbt.setByte(NBT_TAG_LOGIC, (byte) gate.logic.ordinal());
		for (IGateExpansion expansion : gate.expansions.keySet()) {
			addGateExpansion(stack, expansion);
		}
		return stack;
	}

	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return GateDefinition.getLocalizedName(getMaterial(stack), getLogic(stack));
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(int id, CreativeTabs tab, List itemList) {
		for (GateMaterial material : GateMaterial.VALUES) {
			for (GateLogic logic : GateLogic.VALUES) {
				if (material == GateMaterial.REDSTONE && logic == GateLogic.OR)
					continue;
				ItemStack stack = makeGateItem(material, logic);
				for (IGateExpansion exp : GateExpansions.getExpansions()) {
					addGateExpansion(stack, exp);
				}
				itemList.add(stack);
			}
		}
	}

	public static ItemStack[] getGateVarients() {
		ArrayList<ItemStack> gates = new ArrayList<ItemStack>();
		for (GateMaterial material : GateMaterial.VALUES) {
			for (GateLogic logic : GateLogic.VALUES) {
				if (material == GateMaterial.REDSTONE && logic == GateLogic.OR)
					continue;
				gates.add(makeGateItem(material, logic));
			}
		}
		return gates.toArray(new ItemStack[gates.size()]);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
		super.addInformation(stack, player, list, adv);
		list.add("§9§o" + Localization.get("tip.gate.wires"));
		list.add(Localization.get("tip.gate.wires." + getMaterial(stack).getTag()));
		Set<IGateExpansion> expansions = getInstalledExpansions(stack);
		if (!expansions.isEmpty()) {
			list.add("§9§o" + Localization.get("tip.gate.expansions"));
			for (IGateExpansion expansion : expansions) {
				list.add(expansion.getDisplayName());
			}
		}
	}

	@Override
	public Icon getIconIndex(ItemStack stack) {
		return getLogic(stack).getIconItem();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegister) {

		for (GateDefinition.GateMaterial material : GateDefinition.GateMaterial.VALUES) {
			material.registerItemIcon(iconRegister);
		}

		for (GateDefinition.GateLogic logic : GateDefinition.GateLogic.VALUES) {
			logic.registerItemIcon(iconRegister);
		}

		for (IGateExpansion expansion : GateExpansions.getExpansions()) {
			expansion.registerItemOverlay(iconRegister);
		}

		for (IAction action : ActionManager.actions.values()) {
			action.registerIcons(iconRegister);
		}

		for (ITrigger trigger : ActionManager.triggers.values()) {
			trigger.registerIcons(iconRegister);
		}
	}
}
