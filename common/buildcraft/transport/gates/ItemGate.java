/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.transport.gates;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.BuildCraftTransport;
import buildcraft.api.gates.GateExpansions;
import buildcraft.api.gates.IGateExpansion;
import buildcraft.api.gates.IStatement;
import buildcraft.api.gates.StatementManager;
import buildcraft.api.transport.IPipePluggable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.core.ItemBuildCraft;
import buildcraft.core.inventory.InvUtils;
import buildcraft.core.utils.StringUtils;
import buildcraft.transport.Gate;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.gates.GateDefinition.GateLogic;
import buildcraft.transport.gates.GateDefinition.GateMaterial;

public class ItemGate extends ItemBuildCraft {
	public static class GatePluggable implements IPipePluggable {
		public GateMaterial material;
		public GateLogic logic;
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
			nbt.setByte(NBT_TAG_MAT, (byte) material.ordinal());
			nbt.setByte(NBT_TAG_LOGIC, (byte) logic.ordinal());

			NBTTagList expansionsList = nbt.getTagList(NBT_TAG_EX, Constants.NBT.TAG_STRING);
			for (IGateExpansion expansion : expansions) {
				expansionsList.appendTag(new NBTTagString(expansion.getUniqueIdentifier()));
			}
			nbt.setTag(NBT_TAG_EX, expansionsList);
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			material = GateMaterial.fromOrdinal(nbt.getByte(NBT_TAG_MAT));
			logic = GateLogic.fromOrdinal(nbt.getByte(NBT_TAG_LOGIC));

			NBTTagList expansionsList = nbt.getTagList(NBT_TAG_EX, Constants.NBT.TAG_STRING);
			final int expansionsSize = expansionsList.tagCount();
			expansions = new IGateExpansion[expansionsSize];
			for (int i = 0; i < expansionsSize; i++) {
				expansions[i] = GateExpansions.getExpansion(expansionsList.getStringTagAt(i));
			}
		}

		public void writeToByteByf(ByteBuf buf) {
			buf.writeByte(material.ordinal());
			buf.writeByte(logic.ordinal());

			final int expansionsSize = expansions.length;
			buf.writeInt(expansionsSize);
			for (IGateExpansion expansion : expansions) {
				buf.writeByte(GateExpansions.getServerExpansionID(expansion.getUniqueIdentifier()));
			}
		}

		public void readFromByteBuf(ByteBuf buf) {
			material = GateMaterial.fromOrdinal(buf.readByte());
			logic = GateLogic.fromOrdinal(buf.readByte());

			final int expansionsSize = buf.readInt();
			expansions = new IGateExpansion[expansionsSize];
			for (int i = 0; i < expansionsSize; i++) {
				expansions[i] = GateExpansions.getExpansionClient(buf.readByte());
			}
		}

		@Override
		public ItemStack[] getDropItems(IPipeTile pipe) {
			ItemStack gate = makeGateItem(material, logic);
			for (IGateExpansion expansion : expansions) {
				addGateExpansion(gate, expansion);
			}
			return new ItemStack[] { gate };
		}

		@Override
		public void onAttachedPipe(IPipeTile pipe, ForgeDirection direction) {
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
		public boolean blocking(IPipeTile pipe, ForgeDirection direction) {
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
	}

	private static final String NBT_TAG_MAT = "mat";
	private static final String NBT_TAG_LOGIC = "logic";
	private static final String NBT_TAG_EX = "ex";

	public ItemGate() {
		super();
		setHasSubtypes(false);
		setMaxDamage(0);
		setPassSneakClick(true);
	}

	private static NBTTagCompound getNBT(ItemStack stack) {
		if (stack == null || !(stack.getItem() instanceof ItemGate)) {
			return null;
		} else {
			return InvUtils.getItemData(stack);
		}
	}

	public static void setMaterial(ItemStack stack, GateMaterial material) {
		NBTTagCompound nbt = InvUtils.getItemData(stack);
		nbt.setByte(NBT_TAG_MAT, (byte) material.ordinal());
	}

	public static GateMaterial getMaterial(ItemStack stack) {
		NBTTagCompound nbt = getNBT(stack);

		if (nbt == null) {
			return GateMaterial.REDSTONE;
		} else {
			return GateMaterial.fromOrdinal(nbt.getByte(NBT_TAG_MAT));
		}
	}

	public static GateLogic getLogic(ItemStack stack) {
		NBTTagCompound nbt = getNBT(stack);

		if (nbt == null) {
			return GateLogic.AND;
		} else {
			return GateLogic.fromOrdinal(nbt.getByte(NBT_TAG_LOGIC));
		}
	}

	public static void setLogic(ItemStack stack, GateLogic logic) {
		NBTTagCompound nbt = InvUtils.getItemData(stack);
		nbt.setByte(NBT_TAG_LOGIC, (byte) logic.ordinal());
	}

	public static void addGateExpansion(ItemStack stack, IGateExpansion expansion) {
		NBTTagCompound nbt = getNBT(stack);

		if (nbt == null) {
			return;
		}

		NBTTagList expansionList = nbt.getTagList(NBT_TAG_EX, Constants.NBT.TAG_STRING);
		expansionList.appendTag(new NBTTagString(expansion.getUniqueIdentifier()));
		nbt.setTag(NBT_TAG_EX, expansionList);
	}

	public static boolean hasGateExpansion(ItemStack stack, IGateExpansion expansion) {
		NBTTagCompound nbt = getNBT(stack);

		if (nbt == null) {
			return false;
		}

		try {
			NBTTagList expansionList = nbt.getTagList(NBT_TAG_EX, Constants.NBT.TAG_STRING);

			for (int i = 0; i < expansionList.tagCount(); i++) {
				String ex = expansionList.getStringTagAt(i);

				if (ex.equals(expansion.getUniqueIdentifier())) {
					return true;
				}
			}
		} catch (RuntimeException error) {
		}

		return false;
	}

	public static Set<IGateExpansion> getInstalledExpansions(ItemStack stack) {
		Set<IGateExpansion> expansions = new HashSet<IGateExpansion>();
		NBTTagCompound nbt = getNBT(stack);

		if (nbt == null) {
			return expansions;
		}

		try {
			NBTTagList expansionList = nbt.getTagList(NBT_TAG_EX, Constants.NBT.TAG_STRING);
			for (int i = 0; i < expansionList.tagCount(); i++) {
				String exTag = expansionList.getStringTagAt(i);
				IGateExpansion ex = GateExpansions.getExpansion(exTag);
				if (ex != null) {
					expansions.add(ex);
				}
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

	@Override
	public String getItemStackDisplayName(ItemStack stack) {
		return ("" + StatCollector.translateToLocal(this.getUnlocalizedNameInefficiently(stack))).trim();
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List itemList) {
		for (GateMaterial material : GateMaterial.VALUES) {
			for (GateLogic logic : GateLogic.VALUES) {
				if (material == GateMaterial.REDSTONE && logic == GateLogic.OR) {
					continue;
				}

				itemList.add(makeGateItem(material, logic));

				for (IGateExpansion exp : GateExpansions.getExpansions()) {
					ItemStack stackExpansion = makeGateItem(material, logic);
					addGateExpansion(stackExpansion, exp);
					itemList.add(stackExpansion);
				}
			}
		}
	}

	public static ItemStack[] getGateVarients() {
		ArrayList<ItemStack> gates = new ArrayList<ItemStack>();

		for (GateMaterial material : GateMaterial.VALUES) {
			for (GateLogic logic : GateLogic.VALUES) {
				if (material == GateMaterial.REDSTONE && logic == GateLogic.OR) {
					continue;
				}

				gates.add(makeGateItem(material, logic));
			}
		}

		return gates.toArray(new ItemStack[gates.size()]);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean adv) {
		super.addInformation(stack, player, list, adv);

		list.add(StringUtils.localize("tip.gate.wires"));
		list.add(StringUtils.localize("tip.gate.wires." + getMaterial(stack).getTag()));
		Set<IGateExpansion> expansions = getInstalledExpansions(stack);

		if (!expansions.isEmpty()) {
			list.add(StringUtils.localize("tip.gate.expansions"));

			for (IGateExpansion expansion : expansions) {
				list.add(expansion.getDisplayName());
			}
		}
	}

	@Override
	public IIcon getIconIndex(ItemStack stack) {
		return getLogic(stack).getIconItem();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister iconRegister) {
		for (GateDefinition.GateMaterial material : GateDefinition.GateMaterial.VALUES) {
			material.registerItemIcon(iconRegister);
		}

		for (GateDefinition.GateLogic logic : GateDefinition.GateLogic.VALUES) {
			logic.registerItemIcon(iconRegister);
		}

		for (IGateExpansion expansion : GateExpansions.getExpansions()) {
			expansion.registerItemOverlay(iconRegister);
		}

		for (IStatement statement : StatementManager.statements.values()) {
			statement.registerIcons(iconRegister);
		}
	}
}
