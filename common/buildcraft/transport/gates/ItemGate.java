package buildcraft.transport.gates;

import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.ActionManager;
import buildcraft.api.gates.GateExpansionController;
import buildcraft.api.gates.IAction;
import buildcraft.api.gates.ITrigger;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.inventory.InvUtils;
import buildcraft.transport.Gate;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
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

	public static GateMaterial getMaterial(ItemStack stack) {
		NBTTagCompound nbt = getNBT(stack);
		if (nbt == null)
			return GateMaterial.REDSTONE;
		return GateMaterial.fromOrdinal(nbt.getByte("mat"));
	}

	public static GateLogic getLogic(ItemStack stack) {
		NBTTagCompound nbt = getNBT(stack);
		if (nbt == null)
			return GateLogic.AND;
		return GateLogic.fromOrdinal(nbt.getByte("logic"));
	}

	public static void addGateExpansion(ItemStack stack, IGateExpansion expansion) {
		NBTTagCompound nbt = getNBT(stack);
		if (nbt == null)
			return;
		NBTTagList expansionList = nbt.getTagList("ex");
		expansionList.appendTag(new NBTTagString("ex", expansion.getUniqueIdentifier()));
		nbt.setTag("ex", expansionList);
	}

	public static boolean hasGateExpansion(ItemStack stack, IGateExpansion expansion) {
		NBTTagCompound nbt = getNBT(stack);
		if (nbt == null)
			return false;
		try {
			NBTTagList expansionList = nbt.getTagList("ex");
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
			NBTTagList expansionList = nbt.getTagList("ex");
			for (int i = 0; i < expansionList.tagCount(); i++) {
				NBTTagString exTag = (NBTTagString) expansionList.tagAt(i);
				IGateExpansion ex = GateExpansions.expansions.get(exTag.data);
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
		nbt.setByte("mat", (byte) material.ordinal());
		nbt.setByte("logic", (byte) logic.ordinal());
		return stack;
	}

	public static ItemStack makeGateItem(Gate gate) {
		ItemStack stack = new ItemStack(BuildCraftTransport.pipeGate);
		NBTTagCompound nbt = InvUtils.getItemData(stack);
		nbt.setByte("mat", (byte) gate.material.ordinal());
		nbt.setByte("logic", (byte) gate.logic.ordinal());
		for (GateExpansionController expansion : gate.expansions) {
			addGateExpansion(stack, expansion.getType());
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
				for (IGateExpansion exp : GateExpansions.expansions.values()) {
					addGateExpansion(stack, exp);
				}
				itemList.add(stack);
			}
		}
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
		super.addInformation(stack, player, list, adv);
		for (IGateExpansion expansion : getInstalledExpansions(stack)) {
			list.add(expansion.getDisplayName());
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

		for (IGateExpansion expansion : GateExpansions.expansions.values()) {
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
